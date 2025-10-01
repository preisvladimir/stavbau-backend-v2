// src/main/java/cz/stavbau/backend/team/repo/projection/MembersStatsTuple.java
package cz.stavbau.backend.team.repo.projection;

public interface MembersStatsTuple {
    Long getOwners();
    Long getActive();
    Long getInvited();
    Long getDisabled();
    Long getTotal();
}
