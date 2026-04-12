CREATE TABLE password_change_requests (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    otp           VARCHAR(255) NOT NULL,
    status        VARCHAR(50)  NOT NULL DEFAULT 'GENERATED'
                  CHECK (status IN ('GENERATED', 'EXPIRED', 'CHANGED')),
    generated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    valid_to      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_pwd_chg_req_email ON password_change_requests(email);