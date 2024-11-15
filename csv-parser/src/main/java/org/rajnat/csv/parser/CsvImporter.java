package org.rajnat.csv.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.String.format;

public class CsvImporter {
    private static final Logger log = LoggerFactory.getLogger(CsvImporter.class);

    public static <T> List<T> importFromCsv(String fileName, Class<T> clazz) throws IOException {
        List<T> resultList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String headerLine = reader.readLine();  // Read the header line
            String[] headers = headerLine.split(",");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                T obj = mapCsvToObject(values, headers, clazz);
                resultList.add(obj);
            }
        }

        return resultList;
    }

    private static <T> T mapCsvToObject(String[] values, String[] headers, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();

            // Sort fields based on 'order' value in CsvField
            List<Field> sortedFields = Arrays.stream(fields)
                    .filter(f -> f.isAnnotationPresent(CsvField.class))
                    .sorted(Comparator.comparingInt(f -> f.getAnnotation(CsvField.class).order()))
                    .toList();

            for (int i = 0; i < sortedFields.size(); i++) {
                Field field = sortedFields.get(i);
                field.setAccessible(true);
                CsvField annotation = field.getAnnotation(CsvField.class);
                // Find the column index by matching header names
                int index = findColumnIndex(annotation.name(), headers);
                if (index >= 0 && index < values.length) {
                    field.set(obj, convertValue(values[index], field.getType()));
                }
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int findColumnIndex(String columnName, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(columnName)) {
                return i;
            }
        }
        return -1;  // Column not found
    }

    private static Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (Enum.class.isAssignableFrom(targetType)) {
            return convertToEnum(value, targetType);
        }
        // Add more conversions as needed
        return null;
    }

    private static Object convertToEnum(String value, Class<?> enumType) {
        try {
            // Cast enumType to a Class<Enum> for safer handling
            @SuppressWarnings("unchecked")
            Class<Enum<?>> enumClass = (Class<Enum<?>>) enumType;

            // Check if the enum class has a 'fromString' method (if defined)
            try {
                // First, try to use the custom 'fromString' method if it exists.
                var fromStringMethod = enumClass.getMethod("fromString", String.class);
                return fromStringMethod.invoke(null, value);  // Call static fromString method
            } catch (NoSuchMethodException e) {
                // Fall back to default parsing using Enum.valueOf()
                return Enum.valueOf(enumClass.asSubclass(Enum.class), value.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            log.error(format("Invalid enum value: %s for enum: %s", value,enumType.getSimpleName()), e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // Return null if the value cannot be mapped to an enum
    }
}
