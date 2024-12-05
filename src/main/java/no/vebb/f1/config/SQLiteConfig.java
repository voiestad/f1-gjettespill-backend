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
					username TEXT NOT NULL
			);
			""");
		};
	}
}
