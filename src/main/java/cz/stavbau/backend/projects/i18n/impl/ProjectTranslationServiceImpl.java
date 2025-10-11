package cz.stavbau.backend.projects.i18n.impl;

import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.projects.i18n.ProjectTranslationService;
import cz.stavbau.backend.projects.model.ProjectTranslation;
import cz.stavbau.backend.projects.model.ProjectTranslationId;
import cz.stavbau.backend.projects.repo.ProjectTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

// ProjectTranslationServiceImpl.java
@Service
@RequiredArgsConstructor
public class ProjectTranslationServiceImpl implements ProjectTranslationService {

    private final ProjectTranslationRepository translationRepository;
    private final I18nLocaleService i18nLocale;

    @Override
    public String resolveName(UUID projectId) {
        String locale = i18nLocale.resolve().toLanguageTag();
        return translationRepository.findById(new ProjectTranslationId(projectId, locale))
                .map(ProjectTranslation::getName)
                .orElse(null);
    }

    @Override
    public String resolveDescription(UUID projectId) {
        String locale = i18nLocale.resolve().toLanguageTag();
        return translationRepository.findById(new ProjectTranslationId(projectId, locale))
                .map(ProjectTranslation::getDescription)
                .orElse(null);
    }

    @Override
    public Map<UUID, String> batchResolveNames(Set<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) return Collections.emptyMap();

        final String locale = i18nLocale.resolve().toLanguageTag();
        Map<UUID, String> result = new HashMap<>(projectIds.size());

        // 1) preferovaný jazyk
        translationRepository.findByProjectIdInAndLocale(projectIds, locale).stream()
                .filter(tr -> tr.getName() != null && !tr.getName().isBlank())
                .forEach(tr -> result.put(tr.getProjectId(), tr.getName()));

        // 2) fallback pro chybějící projekty – vybereme libovolný neprázdný název
        Set<UUID> missing = projectIds.stream()
                .filter(id -> !result.containsKey(id))
                .collect(java.util.stream.Collectors.toSet());

        if (!missing.isEmpty()) {
            // stáhneme všechny překlady pro missing a v Javě vybereme vhodný
            Map<UUID, String> fallback = translationRepository.findByProjectIdIn(missing).stream()
                    .filter(tr -> tr.getName() != null && !tr.getName().isBlank())
                    .collect(java.util.stream.Collectors.toMap(
                            ProjectTranslation::getProjectId,
                            ProjectTranslation::getName,
                            // při kolizi (víc locale) nech první (nebo můžeš preferovat např. "cs")
                            (a, b) -> a
                    ));
            result.putAll(fallback);
        }

        return result;
    }
}
