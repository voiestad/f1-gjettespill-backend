package no.vebb.f1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class F1Application {

	public static void main(String[] args) {
		SpringApplication.run(F1Application.class, args);
	}

}
