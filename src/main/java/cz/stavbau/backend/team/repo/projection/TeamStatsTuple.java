// src/main/java/cz/stavbau/backend/team/repo/projection/TeamStatsTuple.java
package cz.stavbau.backend.team.repo.projection;

public interface TeamStatsTuple {
    Long getOwners();
    Long getActive();
    Long getInvited();
    Long getDisabled();
    Long getArchived();
    Long getTotal();
}
