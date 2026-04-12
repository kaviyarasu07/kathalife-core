CREATE TABLE journal_activities (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         TEXT         NOT NULL,
    activity_date   DATE         NOT NULL,
    audio_file_path VARCHAR(500) NULL,
    stt_text        TEXT         NULL,
    stt_status      VARCHAR(20)  NOT NULL DEFAULT 'NONE'
                    CHECK (stt_status IN ('NONE','PENDING','PROCESSING','DONE','FAILED')),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP    NULL
);

CREATE INDEX idx_activities_user_date  ON journal_activities(user_id, activity_date DESC);
CREATE INDEX idx_activities_deleted    ON journal_activities(user_id, deleted_at);
CREATE INDEX idx_activities_stt_status ON journal_activities(stt_status);