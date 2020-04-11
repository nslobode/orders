package org.parser.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.parser.orders.model.Order;
import org.parser.orders.processor.impl.CsvProcessor;
import org.parser.orders.processor.impl.JsonProcessor;
import org.parser.orders.utils.Checker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Log4j2
@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableCaching
@AllArgsConstructor
public class OrdersApplication implements CommandLineRunner {

	private JsonProcessor jsonProcessor;

	private CsvProcessor csvProcessor;

	private ObjectMapper mapper;

	public static void main(String[] args) {
		SpringApplication.run(OrdersApplication.class, args);
	}

	@Override
	public void run(String... args) {
		Checker.checkSupportedFormats(args);

		Stream<String> csvs = Arrays.stream(args).filter(arg -> arg.endsWith(".csv"));
		Stream<String> jsons = Arrays.stream(args).filter(arg -> arg.endsWith(".json"));

		CompletableFuture<List<Order>> jsonOrders = jsonProcessor.parseToObjectsFromStream(jsons);
		CompletableFuture<List<Order>> csvOrders = csvProcessor.parseToObjectsFromStream(csvs);
		CompletableFuture.allOf(jsonOrders, csvOrders).join();
		List<Order> orders = null;
		try {
			orders = jsonOrders.get();
		} catch (InterruptedException e) {
			log.error("JsonProcessor has been Interrupted");
			System.exit(1);
		} catch (ExecutionException e) {
			log.error("JsonProcessor threw exception - {}", e::getCause);
			System.exit(1);
		}

		try {
			orders.addAll(csvOrders.get());
		} catch (InterruptedException e) {
			log.error("CsvProcessor has been Interrupted");
		} catch (ExecutionException e) {
			log.error("CsvProcessor threw exception - {}", e::getCause);
		}
		orders.forEach(order -> {
			try {
				System.out.println(mapper.writeValueAsString(order));
			} catch (JsonProcessingException e) {
				log.error("Somehow we couldn't map Order from file {} on line {} back to json - {}",
						order::getFilename, order::getId, e::getCause);
			}
		});
	}
}
