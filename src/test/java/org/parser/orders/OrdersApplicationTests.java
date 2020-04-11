package org.parser.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.parser.orders.dataprovider.DataProvider;
import org.parser.orders.model.Order;
import org.parser.orders.processor.Processor;
import org.parser.orders.processor.impl.CsvProcessor;
import org.parser.orders.processor.impl.JsonProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@SpringBootTest
class OrdersApplicationTests {

	@Autowired
	private ObjectMapper jsonMapper;

	@Test
	void contextLoads() {
	}

	@Test
	public void endToEndTest() {

		//prepare test processors
		String[] args = {"orders1.json", "orders2.json", "orders666.csv"};
		Stream<String> csvs = Arrays.stream(args).filter(arg -> arg.endsWith(".csv"));
		Stream<String> jsons = Arrays.stream(args).filter(arg -> arg.endsWith(".json"));
		DataProvider dataProvider = new DataProvider();
		dataProvider.setDirName("src/test/resources/from-here");
		Processor jsonProcessor = new JsonProcessor(jsonMapper, dataProvider);
		Processor csvProcessor = new CsvProcessor(dataProvider);

		//execute processors
		CompletableFuture<List<Order>> jsonOrders = jsonProcessor.parseToObjectsFromStream(jsons);
		CompletableFuture<List<Order>> csvOrders = csvProcessor.parseToObjectsFromStream(csvs);
		CompletableFuture.allOf(jsonOrders, csvOrders).join();

		//extract data from future
		List<Order> listFromJson = extractJsonOrders(jsonOrders);
		List<Order> listFromCsv = extractCsvOrders(csvOrders);

		runAssertions(listFromJson, listFromCsv);
	}

	private void runAssertions(List<Order> listFromJson, List<Order> listFromCsv) {
		assertEquals(6, listFromJson.size());
		assertEquals(2, listFromCsv.size());

		assertEquals(2, (int) listFromJson.stream()
				.filter(order -> Objects.equals(order.getAmount(), -1L))
				.count());
		assertEquals(1, (int) listFromCsv.stream()
				.filter(order -> Objects.equals(order.getAmount(), 69L))
				.count());

		assertEquals(4, (int) listFromJson.stream()
				.filter(order -> Objects.equals(order.getResult(), "OK"))
				.count());
		assertEquals(2, (int) listFromCsv.stream()
				.filter(order -> Objects.equals(order.getResult(), "OK"))
				.count());
	}

	private List<Order> extractCsvOrders(CompletableFuture<List<Order>> csvOrders) {
		List<Order> listFromCsv = null;
		try {
			listFromCsv =csvOrders.get();
		} catch (InterruptedException e) {
			log.error("CsvProcessor has been Interrupted");
		} catch (ExecutionException e) {
			log.error("CsvProcessor threw exception - {}", e::getCause);
		}
		return listFromCsv;
	}

	private List<Order> extractJsonOrders(CompletableFuture<List<Order>> jsonOrders) {
		List<Order> listFromJson = null;
		try {
			listFromJson = jsonOrders.get();
		} catch (InterruptedException e) {
			log.error("JsonProcessor has been Interrupted");
			System.exit(1);
		} catch (ExecutionException e) {
			log.error("JsonProcessor threw exception - {}", e::getCause);
			System.exit(1);
		}
		return listFromJson;
	}
}
