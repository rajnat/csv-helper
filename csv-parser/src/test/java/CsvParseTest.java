import org.junit.jupiter.api.Test;
import org.rajnat.csv.exception.CsvParseException;
import org.rajnat.csv.parser.CsvExporter;
import org.rajnat.csv.parser.CsvImporter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvParseTest {

    @Test
    public void testSerialize() throws IOException, CsvParseException {
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
        CsvExporter.exportToCsv(employees, "employees.csv");
        // Import from CSV
        List<Employee> importedEmployees = CsvImporter.importFromCsv("employees.csv", Employee.class);
        assertEquals(employees, importedEmployees);
    }
}
