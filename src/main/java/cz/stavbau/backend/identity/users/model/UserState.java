package cz.stavbau.backend.identity.users.model;

/** Stav uživatele pro invite/aktivaci/blokaci (MVP). */
public enum UserState {
    INVITED,
    CREATED,
    ACTIVE,
    DISABLED,
    LOCKED
}
