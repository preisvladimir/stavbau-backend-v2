-- V2025_09_19_002__company_member_contact_fields.sql
ALTER TABLE public.company_members
  ADD COLUMN first_name VARCHAR(100),
  ADD COLUMN last_name  VARCHAR(100),
  ADD COLUMN phone      VARCHAR(40);
