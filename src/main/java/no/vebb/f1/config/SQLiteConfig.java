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
					id TEXT PRIMARY KEY,
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
					position TEXT NOT NULL,
					driver TEXT NOT NULL,
					PRIMARY KEY (race_number, driver),
					FOREIGN KEY (driver) REFERENCES Driver ON DELETE CASCADE,
					FOREIGN KEY (race_number) REFERENCES Race(id) ON DELETE CASCADE
			);
			""");
			jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS Flags (
					name TEXT PRIMARY KEY
			);
			""");
			jdbcTemplate.execute("""
                INSERT INTO Flags (name) 
                VALUES
                    ('Gult Flagg'), 
                    ('Rødt Flagg'),
                    ('Sikkerhetsbil')
                ON CONFLICT(name) DO NOTHING;
            """);
		};
	}
}
