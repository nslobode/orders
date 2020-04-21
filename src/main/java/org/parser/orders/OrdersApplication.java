package org.parser.orders;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.parser.orders.runner.Runner;

@SpringBootApplication
@EnableAsync
@AllArgsConstructor
public class OrdersApplication implements CommandLineRunner {

	private Runner runner;

	public static void main(String[] args) {
		SpringApplication.run(OrdersApplication.class, args);
	}

	@Override
	public void run(String... args) {
		runner.runProcessing(args);
	}
}
