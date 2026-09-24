-- V1: baseline schema. Snapshot of the Hibernate-managed schema as of 2026-09-24.
-- From here on the schema evolves only through new V__ migrations, never by hand.

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255) NOT NULL,
    login character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_login UNIQUE (login)
);

CREATE TABLE public.workspace (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    name character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT workspace_pkey PRIMARY KEY (id)
);

CREATE TABLE public.workspace_member (
    id uuid NOT NULL,
    role character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    workspace_id uuid NOT NULL,
    CONSTRAINT workspace_member_pkey PRIMARY KEY (id),
    CONSTRAINT uk_workspace_member_user UNIQUE (workspace_id, user_id),
    CONSTRAINT workspace_member_role_check CHECK (((role)::text = ANY ((ARRAY['OWNER'::character varying, 'MEMBER'::character varying])::text[]))),
    CONSTRAINT fk_workspace_member_user FOREIGN KEY (user_id) REFERENCES public.users(id),
    CONSTRAINT fk_workspace_member_workspace FOREIGN KEY (workspace_id) REFERENCES public.workspace(id)
);

CREATE TABLE public.category (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    workspace_id uuid NOT NULL,
    CONSTRAINT category_pkey PRIMARY KEY (id),
    CONSTRAINT category_type_check CHECK (((type)::text = ANY ((ARRAY['INCOME'::character varying, 'EXPENSE'::character varying])::text[]))),
    CONSTRAINT fk_category_workspace FOREIGN KEY (workspace_id) REFERENCES public.workspace(id)
);

CREATE TABLE public.wallet (
    id uuid NOT NULL,
    currency character varying(3) NOT NULL,
    initial_balance numeric(19,2) NOT NULL,
    name character varying(255) NOT NULL,
    owner_id uuid NOT NULL,
    CONSTRAINT wallet_pkey PRIMARY KEY (id),
    CONSTRAINT fk_wallet_owner FOREIGN KEY (owner_id) REFERENCES public.users(id)
);

CREATE TABLE public.workspace_wallet (
    id uuid NOT NULL,
    wallet_id uuid NOT NULL,
    workspace_id uuid NOT NULL,
    CONSTRAINT workspace_wallet_pkey PRIMARY KEY (id),
    CONSTRAINT uk_workspace_wallet UNIQUE (workspace_id, wallet_id),
    CONSTRAINT fk_workspace_wallet_wallet FOREIGN KEY (wallet_id) REFERENCES public.wallet(id),
    CONSTRAINT fk_workspace_wallet_workspace FOREIGN KEY (workspace_id) REFERENCES public.workspace(id)
);

CREATE TABLE public.refresh_session (
    id uuid NOT NULL,
    create_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    family_id uuid NOT NULL,
    replaced_by uuid,
    revoked_at timestamp(6) with time zone,
    user_id uuid NOT NULL,
    CONSTRAINT refresh_session_pkey PRIMARY KEY (id),
    CONSTRAINT fk_refresh_session_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.transaction (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    comment character varying(1000),
    created_at timestamp(6) without time zone NOT NULL,
    occurred_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    category_id uuid NOT NULL,
    created_by uuid NOT NULL,
    wallet_id uuid NOT NULL,
    workspace_id uuid NOT NULL,
    CONSTRAINT transaction_pkey PRIMARY KEY (id),
    CONSTRAINT fk_transaction_workspace FOREIGN KEY (workspace_id) REFERENCES public.workspace(id),
    CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES public.wallet(id),
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES public.category(id),
    CONSTRAINT fk_transaction_created_by FOREIGN KEY (created_by) REFERENCES public.users(id)
);

CREATE TABLE public.transfer (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    comment character varying(1000),
    created_at timestamp(6) without time zone NOT NULL,
    occurred_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    created_by uuid NOT NULL,
    from_wallet_id uuid NOT NULL,
    to_wallet_id uuid NOT NULL,
    workspace_id uuid NOT NULL,
    CONSTRAINT transfer_pkey PRIMARY KEY (id),
    CONSTRAINT fk_transfer_workspace FOREIGN KEY (workspace_id) REFERENCES public.workspace(id),
    CONSTRAINT fk_transfer_from_wallet FOREIGN KEY (from_wallet_id) REFERENCES public.wallet(id),
    CONSTRAINT fk_transfer_to_wallet FOREIGN KEY (to_wallet_id) REFERENCES public.wallet(id),
    CONSTRAINT fk_transfer_created_by FOREIGN KEY (created_by) REFERENCES public.users(id)
);

CREATE TABLE public.budget (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    period character varying(7) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    category_id uuid NOT NULL,
    workspace_id uuid NOT NULL,
    CONSTRAINT budget_pkey PRIMARY KEY (id),
    CONSTRAINT uk_budget_workspace_category_period UNIQUE (workspace_id, category_id, period),
    CONSTRAINT fk_budget_workspace FOREIGN KEY (workspace_id) REFERENCES public.workspace(id),
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES public.category(id)
);
