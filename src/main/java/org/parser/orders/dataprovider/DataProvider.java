package org.parser.orders.dataprovider;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data Provider class
 * prepares data for dedicated parsers
 */
@Log4j2
@Component("dataProvider")
@Scope("prototype")
@Setter
public class DataProvider {

	/**
	 * Directory in which files to be parsed are located
	 */
	@Value("${source.directory}")
	private String dirName;

	/**
	 * Data preparation for json processor
	 * @param filenames - stream of json files filenames
	 * @return map of bytes with filenames as values
	 */
	public Map<byte[], String> readBytesWithNames(Stream<String> filenames) {
		Function<Path, String> getFileName = path -> path.getFileName().toString();
		return filenames.parallel()
				.map(filename -> Paths.get(dirName, filename))
				.collect(Collectors.toMap(this::readAllBytes, getFileName));
	}

	/**
	 * Data preparation for csv processor
	 * @param filenames - stream of csv files filenames
	 * @return map of lists of csv lines with filenames as values
	 */
	public Map<List<CSVRecord>, String> readLinesWithNames(Stream<String> filenames) {
		Function<Path, String> getFileName = path -> path.getFileName().toString();
		return filenames.parallel()
				.map(filename -> Paths.get(dirName, filename))
				.collect(Collectors.toMap(this::readCsvRecord, getFileName));
	}

	private byte[] readAllBytes(Path path) {
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(path);
		} catch (IOException e) {
			log.error("Couldn't read from file {}, IOException is {}", () -> path, e::getMessage);
		}
		return bytes;
	}

	private List<CSVRecord> readCsvRecord(Path path) {
		List<CSVRecord> records = new ArrayList<>();
		try (CSVParser csvInputFile = new CSVParser(Files.newBufferedReader(path), CSVFormat.RFC4180)) {
			records = csvInputFile.getRecords();
		} catch (IOException e) {
			log.error("Couldn't read from file {}, IOException is {}", () -> path, e::getMessage);
		}
		return records;
	}
}
