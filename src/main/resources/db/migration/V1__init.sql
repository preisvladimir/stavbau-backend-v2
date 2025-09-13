--
-- PostgreSQL database dump
--

\restrict DZCyENOa6tuE9agHKIMKnAab1oCDi14GzkGDFMKgbdCfuBlQiyDR8Y2gfWQqvom

-- Dumped from database version 16.10
-- Dumped by pg_dump version 16.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: companies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.companies (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    created_by uuid,
    updated_at timestamp(6) with time zone,
    updated_by uuid,
    radek_adresy1 character varying(255),
    radek_adresy2 character varying(255),
    ares_last_sync_at timestamp(6) with time zone,
    ares_raw jsonb,
    cz_nace_prevazujici character varying(6),
    datum_aktualizace_ares date,
    datum_vzniku date,
    financni_urad_code character varying(8),
    ico character varying(8) NOT NULL,
    institucionalni_sektor2010 character varying(16),
    kategorie_poctu_pracovniku character varying(8),
    obchodni_jmeno character varying(255),
    okres_nuts_lau character varying(16),
    pravni_forma_code character varying(8),
    cislo_domovni character varying(16),
    kod_adresniho_mista character varying(16),
    kod_casti_obce character varying(12),
    kod_kraje character varying(4),
    kod_obce character varying(12),
    kod_okresu character varying(8),
    kod_statu character varying(2),
    kod_ulice character varying(16),
    nazev_casti_obce character varying(128),
    nazev_kraje character varying(64),
    nazev_obce character varying(128),
    nazev_okresu character varying(64),
    nazev_statu character varying(64),
    nazev_ulice character varying(128),
    psc character varying(16),
    standardizace_adresy boolean,
    textova_adresa character varying(512),
    typ_cislo_domovni character varying(4),
    zakladni_uzemni_jednotka character varying(16)
);


--
-- Name: company_nace; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.company_nace (
    company_id uuid NOT NULL,
    nace_code character varying(6) NOT NULL
);


--
-- Name: file_links; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.file_links (
    file_id uuid NOT NULL,
    target_id uuid NOT NULL,
    target_type character varying(255) NOT NULL,
    CONSTRAINT file_links_target_type_check CHECK (((target_type)::text = ANY ((ARRAY['COMPANY'::character varying, 'PROJECT'::character varying, 'INVOICE'::character varying])::text[])))
);


--
-- Name: file_tag_join; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.file_tag_join (
    file_id uuid NOT NULL,
    tag_id uuid NOT NULL
);


--
-- Name: file_tags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.file_tags (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    name character varying(64) NOT NULL
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: invoice_lines; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoice_lines (
    id uuid NOT NULL,
    invoice_id uuid NOT NULL,
    item_name character varying(256) NOT NULL,
    line_total numeric(18,2) NOT NULL,
    quantity numeric(18,3) NOT NULL,
    unit character varying(32) NOT NULL,
    unit_price numeric(18,2) NOT NULL,
    vat_rate numeric(5,2) NOT NULL
);


--
-- Name: invoices; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoices (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    currency character varying(3) NOT NULL,
    customer_json jsonb NOT NULL,
    due_date date NOT NULL,
    issue_date date NOT NULL,
    notes oid,
    number character varying(255),
    project_id uuid,
    status character varying(16) NOT NULL,
    subtotal numeric(18,2) NOT NULL,
    supplier_json jsonb NOT NULL,
    tax_date date,
    total numeric(18,2) NOT NULL,
    vat_mode character varying(255) NOT NULL,
    vat_total numeric(18,2) NOT NULL,
    CONSTRAINT invoices_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'ISSUED'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT invoices_vat_mode_check CHECK (((vat_mode)::text = ANY ((ARRAY['NONE'::character varying, 'STANDARD'::character varying])::text[])))
);


--
-- Name: number_series; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.number_series (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    counter_value integer NOT NULL,
    counter_year integer NOT NULL,
    is_default boolean NOT NULL,
    key character varying(32) NOT NULL,
    pattern character varying(64) NOT NULL,
    version integer NOT NULL
);


--
-- Name: stored_files; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stored_files (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    mime_type character varying(255) NOT NULL,
    original_name character varying(255) NOT NULL,
    sha256 character varying(64) NOT NULL,
    size_bytes bigint NOT NULL,
    storage_key character varying(512) NOT NULL,
    uploader_id uuid NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    created_by uuid,
    updated_at timestamp(6) with time zone,
    updated_by uuid,
    company_id uuid NOT NULL,
    email character varying(320) NOT NULL,
    locale character varying(10),
    password_hash character varying(255) NOT NULL,
    refresh_token_id uuid,
    token_version integer NOT NULL
);


--
-- Name: companies companies_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_pkey PRIMARY KEY (id);


--
-- Name: company_nace company_nace_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.company_nace
    ADD CONSTRAINT company_nace_pkey PRIMARY KEY (company_id, nace_code);


--
-- Name: file_links file_links_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_links
    ADD CONSTRAINT file_links_pkey PRIMARY KEY (file_id, target_id, target_type);


--
-- Name: file_tag_join file_tag_join_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_tag_join
    ADD CONSTRAINT file_tag_join_pkey PRIMARY KEY (file_id, tag_id);


--
-- Name: file_tags file_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_tags
    ADD CONSTRAINT file_tags_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: invoice_lines invoice_lines_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoice_lines
    ADD CONSTRAINT invoice_lines_pkey PRIMARY KEY (id);


--
-- Name: invoices invoices_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_pkey PRIMARY KEY (id);


--
-- Name: number_series number_series_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.number_series
    ADD CONSTRAINT number_series_pkey PRIMARY KEY (id);


--
-- Name: stored_files stored_files_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stored_files
    ADD CONSTRAINT stored_files_pkey PRIMARY KEY (id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: companies ux_company_ico; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT ux_company_ico UNIQUE (ico);


--
-- Name: file_tags ux_file_tags_company_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_tags
    ADD CONSTRAINT ux_file_tags_company_name UNIQUE (company_id, name);


--
-- Name: number_series ux_number_series_company_year_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.number_series
    ADD CONSTRAINT ux_number_series_company_year_key UNIQUE (company_id, counter_year, key);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: ix_company_nuts; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_company_nuts ON public.companies USING btree (okres_nuts_lau);


--
-- Name: company_nace fk4ujxyrmd05of02ih46ql46wrw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.company_nace
    ADD CONSTRAINT fk4ujxyrmd05of02ih46ql46wrw FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- PostgreSQL database dump complete
--

\unrestrict DZCyENOa6tuE9agHKIMKnAab1oCDi14GzkGDFMKgbdCfuBlQiyDR8Y2gfWQqvom

