-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.users (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  full_name character varying NOT NULL CHECK (length(TRIM(BOTH FROM full_name)) >= 3),
  email character varying NOT NULL UNIQUE,
  phone character varying,
  alternative_phone character varying,
  birth_date date,
  profile_photo text,
  role character varying NOT NULL DEFAULT 'USER'::character varying CHECK (role::text = ANY (ARRAY['USER'::character varying, 'ADMIN'::character varying]::text[])),
  verified boolean NOT NULL DEFAULT false,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  profile_picture_url text,
  failed_attempts integer NOT NULL DEFAULT 0,
  blocked boolean NOT NULL DEFAULT false,
  password_hash text NOT NULL DEFAULT ''::text,
  CONSTRAINT users_pkey PRIMARY KEY (id)
);
CREATE TABLE public.user_credentials (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL UNIQUE,
  password_hash text NOT NULL,
  failed_attempts integer NOT NULL DEFAULT 0 CHECK (failed_attempts >= 0),
  account_locked boolean NOT NULL DEFAULT false,
  locked_until timestamp without time zone,
  last_login timestamp without time zone,
  last_password_change timestamp without time zone NOT NULL DEFAULT now(),
  password_expires_at timestamp without time zone,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT user_credentials_pkey PRIMARY KEY (id),
  CONSTRAINT fk_credentials_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.password_history (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  credential_id uuid,
  password_hash text NOT NULL,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  user_id uuid,
  active boolean NOT NULL DEFAULT true,
  expired_at timestamp without time zone,
  CONSTRAINT password_history_pkey PRIMARY KEY (id),
  CONSTRAINT fk_password_history FOREIGN KEY (credential_id) REFERENCES public.user_credentials(id),
  CONSTRAINT password_history_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.user_settings (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL UNIQUE,
  theme character varying NOT NULL DEFAULT 'SYSTEM'::character varying CHECK (theme::text = ANY (ARRAY['SYSTEM'::character varying, 'LIGHT'::character varying, 'DARK'::character varying]::text[])),
  language character varying NOT NULL DEFAULT 'es'::character varying CHECK (language::text = ANY (ARRAY['es'::character varying, 'en'::character varying]::text[])),
  currency character varying NOT NULL DEFAULT 'ARS'::character varying,
  notifications_enabled boolean NOT NULL DEFAULT true,
  push_enabled boolean NOT NULL DEFAULT true,
  email_enabled boolean NOT NULL DEFAULT false,
  budget_notifications boolean NOT NULL DEFAULT true,
  shopping_notifications boolean NOT NULL DEFAULT true,
  invitation_notifications boolean NOT NULL DEFAULT true,
  ai_notifications boolean NOT NULL DEFAULT true,
  biometric_login boolean NOT NULL DEFAULT false,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT user_settings_pkey PRIMARY KEY (id),
  CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.login_history (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  login_date timestamp without time zone NOT NULL DEFAULT now(),
  ip_address character varying,
  device character varying,
  success boolean NOT NULL,
  reason character varying,
  user_agent text,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT login_history_pkey PRIMARY KEY (id),
  CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.groups (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  owner_id uuid NOT NULL,
  name character varying NOT NULL,
  description text,
  type character varying NOT NULL DEFAULT 'FAMILIA'::character varying CHECK (type::text = ANY (ARRAY['PERSONAL'::character varying, 'FAMILY'::character varying, 'WORK'::character varying, 'FRIENDS'::character varying, 'OTHER'::character varying]::text[])),
  is_personal boolean NOT NULL DEFAULT false,
  cycle_day integer NOT NULL DEFAULT 1 CHECK (cycle_day >= 1 AND cycle_day <= 28),
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  categoria character varying NOT NULL DEFAULT 'FAMILIA'::character varying,
  created_by uuid,
  CONSTRAINT groups_pkey PRIMARY KEY (id),
  CONSTRAINT fk_group_owner FOREIGN KEY (owner_id) REFERENCES public.users(id)
);
CREATE TABLE public.group_members (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL,
  user_id uuid NOT NULL,
  role character varying NOT NULL DEFAULT 'MEMBER'::character varying CHECK (role::text = ANY (ARRAY['OWNER'::character varying, 'ADMIN'::character varying, 'MEMBER'::character varying]::text[])),
  joined_at timestamp without time zone NOT NULL DEFAULT now(),
  active boolean NOT NULL DEFAULT true,
  left_at timestamp without time zone,
  CONSTRAINT group_members_pkey PRIMARY KEY (id),
  CONSTRAINT fk_group_member_group FOREIGN KEY (group_id) REFERENCES public.groups(id),
  CONSTRAINT fk_group_member_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.group_invitations (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL,
  invited_user_id uuid NOT NULL,
  invited_by uuid NOT NULL,
  status character varying NOT NULL DEFAULT 'PENDING'::character varying CHECK (status::text = ANY (ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying]::text[])),
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  expires_at timestamp without time zone,
  accepted_at timestamp without time zone,
  invited_email character varying,
  token text NOT NULL DEFAULT (gen_random_uuid())::text UNIQUE,
  CONSTRAINT group_invitations_pkey PRIMARY KEY (id),
  CONSTRAINT fk_invitation_group FOREIGN KEY (group_id) REFERENCES public.groups(id),
  CONSTRAINT fk_invitation_user FOREIGN KEY (invited_user_id) REFERENCES public.users(id),
  CONSTRAINT fk_invitation_sender FOREIGN KEY (invited_by) REFERENCES public.users(id)
);
CREATE TABLE public.group_settings (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL UNIQUE,
  currency character varying NOT NULL DEFAULT 'ARS'::character varying,
  notifications_enabled boolean NOT NULL DEFAULT true,
  ai_enabled boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT group_settings_pkey PRIMARY KEY (id),
  CONSTRAINT fk_group_settings FOREIGN KEY (group_id) REFERENCES public.groups(id)
);
CREATE TABLE public.categories (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  name character varying NOT NULL UNIQUE,
  description text,
  icon character varying,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT categories_pkey PRIMARY KEY (id)
);
CREATE TABLE public.products (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  barcode character varying,
  name character varying NOT NULL,
  brand character varying,
  description text,
  default_unit character varying CHECK (default_unit::text = ANY (ARRAY['UNIT'::character varying, 'KG'::character varying, 'G'::character varying, 'L'::character varying, 'ML'::character varying, 'PACK'::character varying]::text[])),
  category_id uuid,
  image_url text,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  priority character varying NOT NULL DEFAULT 'SECUNDARIO'::character varying,
  price numeric NOT NULL DEFAULT 0,
  CONSTRAINT products_pkey PRIMARY KEY (id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES public.categories(id)
);
CREATE TABLE public.product_price_history (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  product_id uuid NOT NULL,
  store_id uuid,
  price numeric NOT NULL CHECK (price >= 0::numeric),
  observed_at timestamp without time zone NOT NULL DEFAULT now(),
  source character varying CHECK (source::text = ANY (ARRAY['TICKET'::character varying, 'USER'::character varying, 'AI'::character varying, 'OTHER'::character varying]::text[])),
  CONSTRAINT product_price_history_pkey PRIMARY KEY (id),
  CONSTRAINT fk_price_product FOREIGN KEY (product_id) REFERENCES public.products(id),
  CONSTRAINT fk_price_store FOREIGN KEY (store_id) REFERENCES public.stores(id)
);
CREATE TABLE public.stores (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  name character varying NOT NULL,
  address text,
  phone character varying,
  latitude double precision,
  longitude double precision,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT stores_pkey PRIMARY KEY (id)
);
CREATE TABLE public.offers (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  store_id uuid,
  title character varying NOT NULL,
  description text,
  discount_type character varying NOT NULL CHECK (discount_type::text = ANY (ARRAY['PERCENTAGE'::character varying, 'FIXED'::character varying, 'SECOND_UNIT'::character varying, 'OTHER'::character varying]::text[])),
  discount_value numeric NOT NULL CHECK (discount_value >= 0::numeric),
  start_date timestamp without time zone NOT NULL,
  end_date timestamp without time zone NOT NULL,
  image_url text,
  terms_conditions text,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT offers_pkey PRIMARY KEY (id),
  CONSTRAINT fk_offer_store FOREIGN KEY (store_id) REFERENCES public.stores(id)
);
CREATE TABLE public.shopping_lists (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL,
  created_by uuid NOT NULL,
  name character varying NOT NULL,
  description text,
  keep_between_periods boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT shopping_lists_pkey PRIMARY KEY (id),
  CONSTRAINT fk_list_group FOREIGN KEY (group_id) REFERENCES public.groups(id),
  CONSTRAINT fk_list_user FOREIGN KEY (created_by) REFERENCES public.users(id)
);
CREATE TABLE public.shopping_list_items (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  shopping_list_id uuid NOT NULL,
  product_id uuid,
  custom_product_name character varying,
  estimated_price numeric CHECK (estimated_price >= 0::numeric),
  estimated_quantity numeric NOT NULL DEFAULT 1 CHECK (estimated_quantity > 0::numeric),
  estimated_brand character varying,
  unit character varying CHECK (unit::text = ANY (ARRAY['UNIT'::character varying, 'KG'::character varying, 'G'::character varying, 'L'::character varying, 'ML'::character varying, 'PACK'::character varying]::text[])),
  priority character varying NOT NULL DEFAULT 'PRIMARY'::character varying CHECK (priority::text = ANY (ARRAY['ESSENTIAL'::character varying, 'PRIMARY'::character varying, 'SECONDARY'::character varying]::text[])),
  checked boolean NOT NULL DEFAULT false,
  purchased_quantity numeric CHECK (purchased_quantity >= 0::numeric),
  notes text,
  created_by uuid,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  last_checked_at timestamp without time zone,
  CONSTRAINT shopping_list_items_pkey PRIMARY KEY (id),
  CONSTRAINT fk_item_list FOREIGN KEY (shopping_list_id) REFERENCES public.shopping_lists(id),
  CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES public.products(id),
  CONSTRAINT fk_item_user FOREIGN KEY (created_by) REFERENCES public.users(id)
);
CREATE TABLE public.ticket_products (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  ticket_id uuid NOT NULL,
  product_id uuid,
  detected_name character varying,
  quantity numeric CHECK (quantity IS NULL OR quantity > 0::numeric),
  price numeric CHECK (price IS NULL OR price >= 0::numeric),
  brand character varying,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT ticket_products_pkey PRIMARY KEY (id),
  CONSTRAINT fk_ticket_products_product FOREIGN KEY (product_id) REFERENCES public.products(id)
);
CREATE TABLE public.ticket_analysis (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  ticket_id uuid NOT NULL UNIQUE,
  ocr_text text,
  summary text,
  status character varying NOT NULL DEFAULT 'PENDING'::character varying CHECK (status::text = ANY (ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'PROCESSED'::character varying, 'ERROR'::character varying]::text[])),
  confidence numeric CHECK (confidence IS NULL OR confidence >= 0::numeric AND confidence <= 1::numeric),
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  processed_at timestamp without time zone,
  CONSTRAINT ticket_analysis_pkey PRIMARY KEY (id)
);
CREATE TABLE public.notifications (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  group_id uuid,
  title character varying NOT NULL,
  message text NOT NULL,
  type character varying NOT NULL DEFAULT 'INFO'::character varying CHECK (type::text = ANY (ARRAY['INFO'::character varying, 'WARNING'::character varying, 'SUCCESS'::character varying, 'ERROR'::character varying, 'INVITATION'::character varying, 'BUDGET'::character varying, 'SHOPPING'::character varying, 'AI'::character varying]::text[])),
  read boolean NOT NULL DEFAULT false,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  read_at timestamp without time zone,
  active boolean NOT NULL DEFAULT true,
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  data text,
  description text,
  CONSTRAINT notifications_pkey PRIMARY KEY (id),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES public.users(id),
  CONSTRAINT fk_notification_group FOREIGN KEY (group_id) REFERENCES public.groups(id)
);
CREATE TABLE public.ai_actions (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  ticket_id uuid,
  shopping_list_item_id uuid,
  action_type character varying NOT NULL CHECK (action_type::text = ANY (ARRAY['UPDATE_PRICE'::character varying, 'UPDATE_BRAND'::character varying, 'MARK_PURCHASED'::character varying, 'CREATE_PRODUCT'::character varying, 'MERGE_PRODUCT'::character varying, 'OTHER'::character varying]::text[])),
  old_value jsonb,
  new_value jsonb,
  accepted boolean,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT ai_actions_pkey PRIMARY KEY (id),
  CONSTRAINT fk_ai_action_item FOREIGN KEY (shopping_list_item_id) REFERENCES public.shopping_list_items(id)
);
CREATE TABLE public.refresh_tokens (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  token text NOT NULL,
  expires_at timestamp without time zone NOT NULL,
  used boolean NOT NULL DEFAULT false,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id),
  CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.audit_logs (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid,
  entity character varying,
  entity_id uuid,
  action character varying,
  old_data jsonb,
  new_data jsonb,
  ip_address character varying,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT audit_logs_pkey PRIMARY KEY (id),
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.flyway_schema_history (
  installed_rank integer NOT NULL,
  version character varying,
  description character varying NOT NULL,
  type character varying NOT NULL,
  script character varying NOT NULL,
  checksum integer,
  installed_by character varying NOT NULL,
  installed_on timestamp without time zone NOT NULL DEFAULT now(),
  execution_time integer NOT NULL,
  success boolean NOT NULL,
  CONSTRAINT flyway_schema_history_pkey PRIMARY KEY (installed_rank)
);
CREATE TABLE public.budgets (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL,
  start_date date NOT NULL,
  end_date date NOT NULL,
  total numeric NOT NULL,
  created_by uuid,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT budgets_pkey PRIMARY KEY (id),
  CONSTRAINT budgets_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id),
  CONSTRAINT budgets_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id)
);
CREATE TABLE public.tickets (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL,
  uploaded_by uuid NOT NULL,
  supermarket_name character varying NOT NULL,
  amount numeric NOT NULL,
  movement_type character varying CHECK (movement_type::text = ANY (ARRAY['EXPENSE'::character varying, 'INCOME'::character varying]::text[])),
  purchase_date timestamp without time zone NOT NULL,
  image_url text,
  comment text,
  status character varying NOT NULL DEFAULT 'PENDING'::character varying CHECK (status::text = ANY (ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'PROCESSED'::character varying, 'ERROR'::character varying]::text[])),
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT tickets_pkey PRIMARY KEY (id),
  CONSTRAINT tickets_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id),
  CONSTRAINT tickets_uploaded_by_fkey FOREIGN KEY (uploaded_by) REFERENCES public.users(id)
);
CREATE TABLE public.notification_settings (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL UNIQUE,
  push_enabled boolean NOT NULL DEFAULT true,
  email_enabled boolean NOT NULL DEFAULT false,
  budget_notifications boolean NOT NULL DEFAULT true,
  shopping_notifications boolean NOT NULL DEFAULT true,
  invitation_notifications boolean NOT NULL DEFAULT true,
  ai_notifications boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT notification_settings_pkey PRIMARY KEY (id),
  CONSTRAINT notification_settings_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
