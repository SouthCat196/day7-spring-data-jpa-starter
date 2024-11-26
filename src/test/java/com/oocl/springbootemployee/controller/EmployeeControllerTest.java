package com.oocl.springbootemployee.controller;

import com.oocl.springbootemployee.model.Employee;
import com.oocl.springbootemployee.model.Gender;
import com.oocl.springbootemployee.repository.EmployeeInMemoryRepository;
import com.oocl.springbootemployee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class EmployeeControllerTest {

    @Autowired
    private MockMvc client;

    @Autowired
    private EmployeeInMemoryRepository employeeInMemoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JacksonTester<Employee> employeeJacksonTester;

    @Autowired
    private JacksonTester<List<Employee>> employeesJacksonTester;

    @BeforeEach
    void setUp() {
        givenDataToJpaRepository();
        givenDataToInMemoryRepository();
    }

    private void givenDataToJpaRepository() {
        employeeRepository.deleteAll();
        employeeRepository.save(new Employee(null, "John Smith", 32, Gender.MALE, 5000.0));
        employeeRepository.save(new Employee(null, "Jane Johnson", 28, Gender.FEMALE, 6000.0));
        employeeRepository.save(new Employee(null, "David Williams", 35, Gender.MALE, 5500.0));
        employeeRepository.save(new Employee(null, "Emily Brown", 23, Gender.FEMALE, 4500.0));
        employeeRepository.save(new Employee(null, "Michael Jones", 40, Gender.MALE, 7000.0));
    }

    private void givenDataToInMemoryRepository() {
        employeeInMemoryRepository.findAll().clear();
        employeeInMemoryRepository.create(new Employee(1, "John Smith", 32, Gender.MALE, 5000.0));
        employeeInMemoryRepository.create(new Employee(2, "Jane Johnson", 28, Gender.FEMALE, 6000.0));
        employeeInMemoryRepository.create(new Employee(3, "David Williams", 35, Gender.MALE, 5500.0));
        employeeInMemoryRepository.create(new Employee(4, "Emily Brown", 23, Gender.FEMALE, 4500.0));
        employeeInMemoryRepository.create(new Employee(5, "Michael Jones", 40, Gender.MALE, 7000.0));
    }

    @Test
    void should_return_employees_when_get_all_given_employee_exist() throws Exception {
        //given
        final List<Employee> givenEmployees = employeeRepository.findAll();

        //when
        //then
        final String jsonResponse = client.perform(MockMvcRequestBuilders.get("/employees"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        final List<Employee> employeesResult = employeesJacksonTester.parseObject(jsonResponse);
        assertThat(employeesResult)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(givenEmployees);
    }

    @Test
    void should_return_employee_when_get_by_id() throws Exception {
        // Given
        final Employee givenEmployee = employeeRepository.findAll().get(0);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.get("/employees/{id}", givenEmployee.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(givenEmployee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(givenEmployee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(givenEmployee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value(givenEmployee.getGender().name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").value(givenEmployee.getSalary()));
    }


    @Test
    void should_return_employees_when_get_by_gender() throws Exception {
        // Given
        List<Employee> femaleEmployee = employeeRepository.getAllByGender(Gender.FEMALE);
        // When
        // Then
        client.perform(MockMvcRequestBuilders.get("/employees")
                        .param("gender", "FEMALE"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(employeesJacksonTester.write(femaleEmployee).getJson()));

    }

    @Test
    void should_create_employee_success() throws Exception {
        // Given
        employeeRepository.deleteAll();
        String givenName = "New Employee";
        Integer givenAge = 18;
        Gender givenGender = Gender.FEMALE;
        Double givenSalary = 5000.0;
        String givenEmployee = String.format(
                "{\"name\": \"%s\", \"age\": \"%s\", \"gender\": \"%s\", \"salary\": \"%s\"}",
                givenName,
                givenAge,
                givenGender,
                givenSalary
        );

        // When
        // Then
        client.perform(MockMvcRequestBuilders.post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(givenEmployee)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(givenName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(givenAge))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value(givenGender.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").value(givenSalary));
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(1);
        assertThat(employees.get(0).getName()).isEqualTo(givenName);
        assertThat(employees.get(0).getAge()).isEqualTo(givenAge);
        assertThat(employees.get(0).getGender()).isEqualTo(givenGender);
        assertThat(employees.get(0).getSalary()).isEqualTo(givenSalary);
    }

    @Test
    void should_update_employee_success() throws Exception {
        // Given
        final Employee givenEmployee = employeeRepository.findAll().get(0);
        givenEmployee.setSalary(9999999.0);
        String employeeJson = employeeJacksonTester.write(givenEmployee).getJson();

        // When
        // Then
        client.perform(MockMvcRequestBuilders.put("/employees/" + givenEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(givenEmployee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(givenEmployee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value(givenEmployee.getGender().name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").value(givenEmployee.getSalary()));
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(5);
        assertThat(employees.get(0).getId()).isEqualTo(givenEmployee.getId());
        assertThat(employees.get(0).getName()).isEqualTo(givenEmployee.getName());
        assertThat(employees.get(0).getAge()).isEqualTo(givenEmployee.getAge());
        assertThat(employees.get(0).getGender()).isEqualTo(givenEmployee.getGender());
        assertThat(employees.get(0).getSalary()).isEqualTo(givenEmployee.getSalary());
    }

    @Test
    void should_remove_employee_success() throws Exception {
        // Given
        Employee employee = employeeRepository.findAll().get(0);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.delete("/employees/" + employee.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(4);
        assertFalse(employeeRepository.findById(employee.getId()).isPresent());

    }

    @Test
    void should_return_employees_when_get_by_pageable() throws Exception {
        //given
        final List<Employee> givenEmployees = employeeRepository.findAll();

        //when
        //then
        client.perform(MockMvcRequestBuilders.get("/employees")
                        .param("pageIndex", "2")
                        .param("pageSize", "2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(givenEmployees.get(2).getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(givenEmployees.get(3).getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(givenEmployees.get(2).getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(givenEmployees.get(2).getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(givenEmployees.get(2).getGender().name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(givenEmployees.get(2).getSalary()));
    }
}