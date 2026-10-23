package com.expensetracker.infrastructure.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expensetracker.application.exception.CategoryAlreadyExistsException;
import com.expensetracker.application.port.in.CreateCategoryUseCase;
import com.expensetracker.application.port.in.ListCategoriesUseCase;
import com.expensetracker.domain.exception.InvalidCategoryException;
import com.expensetracker.domain.model.Category;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.CannotCreateTransactionException;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CreateCategoryUseCase createCategoryUseCase;
  @MockitoBean private ListCategoriesUseCase listCategoriesUseCase;

  @Test
  void shouldReturnAllCategoriesOnGet() throws Exception {
    Category category = Category.of("Alimentación");
    given(listCategoriesUseCase.execute()).willReturn(List.of(category));

    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("Alimentación"));
  }

  @Test
  void shouldCreateCategoryAndReturn201() throws Exception {
    Category created = Category.of("Ocio");
    given(createCategoryUseCase.execute("Ocio")).willReturn(created);

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ocio\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Ocio"))
        .andExpect(jsonPath("$.id").value(created.id().toString()));
  }

  @Test
  void shouldReturn400WithFieldErrorsWhenNameIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Request body failed validation"))
        .andExpect(jsonPath("$.fieldErrors.name").exists());
  }

  @Test
  void shouldReturn400WhenNameExceedsColumnLength() throws Exception {
    String tooLong = "A".repeat(256);

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + tooLong + "\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenCreateUseCaseThrowsInvalidCategoryException() throws Exception {
    given(createCategoryUseCase.execute(any())).willThrow(new InvalidCategoryException("bad"));

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn409WhenCategoryAlreadyExists() throws Exception {
    given(createCategoryUseCase.execute(any()))
        .willThrow(new CategoryAlreadyExistsException("Category already exists: Ocio"));

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ocio\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Category already exists: Ocio"));
  }

  @Test
  void shouldReturnProblemDetailWhenUseCaseThrowsAnUnexpectedException() throws Exception {
    given(listCategoriesUseCase.execute()).willThrow(new IllegalStateException("db is down"));

    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
  }

  @ParameterizedTest
  @MethodSource("databaseUnavailableExceptions")
  void shouldReturn503WhenTheDatabaseIsNotAvailable(RuntimeException databaseDown)
      throws Exception {
    given(listCategoriesUseCase.execute()).willThrow(databaseDown);

    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("The database is not available right now"));
  }

  static Stream<RuntimeException> databaseUnavailableExceptions() {
    return Stream.of(
        new DataAccessResourceFailureException("Unable to acquire JDBC Connection"),
        new CannotCreateTransactionException("Could not open JPA EntityManager for transaction"));
  }

  @Test
  void shouldReturnProblemDetailWhenRequestBodyIsMalformedJson() throws Exception {
    mockMvc
        .perform(
            post("/api/categories").contentType(MediaType.APPLICATION_JSON).content("{\"name\":}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
  }
}
