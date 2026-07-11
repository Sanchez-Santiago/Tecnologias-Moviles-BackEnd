DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'groups' AND column_name = 'type') THEN
        ALTER TABLE groups ALTER COLUMN "type" SET DEFAULT 'FAMILIA';
    END IF;
END $$;
