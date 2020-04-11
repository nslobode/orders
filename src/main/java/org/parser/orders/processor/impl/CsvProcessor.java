package org.parser.orders.processor.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVRecord;
import org.parser.orders.dataprovider.DataProvider;
import org.parser.orders.model.Order;
import org.parser.orders.processor.Processor;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.parser.orders.utils.ErrorMessages.ERROR;
import static org.parser.orders.utils.ErrorMessages.FIELD_IS_MISSING;

@Log4j2
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class CsvProcessor implements Processor {

	private final DataProvider dataProvider;

	@Override
	@Async
	public CompletableFuture<List<Order>> parseToObjectsFromStream(Stream<String> filenames) {
		return CompletableFuture.completedFuture(parseToObjects(dataProvider.readLinesWithNames(filenames)));
	}

	private List<Order> parseToObjects(Map<List<CSVRecord>, String> files) {
		AtomicInteger counter = new AtomicInteger(1);
		return files.entrySet().stream().parallel().map(this::readValue).flatMap(List::stream)
				.map(order -> {
					order.setLine(counter.getAndIncrement());
					return order;
				})
				.collect(Collectors.toList());
	}

	private List<Order> readValue(Map.Entry<List<CSVRecord>, String> pair) {
		List<CSVRecord> files = pair.getKey();
		List<Order> orders = new ArrayList<>();
		for (CSVRecord row : files) {
			Order order = new Order();
			StringBuilder errors = new StringBuilder();
			String id = row.get(0);
			if (id != null) {
				try {
					order.setId(Integer.parseInt(id));
				} catch (NumberFormatException e) {
					order.setId(-1);
					errors.append("Couldn't parse id, not a valid number. ");
				}
			} else {
				order.setId(-1);
				errors.append("Couldn't get id field");
			}
			String amount = row.get(1);
			if (amount != null) {
				try {
					order.setAmount(Integer.parseInt(amount));
				} catch (NumberFormatException e) {
					order.setAmount(-1L);
					errors.append("Couldn't parse amount, not a valid number. ");
				}
			} else {
				order.setAmount(-1L);
				errors.append(FIELD_IS_MISSING + "amount.");
			}
			String currency = row.get(2);
			if (currency == null) {
				order.setCurrency(ERROR);
				errors.append(FIELD_IS_MISSING + "currency. ");
			} else {
				order.setCurrency(currency);
			}
			String comment = row.get(3);
			if (comment == null) {
				order.setComment(ERROR);
				errors.append(FIELD_IS_MISSING + "comment. ");
			} else {
				order.setComment(comment);
			}
			if (StringUtils.isEmpty(errors.toString())) {
				order.setResult("OK");
			} else {
				order.setResult(errors.toString());
			}
			order.setFilename(pair.getValue());
			orders.add(order);
		}
		return orders;
	}
}
