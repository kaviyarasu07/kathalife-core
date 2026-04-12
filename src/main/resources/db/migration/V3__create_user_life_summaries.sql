CREATE TABLE user_life_summaries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID      NOT NULL UNIQUE
                    REFERENCES users(id) ON DELETE CASCADE,
    summary_text    TEXT      NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);