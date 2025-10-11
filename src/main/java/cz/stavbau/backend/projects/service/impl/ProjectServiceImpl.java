package cz.stavbau.backend.projects.service.impl;

import cz.stavbau.backend.common.exception.BadRequest;
import cz.stavbau.backend.common.exception.InvalidException;
import cz.stavbau.backend.common.exception.NotFoundException;
import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.common.i18n.Messages;
import cz.stavbau.backend.common.paging.DomainSortPolicies;
import cz.stavbau.backend.common.paging.PagingPolicy;
import cz.stavbau.backend.common.simple.IdNameView;
import cz.stavbau.backend.customers.repo.CustomerRepository;
import cz.stavbau.backend.projects.dto.*;
import cz.stavbau.backend.projects.filter.ProjectFilter;
import cz.stavbau.backend.projects.filter.ProjectFilters;
import cz.stavbau.backend.projects.i18n.ProjectTranslationService;
import cz.stavbau.backend.projects.mapper.ProjectMapper;
import cz.stavbau.backend.projects.model.Project;
import cz.stavbau.backend.projects.model.ProjectStatus;
import cz.stavbau.backend.projects.model.ProjectTranslation;
import cz.stavbau.backend.projects.model.ProjectTranslationId;
import cz.stavbau.backend.projects.persistence.ProjectSpecification;
import cz.stavbau.backend.projects.repo.ProjectRepository;
import cz.stavbau.backend.projects.repo.ProjectTranslationRepository;
import cz.stavbau.backend.projects.service.ProjectCodeGenerator;
import cz.stavbau.backend.projects.service.ProjectService;
import cz.stavbau.backend.security.SecurityUtils;
import cz.stavbau.backend.tenants.membership.repo.CompanyMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static cz.stavbau.backend.common.validation.ValidationUtils.validateDates;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final Messages messages;
    private final ProjectRepository projectRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final CustomerRepository customerRepository;
    private final ProjectTranslationRepository translationRepository;
    private final ProjectMapper mapper;
    private final I18nLocaleService i18nLocale;
    private final ProjectCodeGenerator codeGenerator;
    private final ProjectTranslationService translationService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectSummaryDto> list(ProjectFilter filter, Pageable pageable) {
        // 1) Tenant guard
        UUID companyId = SecurityUtils.requireCompanyId();

        // 2) (volitelně) normalizace filtru
        ProjectFilter norm = ProjectFilters.normalize(filter);

        // 3) Bezpečný paging + sort
        PageRequest paging = PagingPolicy.ensure(
                pageable,
                DomainSortPolicies.PROJECT_MAX_PAGE_SIZE,
                DomainSortPolicies.PROJECT_DEFAULT_SORT,   // obsahuje i "name"
                DomainSortPolicies.PROJECT_ALLOWED_SORT
        );

        // 4) Spec + načtení entit
        Specification<Project> spec = new ProjectSpecification(companyId, norm);
        Page<Project> page = projectRepository.findAll(spec, paging);

        // 5) Mapování na DTO (kanonické Project.name)
        List<ProjectSummaryDto> items = mapper.toSummaryList(page.getContent());

        // 6) BATCH jména: customerName / projectManagerName
        Set<UUID> customerIds = new HashSet<>();
        Set<UUID> pmIds = new HashSet<>();
        for (Project p : page.getContent()) {
            if (p.getCustomerId() != null) customerIds.add(p.getCustomerId());
            if (p.getProjectManagerId() != null) pmIds.add(p.getProjectManagerId());
        }
        Map<UUID,String> customerNames = customerIds.isEmpty()
                ? Collections.emptyMap()
                : customerRepository.findNamesByCompanyAndIdIn(companyId, customerIds)
                .stream().collect(Collectors.toMap(IdNameView::getId, IdNameView::getName));

        Map<UUID,String> pmNames = pmIds.isEmpty()
                ? Collections.emptyMap()
                : companyMemberRepository.findMemberNamesByCompanyAndIdIn(companyId, pmIds)
                .stream().collect(Collectors.toMap(IdNameView::getId, IdNameView::getName));

        for (ProjectSummaryDto dto : items) {
            if (dto.getCustomerId() != null) {
                dto.setCustomerName(customerNames.get(dto.getCustomerId()));
            }
            if (dto.getProjectManagerId() != null) {
                dto.setProjectManagerName(pmNames.get(dto.getProjectManagerId()));
            }
        }

        // 7) i18n overlay názvů – hromadně, bez N+1 (vrátí jen ty, které mají překlad)
        if (!items.isEmpty()) {
            Set<UUID> ids = page.getContent().stream()
                    .map(Project::getId)
                    .collect(Collectors.toSet());

            Map<UUID, String> trNames = translationService.batchResolveNames(ids);
            for (int i = 0; i < items.size(); i++) {
                UUID pid = page.getContent().get(i).getId();
                String trName = trNames.get(pid);
                if (trName != null && !trName.isBlank()) {
                    items.get(i).setName(trName);
                }
            }
        }

        Page<ProjectSummaryDto> dtoPage = new PageImpl<>(items, paging, page.getTotalElements());

        // 8) UX fallback – když aktuální stránka vyšla prázdná a existují data dřív, vrať o 1 zpět
        if (dtoPage.isEmpty() && dtoPage.getTotalElements() > 0 && paging.getPageNumber() > 0) {
            PageRequest prev = PageRequest.of(paging.getPageNumber() - 1, paging.getPageSize(), paging.getSort());
            page = projectRepository.findAll(spec, prev);
            items = mapper.toSummaryList(page.getContent());

            // znovu doplň batch jména
            customerIds.clear(); pmIds.clear();
            for (Project p : page.getContent()) {
                if (p.getCustomerId() != null) customerIds.add(p.getCustomerId());
                if (p.getProjectManagerId() != null) pmIds.add(p.getProjectManagerId());
            }
            customerNames = customerIds.isEmpty()
                    ? Collections.emptyMap()
                    : customerRepository.findNamesByCompanyAndIdIn(companyId, customerIds)
                    .stream().collect(Collectors.toMap(IdNameView::getId, IdNameView::getName));
            pmNames = pmIds.isEmpty()
                    ? Collections.emptyMap()
                    : companyMemberRepository.findMemberNamesByCompanyAndIdIn(companyId, pmIds)
                    .stream().collect(Collectors.toMap(IdNameView::getId, IdNameView::getName));

            for (ProjectSummaryDto dto : items) {
                if (dto.getCustomerId() != null) dto.setCustomerName(customerNames.get(dto.getCustomerId()));
                if (dto.getProjectManagerId() != null) dto.setProjectManagerName(pmNames.get(dto.getProjectManagerId()));
            }

            // i18n overlay názvů (batch)
            if (!items.isEmpty()) {
                Set<UUID> ids = page.getContent().stream().map(Project::getId).collect(Collectors.toSet());
                Map<UUID, String> trNames = translationService.batchResolveNames(ids);
                for (int i = 0; i < items.size(); i++) {
                    UUID pid = page.getContent().get(i).getId();
                    String trName = trNames.get(pid);
                    if (trName != null && !trName.isBlank()) items.get(i).setName(trName);
                }
            }

            dtoPage = new PageImpl<>(items, prev, page.getTotalElements());
        }

        return dtoPage;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto get(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project entity = findByIdAndCompany(id, companyId);

        // Kanonické hodnoty z entity (name/description)
        ProjectDto dto = mapper.toDto(entity);

        if (entity.getCustomerId() != null) {
            customerRepository.findNamesByCompanyAndIdIn(companyId, List.of(entity.getCustomerId()))
                    .stream().findFirst()
                    .ifPresent(v -> dto.setCustomerName(v.getName()));
        }
        if (entity.getProjectManagerId() != null) {
            companyMemberRepository.findMemberNamesByCompanyAndIdIn(companyId, List.of(entity.getProjectManagerId()))
                    .stream().findFirst()
                    .ifPresent(v -> dto.setProjectManagerName(v.getName()));
        }
        // lokalizované overlaye, pokud existují
        applyLocalizedOverlay(dto, id);

        return dto;
    }

    @Override
    @Transactional
    public ProjectDto create(CreateProjectRequest request) {
        UUID companyId = SecurityUtils.requireCompanyId();

        // Validace (business)
        validateDates(request.getPlannedStartDate(), request.getPlannedEndDate());

        // Mapování + povinné kontexty
        Project entity = mapper.fromCreate(request);
        entity.setCompanyId(companyId);

        // Bezpečné generování kódu (nezávislé na FE)
        String code = codeGenerator.nextCode(companyId, request.getPlannedStartDate());
        entity.setCode(code);

        // Kanonické pojmenování (minimálně name, description může být null)
        String name = (request.getName() != null && !request.getName().isBlank())
                ? request.getName().trim()
                : ("Project " + code);
        entity.setName(name);
        entity.setDescription(request.getDescription()); // může být null

        // Typed JSONB adresa stavby
        if (request.getSiteAddress() != null) {
            var addr = org.mapstruct.factory.Mappers
                    .getMapper(cz.stavbau.backend.common.mapping.AddressMapper.class)
                    .toEntity(request.getSiteAddress());
            entity.setSiteAddress(addr);
        }

        Project saved = projectRepository.save(entity);

        // Volitelně i18n overlay – pokud chceš udržovat i translation tabulku
        String locale = i18nLocale.resolve().toLanguageTag();
        if (request.getName() != null || request.getDescription() != null) {
            // zapisujeme to samé, co je v kanonických polích (nebo jen to, co přišlo)
            upsertTranslation(saved.getId(), locale, request.getName(), request.getDescription());
        }

        return mapper.toDto(saved); // detail DTO má už kanonické name/description
    }

    @Override
    @Transactional
    public ProjectDto update(UUID id, UpdateProjectRequest request) {
        UUID companyId = SecurityUtils.requireCompanyId();

        Project entity = findByIdAndCompany(id, companyId);

        // Validace data rozsahu (PATCH sémantika)
        LocalDate start = request.getPlannedStartDate() != null ? request.getPlannedStartDate() : entity.getPlannedStartDate();
        LocalDate end   = request.getPlannedEndDate()   != null ? request.getPlannedEndDate()   : entity.getPlannedEndDate();
        validateDates(start, end);

        // Kód je neměnný – obrana v hloubce, i kdyby se omylem poslal
        try {
            var f = request.getClass().getDeclaredField("code");
            f.setAccessible(true);
            if (f.get(request) != null) throw new BadRequest(messages.msg("project.code.immutable"));
        } catch (NoSuchFieldException ignore) {
            // request code pole nemá → OK
        } catch (IllegalAccessException ignore) {
            // ignore
        }

        // PATCH na entity (MapStruct IGNORE null policy)
        mapper.update(entity, request);

        // Kanonické name/description (jen pokud přišlo v requestu)
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty())  throw new InvalidException(messages.msg("project.name.required"));
            entity.setName(name);
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }

        // Typed JSONB adresa – pouze pokud přišla (jinak beze změny)
        if (request.getSiteAddress() != null) {
            var addr = org.mapstruct.factory.Mappers
                    .getMapper(cz.stavbau.backend.common.mapping.AddressMapper.class)
                    .toEntity(request.getSiteAddress());
            entity.setSiteAddress(addr);
        }

        Project saved = projectRepository.save(entity);

        // Volitelně i18n overlay update (jen když FE něco poslal)
        if (request.getName() != null || request.getDescription() != null) {
            String locale = i18nLocale.resolve().toLanguageTag();
            upsertTranslation(saved.getId(), locale, request.getName(), request.getDescription());
        }

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project entity = findByIdAndCompany(id, companyId);
        projectRepository.delete(entity); // Pokud preferuješ soft-delete, používej místo toho archive(...)
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project entity = findByIdAndCompany(id, companyId);

        if (entity.getArchivedAt() == null) {
            entity.setArchivedAt(Instant.now());
        }
        if (entity.getStatus() != ProjectStatus.ARCHIVED) {
            entity.setStatus(ProjectStatus.ARCHIVED);
        }
        projectRepository.save(entity);
    }

    @Override
    @Transactional
    public void unarchive(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project entity = findByIdAndCompany(id, companyId);

        // Vrátíme z ARCHIVED do PLANNED (nebo zvol jiný výchozí stav)
        if (entity.getArchivedAt() != null) {
            entity.setArchivedAt(null);
        }
        if (entity.getStatus() == ProjectStatus.ARCHIVED) {
            entity.setStatus(ProjectStatus.PLANNED);
        }
        projectRepository.save(entity);
    }

    @Override
    @Transactional
    public void upsertTranslation(UUID id, String locale, UpsertProjectTranslationRequest req) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project p = findByIdAndCompany(id, companyId);

        var key = new ProjectTranslationId(p.getId(), locale);
        var t = translationRepository.findById(key)
                .orElse(new ProjectTranslation(p.getId(), locale, null, null));
        if (req.getName() != null)        t.setName(req.getName());
        if (req.getDescription() != null) t.setDescription(req.getDescription());
        translationRepository.save(t);
    }

    @Override
    @Transactional
    public void deleteTranslation(UUID id, String locale) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project p = findByIdAndCompany(id, companyId);
        translationRepository.deleteById(new ProjectTranslationId(p.getId(), locale));
    }

    // ========= HELPERS =========

    private Project findByIdAndCompany(UUID id, UUID companyId) {
        return projectRepository.findById(id)
                .filter(p -> companyId.equals(p.getCompanyId()))
                .orElseThrow(() -> new NotFoundException("project.not_found"));
    }

    private void upsertTranslation(UUID projectId, String locale, String name, String description) {
        ProjectTranslationId key = new ProjectTranslationId(projectId, locale);
        ProjectTranslation t = translationRepository.findById(key)
                .orElse(new ProjectTranslation(projectId, locale, null, null));
        if (name != null) t.setName(name);
        if (description != null) t.setDescription(description);
        translationRepository.save(t);
    }

    /** Pokud existuje překlad pro aktuální locale, vyplní nameLocalized/descriptionLocalized. */
    private void applyLocalizedOverlay(ProjectDto dto, UUID projectId) {
        // pokud používáš službu nad translations:
        String locName = translationService.resolveName(projectId);           // může vracet null
        String locDesc = translationService.resolveDescription(projectId);    // může vracet null

        if (locName != null && !locName.isBlank() && !locName.equals(dto.getName())) {
            dto.setNameLocalized(locName);
        }
        if (locDesc != null && !locDesc.isBlank() && !locDesc.equals(dto.getDescription())) {
            dto.setDescriptionLocalized(locDesc);
        }
    }


    private ProjectDto enrichDto(ProjectDto dto, UUID projectId) {
        dto.setName(translationService.resolveName(projectId));
        dto.setDescription(translationService.resolveDescription(projectId));
        return dto;
    }

    private ProjectDto resolveLocalized(ProjectDto dto, UUID projectId) {
        dto.setNameLocalized( Optional.ofNullable(translationService.resolveName(projectId))
                .orElse(dto.getName()) );
        dto.setDescriptionLocalized( Optional.ofNullable(translationService.resolveDescription(projectId))
                .orElse(dto.getDescription()) );
        return dto;
    }

    private ProjectSummaryDto resolveLocalized(ProjectSummaryDto dto, UUID projectId) {
        dto.setNameLocalized( Optional.ofNullable(translationService.resolveName(projectId))
                .orElse(dto.getName()) );
        return dto;
    }
}

