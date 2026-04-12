-- Ensure one active entry per user per date
CREATE UNIQUE INDEX idx_activities_user_date_unique
ON journal_activities(user_id, activity_date)
WHERE deleted_at IS NULL;