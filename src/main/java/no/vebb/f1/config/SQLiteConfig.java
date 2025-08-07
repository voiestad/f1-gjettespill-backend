package no.vebb.f1.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Class is responsible for setting up the required tables in the database.
 */
@Configuration
public class SQLiteConfig {

	@Bean
	public CommandLineRunner intializeDB(JdbcTemplate jdbcTemplate) {
		return args -> {
			jdbcTemplate.execute("PRAGMA foreign_keys = ON;");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS User (
					google_id TEXT PRIMARY KEY,
					id TEXT UNIQUE NOT NULL,
					username TEXT NOT NULL,
					username_upper TEXT NOT NULL
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Race (
					id INTEGER PRIMARY KEY,
					name TEXT NOT NULL
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Year (
					year INTEGER PRIMARY KEY
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS YearFinished (
					year INTEGER PRIMARY KEY,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS PlacementYear (
					year INTEGER NOT NULL,
					guesser TEXT NOT NULL,
					placement INTEGER NOT NULL,
					PRIMARY KEY (year, guesser),
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE,
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS PlacementCategory (
					race_number INTEGER NOT NULL,
					guesser TEXT NOT NULL,
					category TEXT NOT NULL,
					placement INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (race_number, guesser, category),
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE,
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE,
				    FOREIGN KEY (category) REFERENCES Category(name) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS PlacementCategoryYearStart (
					race_number INTEGER NOT NULL,
					guesser TEXT NOT NULL,
					category TEXT NOT NULL,
					placement INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (race_number, guesser, category),
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE,
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE,
				    FOREIGN KEY (category) REFERENCES Category(name) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS PlacementRace (
					race_number INTEGER NOT NULL,
					guesser TEXT NOT NULL,
					placement INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (race_number, guesser),
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS PlacementRaceYearStart (
					year INTEGER NOT NULL,
					guesser TEXT NOT NULL,
					placement INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (year, guesser),
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE,
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS RaceOrder (
					id INTEGER NOT NULL UNIQUE,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (year, position),
					FOREIGN KEY (id) REFERENCES Race(id) ON DELETE CASCADE,
						FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Driver (
					name TEXT PRIMARY KEY
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Constructor (
					name TEXT PRIMARY KEY
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS DriverYear (
					driver TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (driver, year),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS ConstructorYear (
					constructor TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (constructor, year),
					FOREIGN KEY (constructor) REFERENCES Constructor ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS DriverTeam (
					driver TEXT NOT NULL,
					team TEXT NOT NULL,
					year INTEGER NOT NULL,
					PRIMARY KEY (driver, year),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (team) REFERENCES Constructor ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS ConstructorColor (
					constructor TEXT NOT NULL,
					year INTEGER NOT NULL,
					color TEXT NOT NULL,
					PRIMARY KEY (constructor, year),
					FOREIGN KEY (constructor) REFERENCES Constructor ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS DriverAlternativeName (
					alternative_name TEXT NOT NULL,
					driver TEXT NOT NULL,
					year INTEGER NOT NULL,
					PRIMARY KEY (alternative_name, year),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS DriverStandings (
					race_number INTEGER NOT NULL,
					driver TEXT NOT NULL,
					position INTEGER NOT NULL,
					points TEXT NOT NULL,
					PRIMARY KEY (race_number, driver),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS ConstructorStandings (
					race_number INTEGER NOT NULL,
					constructor TEXT NOT NULL,
					position INTEGER NOT NULL,
					points TEXT NOT NULL,
					PRIMARY KEY (race_number, constructor),
					FOREIGN KEY (constructor) REFERENCES Constructor ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS RaceResult (
					race_number INTEGER NOT NULL,
					position TEXT NOT NULL,
					finishing_position INTEGER NOT NULL,
					driver TEXT NOT NULL,
					points TEXT NOT NULL,
					PRIMARY KEY (race_number, finishing_position),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS StartingGrid (
					race_number INTEGER NOT NULL,
					position INTEGER NOT NULL,
					driver TEXT NOT NULL,
					PRIMARY KEY (race_number, driver),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Flag (
					name TEXT PRIMARY KEY
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO Flag (name)
                VALUES
                    ('Yellow Flag'),
                    ('Red Flag'),
                    ('Safety Car')
                ON CONFLICT(name) DO NOTHING;
            """);
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS FlagGuess (
					guesser TEXT NOT NULL,
					flag TEXT NOT NULL,
					year INTEGER NOT NULL,
					amount INTEGER NOT NULL,
					PRIMARY KEY (guesser, flag, year),
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE,
					FOREIGN KEY (flag) REFERENCES Flag ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS ConstructorGuess (
					guesser TEXT NOT NULL,
					constructor TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (guesser, position, year),
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE,
					FOREIGN KEY (constructor) REFERENCES Constructor ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS DriverGuess (
					guesser TEXT NOT NULL,
					driver TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (guesser, position, year),
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE,
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Category (
					name TEXT PRIMARY KEY
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO Category (name)
                VALUES
                    ('DRIVER'),
                    ('CONSTRUCTOR'),
                    ('FLAG'),
                    ('FIRST'),
                    ('TENTH')
                ON CONFLICT(name) DO NOTHING;
            """);
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS DriverPlaceGuess (
					guesser TEXT NOT NULL,
					race_number INTEGER NOT NULL,
					category TEXT NOT NULL,
					driver TEXT NOT NULL,
					PRIMARY KEY (guesser, race_number, category),
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE,
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (category) REFERENCES Category ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS DiffPointsMap (
					category TEXT NOT NULL,
					diff INTEGER NOT NULL,
					points INTEGER NOT NULL,
					year INTEGER NOT NULL,
					PRIMARY KEY (category, diff, year),
					FOREIGN KEY (category) REFERENCES Category ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS SessionType (
					name PRIMARY KEY
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO SessionType (name)
                VALUES
                    ('RACE'),
                    ('SPRINT')
                ON CONFLICT(name) DO NOTHING;
            """);
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS FlagStats (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					flag TEXT NOT NULL,
					race_number INTEGER NOT NULL,
					round INTEGER NOT NULL,
					session_type TEXT NOT NULL,
					FOREIGN KEY (flag) REFERENCES Flag ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE,
					FOREIGN KEY (session_type) REFERENCES SessionType ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Admin (
					user_id TEXT PRIMARY KEY,
					FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS RaceCutoff (
					race_number INTEGER PRIMARY KEY,
					cutoff TEXT NOT NULL,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS YearCutoff (
					year INTEGER PRIMARY KEY,
					cutoff TEXT NOT NULL,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS MailingList (
					user_id TEXT PRIMARY KEY,
					email TEXT NOT NULL,
					FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Notified (
				user_id TEXT NOT NULL,
				race_number INTEGER NOT NULL,
				FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
				FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS VerificationCode (
				user_id TEXT PRIMARY KEY,
				verification_code INTEGER NOT NULL,
				email TEXT NOT NULL,
				cutoff TEXT NOT NULL,
				FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS ReferralCode (
				user_id TEXT PRIMARY KEY,
				referral_code INTEGER NOT NULL,
				cutoff TEXT NOT NULL,
				FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS MailOption (
					option INTEGER PRIMARY KEY
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO MailOption (option)
                VALUES
                    (1),
                    (2),
                    (3),
                    (6),
                    (12),
                    (24)
                ON CONFLICT(option) DO NOTHING;
            """);
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS MailPreference (
				user_id TEXT NOT NULL,
				option INTEGER NOT NULL,
				PRIMARY KEY (user_id, option),
				FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
				FOREIGN KEY (option) REFERENCES MailOption(option) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Bingomaster (
				user_id TEXT PRIMARY KEY,
				FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS BingoCard (
					year INTEGER NOT NULL,
					id INTEGER NOT NULL,
					square_text TEXT NOT NULL,
					marked INTEGER NOT NULL,
					PRIMARY KEY (year, id),
					FOREIGN KEY (year) REFERENCES Year(year) ON DELETE CASCADE
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
				CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID),
				CONSTRAINT SPRING_SESSION_UK UNIQUE (SESSION_ID)
			);
			""");
			jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (EXPIRY_TIME);");
			jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (PRINCIPAL_NAME);");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
				SESSION_PRIMARY_ID CHAR(36) NOT NULL,
				ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
				ATTRIBUTE_BYTES BLOB NOT NULL,
				CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
				CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID)
				REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
				);
			""");
		};
	}
}
