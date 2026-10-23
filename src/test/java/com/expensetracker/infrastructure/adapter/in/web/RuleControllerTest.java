package com.expensetracker.infrastructure.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expensetracker.application.exception.CategoryNotFoundException;
import com.expensetracker.application.exception.RuleNotFoundException;
import com.expensetracker.application.port.in.CreateRuleUseCase;
import com.expensetracker.application.port.in.DeleteRuleUseCase;
import com.expensetracker.application.port.in.ListRulesUseCase;
import com.expensetracker.domain.model.Rule;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RuleController.class)
class RuleControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CreateRuleUseCase createRuleUseCase;
  @MockitoBean private ListRulesUseCase listRulesUseCase;
  @MockitoBean private DeleteRuleUseCase deleteRuleUseCase;

  @Test
  void shouldReturnAllRulesOnGet() throws Exception {
    UUID categoryId = UUID.randomUUID();
    Rule rule = Rule.of("MERCADONA", categoryId, 1);
    given(listRulesUseCase.execute()).willReturn(List.of(rule));

    mockMvc
        .perform(get("/api/rules"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].pattern").value("MERCADONA"))
        .andExpect(jsonPath("$[0].priority").value(1));
  }

  @Test
  void shouldCreateRuleAndReturn201() throws Exception {
    UUID categoryId = UUID.randomUUID();
    Rule created = Rule.of("NETFLIX", categoryId, 2);
    given(createRuleUseCase.execute("Netflix", categoryId, 2)).willReturn(created);

    mockMvc
        .perform(
            post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"pattern":"Netflix","categoryId":"%s","priority":2}
                    """
                        .formatted(categoryId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.pattern").value("NETFLIX"));
  }

  @Test
  void shouldReturn400WhenPatternIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"pattern":"","categoryId":"%s","priority":1}
                    """
                        .formatted(UUID.randomUUID())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenPatternExceedsColumnLength() throws Exception {
    String tooLong = "A".repeat(256);

    mockMvc
        .perform(
            post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"pattern":"%s","categoryId":"%s","priority":1}
                    """
                        .formatted(tooLong, UUID.randomUUID())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenPriorityIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"pattern":"NETFLIX","categoryId":"%s"}
                    """
                        .formatted(UUID.randomUUID())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn404WhenCreatingRuleWithUnknownCategory() throws Exception {
    given(createRuleUseCase.execute(any(), any(), anyInt()))
        .willThrow(new CategoryNotFoundException(UUID.randomUUID()));

    mockMvc
        .perform(
            post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"pattern":"NETFLIX","categoryId":"%s","priority":1}
                    """
                        .formatted(UUID.randomUUID())))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn204OnSuccessfulDelete() throws Exception {
    UUID ruleId = UUID.randomUUID();

    mockMvc.perform(delete("/api/rules/{id}", ruleId)).andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn404WhenDeletingUnknownRule() throws Exception {
    UUID ruleId = UUID.randomUUID();
    willThrow(new RuleNotFoundException(ruleId)).given(deleteRuleUseCase).execute(ruleId);

    mockMvc.perform(delete("/api/rules/{id}", ruleId)).andExpect(status().isNotFound());
  }
}
