package org.rajnat.csv.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.String.format;

class Deserializer {
    private static final Logger log = LoggerFactory.getLogger(Deserializer.class);
    /**
     * Validates the CSV headers.
     *
     * @param <T> the type of objects to import
     * @param headerLine the header line from the CSV file
     * @param clazz the class type of the objects
     * @return true if the headers are valid, false otherwise
     */
    public <T> boolean validateHeaders(String headerLine, Class<T> clazz) {
        String[] headers = headerLine.split(",");
        Field[] fields = clazz.getDeclaredFields();

        List<Field> expectedFields = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(CsvField.class))
                .sorted(Comparator.comparingInt(f -> f.getAnnotation(CsvField.class).order()))
                .toList();

        if (headers.length != expectedFields.size()) {
            log.error("Number of headers does not match the number of fields.");
            return false;
        }

        for (int i = 0; i < expectedFields.size(); i++) {
            Field field = expectedFields.get(i);
            CsvField annotation = field.getAnnotation(CsvField.class);
            String expectedHeaderName = annotation.name();
            if (!headers[i].equals(expectedHeaderName)) {
                log.error(format("Header mismatch at index %d: expected '%s', but found '%s'",
                        i, expectedHeaderName, headers[i]));
                return false;
            }
        }

        return true;
    }

    /**
     * Maps CSV values to a new object instance.
     *
     * @param <T> the type of the object
     * @param values an array of string values from a CSV row
     * @param headers an array of header names corresponding to the values
     * @param clazz the class type of the object
     * @return an instance of the object with fields set from the CSV values
     */
    public <T> T mapCsvToObject(String[] values, String[] headers, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();

            // Sort fields based on 'order' value in CsvField
            List<Field> sortedFields = Arrays.stream(fields)
                    .filter(f -> f.isAnnotationPresent(CsvField.class))
                    .sorted(Comparator.comparingInt(f -> f.getAnnotation(CsvField.class).order()))
                    .toList();

            for (Field field : sortedFields) {
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
            String formattedValues = String.join(",", values);
            log.error(format("Failed to convert the CSV row [%s] to an object of type: %s", formattedValues, clazz.getName()));
            return null;
        }
    }

    public int findColumnIndex(String columnName, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(columnName)) {
                return i;
            }
        }
        return -1;  // Column not found
    }

    public Object convertValue(String value, Class<?> targetType) {
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

    public Object convertToEnum(String value, Class<?> enumType) {
        try {
            // Cast enumType to a Class<Enum> for safer handling
            @SuppressWarnings("unchecked")
            Class<Enum<?>> enumClass = (Class<Enum<?>>) enumType;

            // Check if the enum class has a 'fromString' method (if defined)
            try {
                // First, try to use the custom 'fromString' method if it exists.
                var fromStringMethod = enumClass.getMethod("fromString", String.class);
                return fromStringMethod.invoke(null, value);  // Call static fromString method
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Fall back to default parsing using Enum.valueOf()
                return Enum.valueOf(enumClass.asSubclass(Enum.class), value.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid enum value: {} for enum: {}", value, enumType.getSimpleName(), e);
        }
        return null;  // Return null if the value cannot be mapped to an enum
    }
}
