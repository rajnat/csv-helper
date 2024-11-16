import org.junit.jupiter.api.Test;
import org.rajnat.csv.parser.CsvExporter;
import org.rajnat.csv.parser.CsvImporter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvParseTest {

    @Test
    public void testSerialize() throws ExecutionException, InterruptedException {
        Employee emp1 = new Employee();
        emp1.setId(1);
        emp1.setName("John Doe");
        emp1.setAge(30);
        emp1.setSalary(50000.0);
        emp1.setContractType(ContractType.FULLTIME);

        Employee emp2 = new Employee();
        emp2.setId(2);
        emp2.setName("Jane Smith");
        emp2.setAge(28);
        emp2.setSalary(55000.0);
        emp2.setContractType(ContractType.PART_TIME);

        List<Employee> employees = Arrays.asList(emp1, emp2);
        CsvExporter exporter = new CsvExporter();
        // Export to CSV asynchronously
        CompletableFuture<?> exportFuture = exporter.exportToCsv(employees, "employees.csv");

        // Wait for the export to complete
        exportFuture.get();

        // Import from CSV
        CsvImporter importer = new CsvImporter();
        CompletableFuture<List<Employee>> importedEmployeesFuture = importer.importFromCsvAsync("employees.csv", Employee.class);
        List<Employee> importedEmployees = importedEmployeesFuture.get();
        assertEquals(employees, importedEmployees);
    }
}
