CREATE TABLE IF NOT EXISTS todos (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO todos (title, completed)
VALUES
    ('Wire Vue to the Spring API', true),
    ('Add your first domain feature', false)
ON CONFLICT DO NOTHING;
