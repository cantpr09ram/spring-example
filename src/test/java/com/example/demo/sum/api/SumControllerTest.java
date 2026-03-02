package com.example.demo.sum.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.sum.exception.GlobalExceptionHandler;
import com.example.demo.sum.service.SumService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SumController.class)
@Import(GlobalExceptionHandler.class)
class SumControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SumService sumService;

  @Test
  void addReturnsResult() throws Exception {
    when(sumService.add(1L, 2L)).thenReturn(3L);

    mockMvc
        .perform(get("/api/v1/add").param("a", "1").param("b", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.a").value(1))
        .andExpect(jsonPath("$.b").value(2))
        .andExpect(jsonPath("$.result").value(3));
  }

  @Test
  void addReturnsBadRequestOnOverflow() throws Exception {
    when(sumService.add(Long.MAX_VALUE, 1L)).thenThrow(new ArithmeticException("overflow"));

    mockMvc
        .perform(get("/api/v1/add").param("a", Long.toString(Long.MAX_VALUE)).param("b", "1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.path").value("/api/v1/add"))
        .andExpect(jsonPath("$.timestamp").isNotEmpty())
        .andExpect(jsonPath("$.message").value("number overflow"));
  }

  @Test
  void addReturnsBadRequestWhenParameterMissing() throws Exception {
    mockMvc
        .perform(get("/api/v1/add").param("b", "2"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.path").value("/api/v1/add"))
        .andExpect(jsonPath("$.message").value("a is required"));
  }

  @Test
  void addReturnsBadRequestWhenSecondParameterMissing() throws Exception {
    mockMvc
        .perform(get("/api/v1/add").param("a", "2"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("b is required"));
  }

  @Test
  void addReturnsBadRequestWhenParameterTypeInvalid() throws Exception {
    mockMvc
        .perform(get("/api/v1/add").param("a", "abc").param("b", "2"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("a must be a valid number"));
  }

  @Test
  void addReturnsBadRequestWhenSecondParameterTypeInvalid() throws Exception {
    mockMvc
        .perform(get("/api/v1/add").param("a", "1").param("b", "xyz"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("b must be a valid number"));
  }
}
