package cz.stavbau.backend.common.domain;

public enum AddressSource {
    USER,     // ruční zadání
    ARES,     // státní rejstřík
    GEO,      // geokodér (Mapy.cz apod.)
    IMPORT    // import z externího systému
}
