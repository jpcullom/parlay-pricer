-- V1__initial_schema.sql
-- Core tables for SGP Analyzer

-- Raw game log data: one row per player per stat category per game
CREATE TABLE game_logs (
    id              BIGSERIAL PRIMARY KEY,
    game_id         VARCHAR(64)    NOT NULL,
    game_date       DATE           NOT NULL,
    player_name     VARCHAR(128)   NOT NULL,
    team            VARCHAR(10)    NOT NULL,
    market          VARCHAR(32)    NOT NULL,
    stat_value      DOUBLE PRECISION NOT NULL,

    CONSTRAINT uq_game_log UNIQUE (game_id, player_name, market)
);

CREATE INDEX idx_game_logs_player ON game_logs (player_name);
CREATE INDEX idx_game_logs_team   ON game_logs (team);
CREATE INDEX idx_game_logs_market ON game_logs (market);

-- Precomputed pairwise player correlations
CREATE TABLE player_correlations (
    id              BIGSERIAL PRIMARY KEY,
    player_a        VARCHAR(128)   NOT NULL,
    player_b        VARCHAR(128)   NOT NULL,
    market_a        VARCHAR(32)    NOT NULL,
    market_b        VARCHAR(32)    NOT NULL,
    correlation     DOUBLE PRECISION NOT NULL,
    sample_size     INT            NOT NULL,
    computed_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT uq_player_correlation UNIQUE (player_a, player_b, market_a, market_b)
);

CREATE INDEX idx_correlations_players ON player_correlations (player_a, player_b);

-- Aggregated player statistics (mean, stddev per market)
CREATE TABLE player_statistics (
    id              BIGSERIAL PRIMARY KEY,
    player_name     VARCHAR(128)   NOT NULL,
    team            VARCHAR(10)    NOT NULL,
    market          VARCHAR(32)    NOT NULL,
    mean            DOUBLE PRECISION NOT NULL,
    std_dev         DOUBLE PRECISION NOT NULL,
    sample_size     INT            NOT NULL,
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT uq_player_statistic UNIQUE (player_name, market)
);

CREATE INDEX idx_statistics_player ON player_statistics (player_name);
CREATE INDEX idx_statistics_team   ON player_statistics (team);
