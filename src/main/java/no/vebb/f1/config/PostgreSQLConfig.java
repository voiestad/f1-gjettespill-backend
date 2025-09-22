package no.vebb.f1.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgreSQLConfig {

	@Bean
	public CommandLineRunner intializeDB(JdbcTemplate jdbcTemplate) {
		return args -> {
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS users (
					google_id TEXT PRIMARY KEY,
					user_id UUID UNIQUE NOT NULL,
					username CITEXT COLLATE "nb_NO.utf8" UNIQUE NOT NULL
				);
			""");
			jdbcTemplate.execute("""
				CREATE SEQUENCE IF NOT EXISTS anonymous_username_seq START 1;
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS races (
					race_id INTEGER PRIMARY KEY,
					race_name TEXT NOT NULL
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS years (
					year INTEGER PRIMARY KEY
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS years_finished (
					year INTEGER PRIMARY KEY,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS placements_year (
					year INTEGER NOT NULL,
					user_id UUID NOT NULL,
					placement INTEGER NOT NULL,
					PRIMARY KEY (year, user_id),
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE,
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
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
			""");
			jdbcTemplate.execute("""
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
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS placements_race (
					race_id INTEGER NOT NULL,
					user_id UUID NOT NULL,
					placement INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (race_id, user_id),
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS placements_race_year_start (
					year INTEGER NOT NULL,
					user_id UUID NOT NULL,
					placement INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (year, user_id),
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE,
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS race_order (
					race_id INTEGER PRIMARY KEY,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS drivers (
					driver_name TEXT PRIMARY KEY
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS constructors (
					constructor_name TEXT PRIMARY KEY
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS drivers_year (
					driver_name TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (driver_name, year),
					FOREIGN KEY (driver_name) REFERENCES drivers(driver_name) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS constructors_year (
					constructor_name TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (constructor_name, year),
					FOREIGN KEY (constructor_name) REFERENCES constructors(constructor_name) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS drivers_team (
					driver_name TEXT NOT NULL,
					constructor_name TEXT NOT NULL,
					year INTEGER NOT NULL,
					PRIMARY KEY (driver_name, year),
					FOREIGN KEY (driver_name) REFERENCES drivers(driver_name) ON DELETE CASCADE,
					FOREIGN KEY (constructor_name) REFERENCES constructors(constructor_name) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS constructors_color (
					constructor_name TEXT NOT NULL,
					year INTEGER NOT NULL,
					color TEXT NOT NULL,
					PRIMARY KEY (constructor_name, year),
					FOREIGN KEY (constructor_name) REFERENCES constructors(constructor_name) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS drivers_alternative_name (
					alternative_name TEXT NOT NULL,
					year INTEGER NOT NULL,
					driver_name TEXT NOT NULL,
					PRIMARY KEY (alternative_name, year),
					FOREIGN KEY (driver_name) REFERENCES drivers(driver_name) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS driver_standings (
					race_id INTEGER NOT NULL,
					driver_name TEXT NOT NULL,
					position INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (race_id, driver_name),
					FOREIGN KEY (driver_name) REFERENCES drivers(driver_name) ON DELETE CASCADE,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS constructor_standings (
					race_id INTEGER NOT NULL,
					constructor_name TEXT NOT NULL,
					position INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (race_id, constructor_name),
					FOREIGN KEY (constructor_name) REFERENCES constructors(constructor_name) ON DELETE CASCADE,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS race_results (
					race_id INTEGER NOT NULL,
					position TEXT NOT NULL,
					finishing_position INTEGER NOT NULL,
					driver_name TEXT NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (race_id, finishing_position),
					FOREIGN KEY (driver_name) REFERENCES drivers(driver_name) ON DELETE CASCADE,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS starting_grids (
					race_id INTEGER NOT NULL,
					position INTEGER NOT NULL,
					driver_name TEXT NOT NULL,
					PRIMARY KEY (race_id, driver_name),
					FOREIGN KEY (driver_name) REFERENCES drivers(driver_name) ON DELETE CASCADE,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS flag_guesses (
					user_id UUID NOT NULL,
					flag_name TEXT NOT NULL,
					year INTEGER NOT NULL,
					amount INTEGER NOT NULL,
					PRIMARY KEY (user_id, flag_name, year),
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS constructor_guesses (
					user_id UUID NOT NULL,
					constructor_name TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (user_id, position, year),
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
					FOREIGN KEY (constructor_name) REFERENCES constructors(constructor_name) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS driver_guesses (
					user_id UUID NOT NULL,
					driver_name TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (user_id, position, year),
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
					FOREIGN KEY (driver_name) REFERENCES drivers(driver_name) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS driver_place_guesses (
					user_id UUID NOT NULL,
					race_id INTEGER NOT NULL,
					category_name TEXT NOT NULL,
					driver_name TEXT NOT NULL,
					PRIMARY KEY (user_id, race_id, category_name),
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
					FOREIGN KEY (driver_name) REFERENCES drivers(driver_name) ON DELETE CASCADE,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS user_year_idx ON flag_guesses (user_id, year);");
			jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS user_year_idx ON driver_guesses (user_id, year);");
			jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS user_year_idx ON constructor_guesses (user_id, year);");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS diff_points_mappings (
					category_name TEXT NOT NULL,
					diff INTEGER NOT NULL,
					points INTEGER NOT NULL,
					year INTEGER NOT NULL,
					PRIMARY KEY (category_name, diff, year),
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS flag_stats (
					flag_id SERIAL PRIMARY KEY,
					flag_name TEXT NOT NULL,
					race_id INTEGER NOT NULL,
					round INTEGER NOT NULL,
					session_type TEXT NOT NULL,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS admins (
					user_id UUID PRIMARY KEY,
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS race_cutoffs (
					race_id INTEGER PRIMARY KEY,
					cutoff TIMESTAMPTZ NOT NULL,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS year_cutoffs (
					year INTEGER PRIMARY KEY,
					cutoff TIMESTAMPTZ NOT NULL,
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS mailing_list (
					user_id UUID PRIMARY KEY,
					email TEXT NOT NULL UNIQUE,
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS notified (
					id SERIAL PRIMARY KEY,
					user_id UUID NOT NULL,
					race_id INTEGER NOT NULL,
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
					FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS verification_codes (
					user_id UUID PRIMARY KEY,
					verification_code INTEGER NOT NULL,
					email TEXT NOT NULL,
					cutoff TIMESTAMPTZ NOT NULL,
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS referral_codes (
					user_id UUID PRIMARY KEY,
					referral_code BIGINT NOT NULL,
					cutoff TIMESTAMPTZ NOT NULL,
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS mail_options (
					mail_option INTEGER PRIMARY KEY
				);
			""");
			jdbcTemplate.execute("""
                INSERT INTO mail_options (mail_option)
                VALUES
                    (1),
                    (2),
                    (3),
                    (6),
                    (12),
                    (24)
                ON CONFLICT(mail_option) DO NOTHING;
            """);
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS mail_preferences (
					user_id UUID NOT NULL,
					mail_option INTEGER NOT NULL,
					PRIMARY KEY (user_id, mail_option),
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
					FOREIGN KEY (mail_option) REFERENCES mail_options(mail_option) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS bingomasters (
					user_id UUID PRIMARY KEY,
					FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS bingo_cards (
					year INTEGER NOT NULL,
					bingo_square_id INTEGER NOT NULL,
					square_text TEXT NOT NULL,
					marked BOOLEAN NOT NULL,
					PRIMARY KEY (year, bingo_square_id),
					FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
				);
			""");
			jdbcTemplate.execute("""
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
			""");
			jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);");
			jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);");
			jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
					SESSION_PRIMARY_ID CHAR(36) NOT NULL,
					ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
					ATTRIBUTE_BYTES BYTEA NOT NULL,
					CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
					CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
				);
			""");
		};
	}
}
