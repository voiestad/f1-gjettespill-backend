package no.vebb.f1.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

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
				CREATE TABLE IF NOT EXISTS RaceOrder (
					id INTEGER NOT NULL UNIQUE,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (year, position),
					FOREIGN KEY (id) REFERENCES Race(id) ON DELETE CASCADE
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
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS ConstructorYear (
					constructor TEXT NOT NULL,
					year INTEGER NOT NULL,
					position INTEGER NOT NULL,
					PRIMARY KEY (constructor, year),
					FOREIGN KEY (constructor) REFERENCES Constructor ON DELETE CASCADE
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
				CREATE TABLE IF NOT EXISTS Sprint (
					race_number INTEGER PRIMARY KEY,
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
					FOREIGN KEY (flag) REFERENCES Flag ON DELETE CASCADE
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
					FOREIGN KEY (constructor) REFERENCES Constructor ON DELETE CASCADE
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
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE
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
				CREATE TABLE IF NOT EXISTS CategoryTranslation (
					category TEXT PRIMARY KEY,
					translation TEXT NOT NULL,
					FOREIGN KEY (category) REFERENCES Category ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO CategoryTranslation (category, translation) 
                VALUES
                    ('DRIVER', 'Sjåfører'), 
                    ('CONSTRUCTOR', 'Konstruktører'),
                    ('FLAG', 'Antall'),
                    ('FIRST', '1.plass'),
                    ('TENTH', '10.plass')
                ON CONFLICT(category) DO NOTHING;
            """);
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS DiffPointsMap (
					category TEXT NOT NULL,
					diff INTEGER NOT NULL,
					points INTEGER NOT NULL,
					year INTEGER NOT NULL,
					PRIMARY KEY (category, diff, year),
					FOREIGN KEY (category) REFERENCES Category ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO DiffPointsMap (category, diff, points, year) 
                VALUES
                    ('DRIVER', 0, 20, 2025),
                    ('DRIVER', 1, 12, 2025),
                    ('DRIVER', 2, 6, 2025),
                    ('DRIVER', 3, 3, 2025),
                    ('DRIVER', 4, 1, 2025),

                    ('CONSTRUCTOR', 0, 30, 2025),
                    ('CONSTRUCTOR', 1, 15, 2025),
                    ('CONSTRUCTOR', 2, 5, 2025),

                    ('FLAG', 0, 50, 2025),
                    ('FLAG', 1, 40, 2025),
                    ('FLAG', 2, 35, 2025),
                    ('FLAG', 3, 30, 2025),
					('FLAG', 4, 28, 2025),
					('FLAG', 5, 26, 2025),
					('FLAG', 6, 24, 2025),
					('FLAG', 7, 22, 2025),
					('FLAG', 8, 20, 2025),
					('FLAG', 9, 18, 2025),
					('FLAG', 10, 16, 2025),
					('FLAG', 11, 14, 2025),
					('FLAG', 12, 12, 2025),
					('FLAG', 13, 10, 2025),
					('FLAG', 14, 9, 2025),
					('FLAG', 15, 8, 2025),
					('FLAG', 16, 7, 2025),
					('FLAG', 17, 6, 2025),
					('FLAG', 18, 5, 2025),
					('FLAG', 19, 4, 2025),
					('FLAG', 20, 3, 2025),
					('FLAG', 21, 2, 2025),
					('FLAG', 22, 1, 2025),

                    ('FIRST', 0, 5, 2025),
                    ('FIRST', 1, 2, 2025),
                    ('FIRST', 2, 1, 2025),

                    ('TENTH', 0, 10, 2025),
                    ('TENTH', 1, 8, 2025),
                    ('TENTH', 2, 6, 2025),
                    ('TENTH', 3, 5, 2025),
                    ('TENTH', 4, 4, 2025),
                    ('TENTH', 5, 3, 2025),
                    ('TENTH', 6, 2, 2025),
                    ('TENTH', 7, 1, 2025)
                ON CONFLICT(category, diff, year) DO NOTHING;
            """);
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS FlagStats (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					flag TEXT NOT NULL,
					race_number INTEGER NOT NULL,
					round INTEGER NOT NULL,
					FOREIGN KEY (flag) REFERENCES Flag ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS FlagTranslation (
					flag TEXT PRIMARY KEY,
					translation TEXT NOT NULL,
					FOREIGN KEY (flag) REFERENCES Flag ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO FlagTranslation (flag, translation) 
                VALUES
                    ('Yellow Flag', 'Gult Flagg'), 
                    ('Red Flag', 'Rødt Flagg'),
                    ('Safety Car', 'Sikkerhetsbil')
                ON CONFLICT(flag) DO NOTHING;
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
				cutoff TEXT NOT NULL
			);
			""");
		};
	}
}
