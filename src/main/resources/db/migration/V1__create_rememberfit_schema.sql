CREATE TABLE decks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(40) NOT NULL,
    created_at TIMESTAMP(6)
);

CREATE TABLE cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    front_text VARCHAR(200) NOT NULL,
    back_text VARCHAR(1000) NOT NULL,
    repetition INTEGER NOT NULL DEFAULT 0,
    interval_days INTEGER NOT NULL DEFAULT 0,
    ease_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    next_review_date DATE,
    deck_id BIGINT NOT NULL,
    CONSTRAINT fk_cards_deck
        FOREIGN KEY (deck_id) REFERENCES decks (id) ON DELETE CASCADE
);

CREATE INDEX idx_cards_deck_review
    ON cards (deck_id, next_review_date, id);

CREATE TABLE study_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    deck_id BIGINT NOT NULL,
    quality INTEGER NOT NULL,
    repetition_after INTEGER NOT NULL,
    interval_days_after INTEGER NOT NULL,
    ease_factor_after DOUBLE PRECISION NOT NULL,
    next_review_date DATE NOT NULL,
    study_date DATE NOT NULL,
    studied_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_study_logs_study_date
    ON study_logs (study_date);

CREATE INDEX idx_study_logs_card_id
    ON study_logs (card_id);
