CREATE TABLE languages (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code          VARCHAR(10)  NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    native_name   VARCHAR(100) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    tts_supported BOOLEAN      NOT NULL DEFAULT false,
    stt_supported BOOLEAN      NOT NULL DEFAULT false,
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_languages_code      ON languages(code);
CREATE INDEX idx_languages_is_active ON languages(is_active);

INSERT INTO languages
    (code, name, native_name, is_active,
     tts_supported, stt_supported, display_order)
VALUES
    ('ta', 'Tamil',    'தமிழ்',     true, true,  true,  1),
    ('te', 'Telugu',   'తెలుగు',    true, true,  true,  2),
    ('hi', 'Hindi',    'हिन्दी',    true, true,  true,  3),
    ('ml', 'Malayalam','മലയാളം',   true, true,  true,  4),
    ('kn', 'Kannada',  'ಕನ್ನಡ',    true, true,  true,  5),
    ('bn', 'Bengali',  'বাংলা',     true, false, false, 6);