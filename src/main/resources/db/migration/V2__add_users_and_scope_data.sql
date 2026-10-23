CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

-- 기존 데이터를 버리지 않기 위해, 암기장이 이미 있으면 자리표시 사용자에게 귀속시킨다.
-- password_hash 는 BCrypt 형식이 아니므로 이 계정으로는 로그인할 수 없다.
-- decks 가 비어 있으면 SELECT DISTINCT 가 0행을 돌려주어 아무것도 만들지 않는다.
INSERT INTO users (email, password_hash, nickname, created_at)
SELECT DISTINCT 'legacy@rememberfit.invalid', '(migrated)', '이전 사용자', CURRENT_TIMESTAMP
FROM decks;

ALTER TABLE decks ADD COLUMN user_id BIGINT;
UPDATE decks SET user_id = (SELECT MIN(id) FROM users);
ALTER TABLE decks MODIFY COLUMN user_id BIGINT NOT NULL;
ALTER TABLE decks ADD CONSTRAINT fk_decks_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX idx_decks_user_id ON decks (user_id);

ALTER TABLE study_logs ADD COLUMN user_id BIGINT;
UPDATE study_logs SET user_id = (SELECT MIN(id) FROM users);
ALTER TABLE study_logs MODIFY COLUMN user_id BIGINT NOT NULL;

CREATE INDEX idx_study_logs_user_study_date ON study_logs (user_id, study_date);
