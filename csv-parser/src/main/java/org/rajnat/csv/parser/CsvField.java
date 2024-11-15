package org.rajnat.csv.parser;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvField {
    String name() default "";  // The column name in the CSV
    int order() default Integer.MAX_VALUE;  // Column order for serialization
}
