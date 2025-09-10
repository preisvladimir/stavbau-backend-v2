-- V2025_09_10_001__companies_registered_address_lines.sql
-- Doplneni chybejicich radku adresy pro embedded RegisteredAddress

ALTER TABLE companies
  ADD COLUMN IF NOT EXISTS radek_adresy1 VARCHAR(255),
  ADD COLUMN IF NOT EXISTS radek_adresy2 VARCHAR(255);
