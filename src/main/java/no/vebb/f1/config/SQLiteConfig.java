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
					PRIMARY KEY (driver, year),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS ConstructorYear (
					constructor TEXT NOT NULL,
					year INTEGER NOT NULL,
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
				CREATE TABLE IF NOT EXISTS SprintResult (
					race_number INTEGER NOT NULL,
					position TEXT NOT NULL,
					finishing_position INTEGER NOT NULL,
					driver TEXT NOT NULL,
					points TEXT NOT NULL,
					PRIMARY KEY (race_number, driver, finishing_position),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
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
					PRIMARY KEY (race_number, driver, finishing_position),
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
				CREATE TABLE IF NOT EXISTS FirstPlaceGuess (
					guesser TEXT NOT NULL,
					race_number INTEGER NOT NULL,
					driver TEXT NOT NULL,
					PRIMARY KEY (guesser, race_number),
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE,
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS TenthPlaceGuess (
					guesser TEXT NOT NULL,
					race_number INTEGER NOT NULL,
					driver TEXT NOT NULL,
					PRIMARY KEY (guesser, race_number),
					FOREIGN KEY (guesser) REFERENCES User(id) ON DELETE CASCADE,
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
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
				CREATE TABLE IF NOT EXISTS DiffPointsMap (
					category TEXT NOT NULL,
					diff INTEGER NOT NULL,
					points INTEGER NOT NULL,
					PRIMARY KEY (category, diff),
					FOREIGN KEY (category) REFERENCES Category ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO DiffPointsMap (category, diff, points) 
                VALUES
                    ('DRIVER', 0, 20),
                    ('DRIVER', 1, 12),
                    ('DRIVER', 2, 6),
                    ('DRIVER', 3, 3),
                    ('DRIVER', 4, 1),

                    ('CONSTRUCTOR', 0, 30),
                    ('CONSTRUCTOR', 1, 15),
                    ('CONSTRUCTOR', 2, 5),

                    ('FLAG', 0, 60),

                    ('FIRST', 0, 5),
                    ('FIRST', 1, 2),
                    ('FIRST', 2, 1),

                    ('TENTH', 0, 10),
                    ('TENTH', 1, 8),
                    ('TENTH', 2, 6),
                    ('TENTH', 3, 5),
                    ('TENTH', 4, 4),
                    ('TENTH', 5, 3),
                    ('TENTH', 6, 2),
                    ('TENTH', 7, 1)
                ON CONFLICT(category, diff) DO NOTHING;
            """);
		};
	}
}
