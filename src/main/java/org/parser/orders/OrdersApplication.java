package org.parser.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
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

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableCaching
@AllArgsConstructor
public class OrdersApplication implements CommandLineRunner {

	private JsonProcessor jsonProcessor;

	private CsvProcessor csvProcessor;

	public static void main(String[] args) {
		SpringApplication.run(OrdersApplication.class, args);
	}

	@Override
	public void run(String... args) throws ExecutionException, InterruptedException {
		Checker.checkSupportedFormats(args);
		Stream<String> csvs = Arrays.stream(args).filter(arg -> arg.endsWith(".csv"));
		Stream<String> jsons = Arrays.stream(args).filter(arg -> arg.endsWith(".json"));
		CompletableFuture<List<Order>> jsonOrders = jsonProcessor.parseToObjectsFromStream(jsons);
		CompletableFuture<List<Order>> csvOrders = csvProcessor.parseToObjectsFromStream(csvs);
		ObjectMapper mapper = new ObjectMapper();
		CompletableFuture.allOf(jsonOrders, csvOrders).join();
		List<Order> orders = jsonOrders.get();
		orders.addAll(csvOrders.get());
		orders.forEach(order -> {
			try {
				System.out.println(mapper.writeValueAsString(order));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		});
	}
}
