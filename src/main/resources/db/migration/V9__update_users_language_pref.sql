-- Make language_pref nullable
-- User selects language on bio page, not signup
ALTER TABLE users
    ALTER COLUMN language_pref DROP NOT NULL,
    ALTER COLUMN language_pref DROP DEFAULT;

-- Add FK constraint to languages table
ALTER TABLE users
    ADD CONSTRAINT fk_users_language
    FOREIGN KEY (language_pref)
    REFERENCES languages(code)
    ON UPDATE CASCADE
    ON DELETE SET NULL;