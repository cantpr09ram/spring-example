package com.example.demo.sum.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SumServiceTest {

  private final SumService sumService = new SumService();

  @Test
  void addReturnsSumForNormalValues() {
    long result = sumService.add(1L, 2L);

    assertEquals(3L, result);
  }

  @Test
  void addHandlesNegativeValues() {
    long result = sumService.add(-10L, 4L);

    assertEquals(-6L, result);
  }

  @Test
  void addThrowsArithmeticExceptionOnOverflow() {
    assertThrows(ArithmeticException.class, () -> sumService.add(Long.MAX_VALUE, 1L));
  }

  @Test
  void addThrowsArithmeticExceptionOnUnderflow() {
    assertThrows(ArithmeticException.class, () -> sumService.add(Long.MIN_VALUE, -1L));
  }
}
