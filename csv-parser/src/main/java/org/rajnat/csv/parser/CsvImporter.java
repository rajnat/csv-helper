package org.rajnat.csv.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.lang.String.format;

public class CsvImporter implements org.rajnat.csv.api.Importer {
    private static final Logger log = LoggerFactory.getLogger(CsvImporter.class);
    private final Deserializer deserializer = new Deserializer();
    /**
     * Imports a list of objects from a CSV file asynchronously.
     *
     * @param <T> the type of objects to import
     * @param fileName the name of the input CSV file
     * @param clazz the class type of the objects
     * @return a CompletableFuture containing a list of imported objects
     */
    @Override
    public <T> CompletableFuture<List<T>> importFromCsvAsync(String fileName, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> resultList = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String headerLine = reader.readLine();  // Read the header line
                if (!deserializer.validateHeaders(headerLine, clazz)) {
                    throw new IllegalArgumentException("CSV headers are invalid.");
                }
                String[] headers = headerLine.split(",");

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    T obj = deserializer.mapCsvToObject(values, headers, clazz);
                    resultList.add(obj);
                }
            } catch (IOException e) {
                log.error("Error reading CSV file: {}", fileName, e);
            }

            return resultList;
        });
    }

    @Override
    public <T> CompletableFuture<Stream<T>> importFromCsvStream(String filename, Class<T> clazz) {
        try {
            CsvIterator<T> iterator = new CsvIterator<T>(filename, deserializer, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


}
