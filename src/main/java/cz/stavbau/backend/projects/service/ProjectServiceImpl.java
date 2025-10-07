package cz.stavbau.backend.projects.service;

import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.projects.dto.*;
import cz.stavbau.backend.projects.filter.ProjectFilter;
import cz.stavbau.backend.projects.mapper.ProjectMapper;
import cz.stavbau.backend.projects.model.*;
import cz.stavbau.backend.projects.repo.*;
import cz.stavbau.backend.projects.persistence.ProjectSpecification;
import cz.stavbau.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectTranslationRepository translationRepository;
    private final ProjectMapper mapper;
    private final I18nLocaleService i18nLocale;
    private final ProjectCodeGenerator codeGenerator;

    @Override
    @Transactional
    public ProjectDto create(CreateProjectRequest request) {
        UUID companyId = SecurityUtils.requireCompanyId();

        validateDates(request.getPlannedStartDate(), request.getPlannedEndDate());

        Project entity = mapper.fromCreate(request);
        entity.setCompanyId(companyId);

        // FE code neposílá – generujeme vždy (bezpečné vůči souběhu)
        String code = codeGenerator.nextCode(companyId, request.getPlannedStartDate());
        entity.setCode(code);

        // Adresa stavby (typed JSONB), stejně jako u Customers.billingAddress
        if (request.getSiteAddress() != null) {
            var addr = org.mapstruct.factory.Mappers
                    .getMapper(cz.stavbau.backend.common.mapping.AddressMapper.class)
                    .toEntity(request.getSiteAddress());
            entity.setSiteAddress(addr);
        }
        Project saved = projectRepository.save(entity);

        // ulož i18n překlad (min 1 jazyk = resolved)
        String locale = i18nLocale.resolve().toLanguageTag();
        upsertTranslation(saved.getId(), locale, nullToEmpty(request.getName()), nullToEmpty(request.getDescription()));

        return enrichDto(mapper.toDto(saved), saved.getId());
    }

    @Override
    @Transactional
    public ProjectDto update(UUID id, UpdateProjectRequest request) {
        UUID companyId = SecurityUtils.requireCompanyId();

        Project entity = findByIdAndCompany(id, companyId);
        if (request.getPlannedStartDate() != null || request.getPlannedEndDate() != null) {
            validateDates(request.getPlannedStartDate(), request.getPlannedEndDate());
        }

        // Kód je neměnný – pokud by FE omylem poslal, odmítneme
        if (request.getClass().getDeclaredFields() != null) {
            // bezpečná obrana i když jsme code z requestu odstranili
            try {
                var f = request.getClass().getDeclaredField("code");
                f.setAccessible(true);
                Object attempted = f.get(request);
                if (attempted != null) {
                    throw badRequest("project.code.immutable");
                }
            } catch (NoSuchFieldException ignored) { /* pole neexistuje → OK */ }
            catch (IllegalAccessException ignored) { /* ignore */ }
        }
        mapper.update(entity, request);
        // PATCH sémantika: pokud přišla adresa, přepiš; pokud nepřišla, ponech stávající
        if (request.getSiteAddress() != null) {
            var addr = org.mapstruct.factory.Mappers
                    .getMapper(cz.stavbau.backend.common.mapping.AddressMapper.class)
                    .toEntity(request.getSiteAddress());
            entity.setSiteAddress(addr);
        }
        Project saved = projectRepository.save(entity);

        // i18n update pokud přišlo name/description
        String locale = i18nLocale.resolve().toLanguageTag();
        if (request.getName() != null || request.getDescription() != null) {
            upsertTranslation(saved.getId(), locale, request.getName(), request.getDescription());
        }

        return enrichDto(mapper.toDto(saved), saved.getId());
    }

    @Override
    @Transactional
    public ProjectDto get(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project entity = findByIdAndCompany(id, companyId);
        return enrichDto(mapper.toDto(entity), id);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ProjectSummaryDto> list(String q, Pageable pageable) {
        ProjectFilter f = new ProjectFilter();
        f.setQ(q);
        var locale = i18nLocale.resolve(); // java.util.Locale
        return list(f, pageable,locale);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ProjectSummaryDto> list(String q, Pageable pageable,Locale locale) {
        ProjectFilter f = new ProjectFilter();
        f.setQ(q);
        return list(f, pageable,locale);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ProjectSummaryDto> list(ProjectFilter filter, Pageable pageable) {
        var locale = i18nLocale.resolve(); // java.util.Locale
        return list(filter, pageable,locale);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ProjectSummaryDto> list(ProjectFilter filter, Pageable pageable, Locale locale) {
        UUID companyId = SecurityUtils.requireCompanyId();
        var spec = new ProjectSpecification(companyId, filter, locale);
        Page<Project> page = projectRepository.findAll(spec, pageable);

        List<ProjectSummaryDto> items = mapper.toSummaryList(page.getContent());
        // doplníme resolved name + statusLabel (zachováme stávající chování)
        for (int i = 0; i < items.size(); i++) {
            Project p = page.getContent().get(i);
            ProjectSummaryDto dto = items.get(i);
            dto.setName(resolveName(p.getId()));     // existující resolver
            dto.setStatusLabel(p.getStatus().name()); // TODO: EnumLabeler → lokalizace
        }
        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project entity = findByIdAndCompany(id, companyId);
        projectRepository.delete(entity); // v PR 3/4 nahradíme za archive
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Project entity = findByIdAndCompany(id, companyId);
        entity.setArchivedAt(Instant.now());
        if (entity.getStatus() != ProjectStatus.ARCHIVED) {
            entity.setStatus(ProjectStatus.ARCHIVED);
        }
        projectRepository.save(entity);
    }

    // ===== helpers =====

    private Project findByIdAndCompany(UUID id, UUID companyId) {
        return projectRepository.findById(id)
                .filter(p -> companyId.equals(p.getCompanyId()))
                .orElseThrow(() -> notFound("project.not_found"));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw invalid("project.dates.invalid_range");
        }
    }

    private void upsertTranslation(UUID projectId, String locale, String name, String description) {
        ProjectTranslationId key = new ProjectTranslationId(projectId, locale);
        ProjectTranslation t = translationRepository.findById(key).orElse(new ProjectTranslation(projectId, locale, null, null));
        if (name != null) t.setName(name);
        if (description != null) t.setDescription(description);
        translationRepository.save(t);
    }

    private ProjectDto enrichDto(ProjectDto dto, UUID projectId) {
        dto.setName(resolveName(projectId));
        dto.setDescription(resolveDescription(projectId));
        dto.setStatusLabel(dto.getStatus() != null ? dto.getStatus().name() : null); // TODO EnumLabeler
        return dto;
    }

    private String resolveName(UUID projectId) {
        String locale = i18nLocale.resolve().toLanguageTag();
        // fallback chain: resolved -> company default -> app default
        return translationRepository.findById(new ProjectTranslationId(projectId, locale))
                .map(ProjectTranslation::getName)
                .orElseGet(() -> translationRepository.findByProjectId(projectId).stream()
                        .findFirst().map(ProjectTranslation::getName).orElse(null));
    }

    private String resolveDescription(UUID projectId) {
        String locale = i18nLocale.resolve().toLanguageTag();
        return translationRepository.findById(new ProjectTranslationId(projectId, locale))
                .map(ProjectTranslation::getDescription)
                .orElse(null);
    }

    private static String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private RuntimeException notFound(String code) { return new IllegalArgumentException(code); }
    private RuntimeException conflict(String code) { return new IllegalStateException(code); }
    private RuntimeException invalid(String code)  { return new IllegalArgumentException(code); }
    private RuntimeException badRequest(String code)  { return new IllegalArgumentException(code); }
}
