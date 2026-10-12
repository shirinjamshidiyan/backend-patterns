CREATE TABLE IF NOT EXISTS one_time_tokens (
   token_value VARCHAR(36) NOT NULL PRIMARY KEY,
   username VARCHAR(255) NOT NULL,
   expires_at TIMESTAMP NOT NULL
);