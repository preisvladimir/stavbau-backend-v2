package cz.stavbau.backend.invoices.model;
/**
 * Jednoduchý režim DPH pro MVP.
 */
public enum VatMode {
    NONE, // bez DPH
    STANDARD // standardní sazby na řádcích (0/10/12/21) – výpočty po řádcích
}
