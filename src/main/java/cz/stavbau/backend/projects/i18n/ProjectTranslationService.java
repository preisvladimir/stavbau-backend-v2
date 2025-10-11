// cz.stavbau.backend.projects.i18n.ProjectTranslationService
package cz.stavbau.backend.projects.i18n;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ProjectTranslationService {
    String resolveName(UUID projectId);
    String resolveDescription(UUID projectId);

    /** Hromadně vrátí mapu projectId -> name s preferovaným locale a rozumným fallbackem. */
    Map<UUID, String> batchResolveNames(Set<UUID> projectIds);
}
