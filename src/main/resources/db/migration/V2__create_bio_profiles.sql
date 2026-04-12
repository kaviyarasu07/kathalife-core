CREATE TABLE bio_profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL UNIQUE
                    REFERENCES users(id) ON DELETE CASCADE,
    full_name       VARCHAR(255) NULL,
    date_of_birth   DATE         NULL,
    hometown        VARCHAR(255) NULL,
    occupation      VARCHAR(255) NULL,
    family_notes    VARCHAR(500) NULL,
    profile_pic_url VARCHAR(500) NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bio_profiles_user_id ON bio_profiles(user_id);