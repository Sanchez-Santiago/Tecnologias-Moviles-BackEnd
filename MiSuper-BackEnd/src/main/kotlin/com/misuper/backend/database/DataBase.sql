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
  role USER-DEFINED NOT NULL DEFAULT 'USER'::user_role,
  verified boolean NOT NULL DEFAULT false,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
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
  credential_id uuid NOT NULL,
  password_hash text NOT NULL,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT password_history_pkey PRIMARY KEY (id),
  CONSTRAINT fk_password_history FOREIGN KEY (credential_id) REFERENCES public.user_credentials(id)
);
CREATE TABLE public.user_settings (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL UNIQUE,
  theme USER-DEFINED NOT NULL DEFAULT 'SYSTEM'::theme_mode,
  language USER-DEFINED NOT NULL DEFAULT 'es'::language_code,
  notifications_enabled boolean NOT NULL DEFAULT true,
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
  CONSTRAINT login_history_pkey PRIMARY KEY (id),
  CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.groups (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  owner_id uuid NOT NULL,
  name character varying NOT NULL,
  description text,
  type character varying NOT NULL CHECK (type::text = ANY (ARRAY['PERSONAL'::character varying, 'FAMILY'::character varying, 'WORK'::character varying, 'FRIENDS'::character varying, 'OTHER'::character varying]::text[])),
  is_personal boolean NOT NULL DEFAULT false,
  cycle_day integer NOT NULL DEFAULT 1 CHECK (cycle_day >= 1 AND cycle_day <= 28),
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
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
  allow_member_invite boolean NOT NULL DEFAULT false,
  allow_member_delete boolean NOT NULL DEFAULT false,
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
  active boolean NOT NULL DEFAULT true,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT products_pkey PRIMARY KEY (id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES public.categories(id)
);
CREATE TABLE public.shopping_lists (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL,
  created_by uuid NOT NULL,
  name character varying NOT NULL,
  description text,

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
  estimated_quantity numeric DEFAULT 1 CHECK (estimated_quantity > 0::numeric),
  estimated_brand character varying,
  unit character varying CHECK (unit::text = ANY (ARRAY['UNIT'::character varying, 'KG'::character varying, 'G'::character varying, 'L'::character varying, 'ML'::character varying, 'PACK'::character varying]::text[])),
  priority character varying DEFAULT 'PRIMARY'::character varying CHECK (priority::text = ANY (ARRAY['ESSENTIAL'::character varying, 'PRIMARY'::character varying, 'SECONDARY'::character varying]::text[])),
  checked boolean NOT NULL DEFAULT false,
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
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  updated_at timestamp without time zone NOT NULL DEFAULT now(),
  status character varying NOT NULL DEFAULT 'PENDING'::character varying CHECK (status::text = ANY (ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'PROCESSED'::character varying, 'ERROR'::character varying]::text[])),
  CONSTRAINT tickets_pkey PRIMARY KEY (id),
  CONSTRAINT tickets_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id),
  CONSTRAINT tickets_uploaded_by_fkey FOREIGN KEY (uploaded_by) REFERENCES public.users(id)
);
CREATE TABLE public.ticket_products (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  ticket_id uuid NOT NULL,
  product_id uuid,
  detected_name character varying,
  quantity numeric,
  price numeric,
  brand character varying,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT ticket_products_pkey PRIMARY KEY (id),
  CONSTRAINT ticket_products_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id),
  CONSTRAINT ticket_products_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES public.tickets(id)
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
CREATE TABLE public.notifications (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  group_id uuid,
  title character varying NOT NULL,
  description text NOT NULL,
  type character varying CHECK (type::text = ANY (ARRAY['INFO'::character varying, 'WARNING'::character varying, 'SUCCESS'::character varying, 'ERROR'::character varying, 'INVITATION'::character varying, 'BUDGET'::character varying, 'SHOPPING'::character varying, 'AI'::character varying]::text[])),
  read boolean NOT NULL DEFAULT false,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  read_at timestamp without time zone,
  CONSTRAINT notifications_pkey PRIMARY KEY (id),
  CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id),
  CONSTRAINT notifications_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id)
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
CREATE TABLE public.ticket_analysis (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  ticket_id uuid NOT NULL UNIQUE,
  ocr_text text,
  summary text,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  status character varying NOT NULL DEFAULT 'PENDING'::character varying CHECK (status::text = ANY (ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'PROCESSED'::character varying, 'ERROR'::character varying]::text[])),
  processed_at timestamp without time zone,
  CONSTRAINT ticket_analysis_pkey PRIMARY KEY (id),
  CONSTRAINT ticket_analysis_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES public.tickets(id)
);
CREATE TABLE public.ai_recommendations (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL,
  generated_at timestamp without time zone NOT NULL DEFAULT now(),
  type character varying CHECK (type::text = ANY (ARRAY['SAVING'::character varying, 'WARNING'::character varying, 'SHOPPING'::character varying, 'PRICE'::character varying, 'BUDGET'::character varying, 'GENERAL'::character varying]::text[])),
  title character varying NOT NULL,
  description text NOT NULL,
  priority character varying CHECK (priority::text = ANY (ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying]::text[])),
  read boolean NOT NULL DEFAULT false,
  CONSTRAINT ai_recommendations_pkey PRIMARY KEY (id),
  CONSTRAINT ai_recommendations_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id)
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
  CONSTRAINT audit_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.ai_reports (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL,
  title character varying NOT NULL,
  summary text NOT NULL,
  recommendations text,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT ai_reports_pkey PRIMARY KEY (id),
  CONSTRAINT ai_reports_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id)
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
  CONSTRAINT ai_actions_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES public.tickets(id),
  CONSTRAINT ai_actions_shopping_list_item_id_fkey FOREIGN KEY (shopping_list_item_id) REFERENCES public.shopping_list_items(id)
);
