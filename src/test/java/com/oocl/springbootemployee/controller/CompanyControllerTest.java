package com.oocl.springbootemployee.controller;

import com.oocl.springbootemployee.model.Company;
import com.oocl.springbootemployee.model.Employee;
import com.oocl.springbootemployee.model.Gender;
import com.oocl.springbootemployee.repository.CompanyRepository;
import com.oocl.springbootemployee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
class CompanyControllerTest {
    @Autowired
    private MockMvc client;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JacksonTester<List<Company>> companyListJacksonTester;
    @Autowired
    private JacksonTester<List<Employee>> employeeListJacksonTester;
    private Employee john_smith;
    private Employee jane_johnson;
    private Employee david_williams;
    private Employee emily_brown;
    private Employee michael_jones;
    private Company acme_corporation;
    private Company techcom_solutions;
    private Company stellar_enterprises;
    private Company global_innovators;
    private Company nexus_industries;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();
        companyRepository.flush();
        employeeRepository.deleteAll();
        employeeRepository.flush();

        john_smith = new Employee("John Smith", 32, Gender.MALE, 5000.0);
        jane_johnson = new Employee("Jane Johnson", 28, Gender.FEMALE, 6000.0);
        david_williams = new Employee("David Williams", 35, Gender.MALE, 5500.0);
        emily_brown = new Employee("Emily Brown", 23, Gender.FEMALE, 4500.0);
        michael_jones = new Employee("Michael Jones", 40, Gender.MALE, 7000.0);
        acme_corporation = companyRepository.save(new Company("Acme Corporation", List.of(john_smith, jane_johnson)));
        techcom_solutions = companyRepository.save(new Company("TechCom Solutions", List.of(david_williams, emily_brown, michael_jones)));
        global_innovators = companyRepository.save(new Company("Global Innovators"));
        stellar_enterprises = companyRepository.save(new Company("Stellar Enterprises"));
        nexus_industries = companyRepository.save(new Company("Nexus Industries"));
    }

    @Test
    void should_return_all_companies() throws Exception {
        // Given
        final List<Company> givenCompanies = companyRepository.findAll();

        // When
        final MvcResult result = client.perform(MockMvcRequestBuilders.get("/companies")).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        final List<Company> fetchedCompanies = companyListJacksonTester.parseObject(result.getResponse().getContentAsString());
        assertThat(fetchedCompanies).hasSameSizeAs(givenCompanies);
        assertThat(fetchedCompanies)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(givenCompanies);
    }

    @Test
    void should_return_paged_companies_when_get_by_page_params() throws Exception {
        // Given
        var pageIndex = 2;
        var pageSize = 2;
        final var the5thEmployeeCompanyInPage3 = companyRepository.findById(nexus_industries.getId());

        // When
        // Then
        client.perform(MockMvcRequestBuilders.get(String.format("/companies?pageIndex=%s&pageSize=%s", pageIndex + 1, pageSize)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(the5thEmployeeCompanyInPage3.get().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(the5thEmployeeCompanyInPage3.get().getName()));
    }

    @Test
    void should_return_employees_when_get_employees_under_the_company() throws Exception {
        // Given
        var givenCompanyId = acme_corporation.getId();

        // When
        final var result =
                client.perform(MockMvcRequestBuilders.get("/companies/" + givenCompanyId + "/employees")).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        final List<Employee> fetchedEmployees =
                employeeListJacksonTester.parseObject(result.getResponse().getContentAsString());

        assertThat(fetchedEmployees).hasSize(2);
        assertThat(fetchedEmployees.stream().map(Employee::getId).toList())
                .containsAll(List.of(john_smith.getId(), jane_johnson.getId()));
    }

    @Test
    void should_return_company_when_get_by_id() throws Exception {
        // Given

        final var companyGiven = companyRepository.findAll().get(0);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.get("/companies/" + companyGiven.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(companyGiven.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(companyGiven.getName()));
    }

    @Test
    void should_return_created_company() throws Exception {
        //Given
        var givenName = "New Company";
        String requestBody = String.format("{\"name\": \"%s\" }", givenName);

        // When
        // Then
        client.perform(
                        MockMvcRequestBuilders.post("/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(givenName));
    }

    @Test
    void should_return_updated_company_when_update_with_id_and_data() throws Exception {
        // Given
        var idToUpdate = techcom_solutions.getId();
        var nameToUpdate = "New Name";
        String requestBody = String.format("{\"name\": \"%s\" }", nameToUpdate);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.put("/companies/" + idToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(idToUpdate))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(nameToUpdate));
    }

    @Test
    void should_return_no_content_when_delete() throws Exception {
        // Given
        var toDeleteCompanyId = nexus_industries.getId();

        // When
        final var result =
                client.perform(MockMvcRequestBuilders.delete("/companies/" + toDeleteCompanyId)).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(companyRepository.findAll()).hasSize(4);
    }
}
