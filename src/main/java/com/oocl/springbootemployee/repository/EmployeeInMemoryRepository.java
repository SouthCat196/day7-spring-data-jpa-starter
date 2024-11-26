package com.oocl.springbootemployee.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.oocl.springbootemployee.model.Employee;
import com.oocl.springbootemployee.model.Gender;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeInMemoryRepository {
    private final List<Employee> employees = new ArrayList<>();

    public EmployeeInMemoryRepository() {
        initEmployeeData();
    }

    public List<Employee> findAll() {
        return this.employees;
    }

    private void initEmployeeData() {
        this.employees.add(new Employee(1, "John Smith", 32, Gender.MALE, 5000.0));
        this.employees.add(new Employee(2, "Jane Johnson", 28, Gender.FEMALE, 6000.0));
        this.employees.add(new Employee(3, "David Williams", 35, Gender.MALE, 5500.0));
        this.employees.add(new Employee(4, "Emily Brown", 23, Gender.FEMALE, 4500.0));
        this.employees.add(new Employee(5, "Michael Jones", 40, Gender.MALE, 7000.0));
    }

    public Employee findById(Integer id) {
        return employees.stream()
            .filter(employee -> Objects.equals(employee.getId(), id))
            .findFirst()
            .orElse(null);
    }

    public List<Employee> findAllByGender(Gender gender) {
        return employees.stream()
            .filter(employee -> employee.getGender().equals(gender))
            .collect(Collectors.toList());
    }

    public Employee create(Employee employee) {
        final Employee newEmployee = new Employee(
            this.findAll().size() + 1,
            employee.getName(),
            employee.getAge(),
            employee.getGender(),
            employee.getSalary());

        employees.add(newEmployee);
        return newEmployee;
    }

    public Employee update(Integer id, Employee employee) {
        return employees.stream()
            .filter(storedEmployee -> storedEmployee.getId().equals(id))
            .findFirst()
            .map(storedEmployee -> updateEmployeeAttributes(storedEmployee, employee))
            .orElse(null);
    }

    private Employee updateEmployeeAttributes(Employee employeeStored, Employee newEmployee) {
        if (newEmployee.getName() != null) {
            employeeStored.setName(newEmployee.getName());
        }
        if (newEmployee.getAge() != null) {
            employeeStored.setAge(newEmployee.getAge());
        }
        if (newEmployee.getGender() != null) {
            employeeStored.setGender(newEmployee.getGender());
        }
        if (newEmployee.getSalary() != null) {
            employeeStored.setSalary(newEmployee.getSalary());
        }
        return employeeStored;
    }

    public void deleteById(Integer id) {
        employees.removeIf(employee -> Objects.equals(employee.getId(), id));
    }

    public List<Employee> findAllByPage(Integer pageIndex, Integer pageSize) {
        return employees.stream()
            .skip((long) (pageIndex - 1) * pageSize)
            .limit(pageSize)
            .toList();
    }
}
