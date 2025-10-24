CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY,
    google_id TEXT UNIQUE NOT NULL,
    username CITEXT COLLATE "nb_NO.utf8" UNIQUE NOT NULL
);
CREATE SEQUENCE IF NOT EXISTS anonymous_username_seq START 1;
CREATE TABLE IF NOT EXISTS years (
    year INTEGER PRIMARY KEY
);
CREATE TABLE IF NOT EXISTS races (
    race_id INTEGER PRIMARY KEY,
    race_name TEXT NOT NULL,
    year INTEGER NOT NULL,
    position INTEGER NOT NULL,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS years_finished (
    year INTEGER PRIMARY KEY,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS placements_year (
    year INTEGER NOT NULL,
    user_id UUID NOT NULL,
    placement INTEGER NOT NULL,
    PRIMARY KEY (year, user_id),
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS placements_category (
    race_id INTEGER NOT NULL,
    user_id UUID NOT NULL,
    category_name TEXT NOT NULL,
    placement INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (race_id, user_id, category_name),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS placements_category_year_start (
    year INTEGER NOT NULL,
    user_id UUID NOT NULL,
    category_name TEXT NOT NULL,
    placement INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (year, user_id, category_name),
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS placements_race (
    race_id INTEGER NOT NULL,
    user_id UUID NOT NULL,
    placement INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (race_id, user_id),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS placements_race_year_start (
    year INTEGER NOT NULL,
    user_id UUID NOT NULL,
    placement INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (year, user_id),
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS drivers (
    driver_id SERIAL PRIMARY KEY,
    driver_name TEXT NOT NULL,
    year INTEGER NOT NULL,
    position INTEGER NOT NULL,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE,
    UNIQUE (driver_name, year)
);
CREATE TABLE IF NOT EXISTS constructors (
    constructor_id SERIAL PRIMARY KEY,
    constructor_name TEXT NOT NULL,
    year INTEGER NOT NULL,
    position INTEGER NOT NULL,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE,
    UNIQUE (constructor_name, year)
);
CREATE TABLE IF NOT EXISTS drivers_team (
    driver_id INTEGER PRIMARY KEY,
    constructor_id INTEGER NOT NULL,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE,
    FOREIGN KEY (constructor_id) REFERENCES constructors(constructor_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS constructors_color (
    constructor_id INTEGER PRIMARY KEY ,
    color TEXT NOT NULL,
    FOREIGN KEY (constructor_id) REFERENCES constructors(constructor_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS driver_standings (
    race_id INTEGER NOT NULL,
    driver_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (race_id, driver_id),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS constructor_standings (
    race_id INTEGER NOT NULL,
    constructor_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (race_id, constructor_id),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (constructor_id) REFERENCES constructors(constructor_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS race_results (
    race_id INTEGER NOT NULL,
    qualified_position TEXT NOT NULL,
    position INTEGER NOT NULL,
    driver_id INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (race_id, position),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS starting_grids (
    race_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    driver_id INTEGER NOT NULL,
    PRIMARY KEY (race_id, driver_id),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS flag_guesses (
    user_id UUID NOT NULL,
    flag_name TEXT NOT NULL,
    year INTEGER NOT NULL,
    amount INTEGER NOT NULL,
    PRIMARY KEY (user_id, flag_name, year),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS constructor_guesses (
    user_id UUID NOT NULL,
    constructor_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    position INTEGER NOT NULL,
    PRIMARY KEY (user_id, position, year),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (constructor_id) REFERENCES constructors(constructor_id) ON DELETE CASCADE,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS driver_guesses (
    user_id UUID NOT NULL,
    driver_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    position INTEGER NOT NULL,
    PRIMARY KEY (user_id, position, year),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS driver_place_guesses (
    user_id UUID NOT NULL,
    race_id INTEGER NOT NULL,
    category_name TEXT NOT NULL,
    driver_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, race_id, category_name),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE,
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS user_year_idx ON flag_guesses (user_id, year);
CREATE INDEX IF NOT EXISTS user_year_idx ON driver_guesses (user_id, year);
CREATE INDEX IF NOT EXISTS user_year_idx ON constructor_guesses (user_id, year);
CREATE TABLE IF NOT EXISTS diff_points_mappings (
    category_name TEXT NOT NULL,
    diff INTEGER NOT NULL,
    points INTEGER NOT NULL,
    year INTEGER NOT NULL,
    PRIMARY KEY (category_name, diff, year),
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS flag_stats (
    flag_id SERIAL PRIMARY KEY,
    flag_name TEXT NOT NULL,
    race_id INTEGER NOT NULL,
    round INTEGER NOT NULL,
    session_type TEXT NOT NULL,
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS admins (
    user_id UUID PRIMARY KEY,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS race_cutoffs (
    race_id INTEGER PRIMARY KEY,
    cutoff TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS year_cutoffs (
    year INTEGER PRIMARY KEY,
    cutoff TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS notified (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    race_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS referral_codes (
    user_id UUID PRIMARY KEY,
    referral_code BIGINT NOT NULL,
    cutoff TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS guess_reminder_options (
    guess_reminder_option INTEGER PRIMARY KEY
);
INSERT INTO guess_reminder_options (guess_reminder_option)
VALUES
    (1),
    (2),
    (3),
    (6),
    (12),
    (24)
ON CONFLICT(guess_reminder_option) DO NOTHING;
CREATE TABLE IF NOT EXISTS guess_reminder_preferences (
    user_id UUID NOT NULL,
    guess_reminder_option INTEGER NOT NULL,
    PRIMARY KEY (user_id, guess_reminder_option),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (guess_reminder_option) REFERENCES guess_reminder_options(guess_reminder_option) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS bingomasters (
    user_id UUID PRIMARY KEY,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS bingo_cards (
    year INTEGER NOT NULL,
    bingo_square_id INTEGER NOT NULL,
    square_text TEXT NOT NULL,
    marked BOOLEAN NOT NULL,
    PRIMARY KEY (year, bingo_square_id),
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS ntfy_topics (
    user_id UUID PRIMARY KEY,
    topic UUID UNIQUE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);
CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);
CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BYTEA NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);
