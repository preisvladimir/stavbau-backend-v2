// src/main/java/cz/stavbau/backend/team/repo/projection/MembersStatsProjection.java
package cz.stavbau.backend.features.members.repo.projection;

public interface MembersStatsProjection {
    Long getOwners();
    Long getActive();
    Long getInvited();
    Long getDisabled();
    Long getArchived();
    Long getTotal();
}
