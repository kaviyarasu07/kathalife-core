ALTER TABLE journal_activities
ADD COLUMN story_locked BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_activities_story_locked
ON journal_activities(user_id, story_locked);
