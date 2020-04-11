package org.parser.orders.processor.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.parser.orders.dataprovider.DataProvider;
import org.parser.orders.model.Order;
import org.parser.orders.processor.Processor;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class JsonProcessor implements Processor {

	private final ObjectMapper jsonMapper;

	private final DataProvider dataProvider;

	@Override
	@Async
	public CompletableFuture<List<Order>> parseToObjectsFromStream(Stream<String> filenames) {
		return CompletableFuture.completedFuture(parseToObjects(dataProvider.readBytesWithNames(filenames)));
	}

	private List<Order> parseToObjects(Map<byte[], String> files) {
		AtomicLong counter = new AtomicLong(1L);
		return files.entrySet().stream().parallel()
				.map(this::readValue)
				.flatMap(List::stream)
				.map(order -> {
					order.setLine(counter.getAndIncrement());
					return order;
				})
				.collect(Collectors.toList());
	}

	private List<Order> readValue(Map.Entry<byte[], String> pair) {
		JavaType orderList = jsonMapper.getTypeFactory().constructCollectionType(List.class, Order.class);
		List<Order> orders = null;
		try {
			orders = jsonMapper.readValue(pair.getKey(), orderList);
		} catch (JsonParseException parseEx){
			log.error("Couldn't parse json object {}, malformed file", () -> new String(pair.getKey()));
		} catch (IOException ioEx) {
			log.error("Couldn't read json object {}", () -> new String(pair.getKey()));
		}
		Objects.requireNonNull(orders, "Parsing failed").forEach(order -> order.setFilename(pair.getValue()));
		return orders;
	}
}
