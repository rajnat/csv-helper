package org.rajnat.csv.parser;
import org.rajnat.csv.exception.CsvParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

public class CsvExporter {
    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);
    public static <T> void exportToCsv(List<T> data, String fileName) throws IOException, CsvParseException {
        if (data == null || data.isEmpty()) {
            return;
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            // Get the header row from the first object in the list
            T firstObject = data.get(0);
            String header = getCsvHeader(firstObject);
            writer.append(header).append("\n");

            // Write the data rows
            for (T item : data) {
                String row = getCsvRow(item);
                writer.append(row).append("\n");
            }
        }
    }

    private static <T> String getCsvHeader(T object) {
        StringBuilder header = new StringBuilder();
        Field[] fields = object.getClass().getDeclaredFields();

        // Sort fields based on 'order' value in CsvField
        List<Field> sortedFields = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(CsvField.class))
                .sorted((f1, f2) -> Integer.compare(f1.getAnnotation(CsvField.class).order(),
                        f2.getAnnotation(CsvField.class).order()))
                .toList();

        for (Field field : sortedFields) {
            CsvField annotation = field.getAnnotation(CsvField.class);
            header.append(annotation.name()).append(",");
        }

        return header.toString().replaceAll(",$", ""); // Remove the trailing comma
    }

    private static <T> String getCsvRow(T object) throws CsvParseException {
        StringBuilder row = new StringBuilder();
        Field[] fields = object.getClass().getDeclaredFields();

        // Sort fields based on 'order' value in CsvField
        List<Field> sortedFields = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(CsvField.class))
                .sorted((f1, f2) -> Integer.compare(f1.getAnnotation(CsvField.class).order(),
                        f2.getAnnotation(CsvField.class).order()))
                .toList();

        for (Field field : sortedFields) {
            try {
                field.setAccessible(true); // Make the field accessible even if it's private
                Object value = field.get(object);
                row.append(value != null ? value.toString() : "").append(",");
            } catch (IllegalAccessException e) {
                log.error("Illegal access Exception while accessing field {}", field, e);
                throw new CsvParseException(format("Failed to access field: %s", field), e);
            }
        }

        return row.toString().replaceAll(",$", ""); // Remove the trailing comma
    }
}