
import org.rajnat.csv.parser.CsvField;

import java.util.Objects;

public class Employee {
    @CsvField(name = "Employee ID", order = 1)
    private int id;

    @CsvField(name = "Name", order = 2)
    private String name;

    @CsvField(name = "Age", order = 3)
    private int age;

    @CsvField(name = "Salary", order = 4)
    private double salary;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id == employee.id && age == employee.age && Double.compare(salary, employee.salary) == 0 && Objects.equals(name, employee.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, salary);
    }
}

