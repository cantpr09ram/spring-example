package com.example.demo.sum.api;

import com.example.demo.sum.api.dto.SumResponse;
import com.example.demo.sum.service.SumService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SumController {

  private final SumService sumService;

  public SumController(SumService sumService) {
    this.sumService = sumService;
  }

  @GetMapping("/api/v1/add")
  public SumResponse add(
      @RequestParam("a") long a,
      @RequestParam("b") long b
  ) {
    long result = sumService.add(a, b);
    return new SumResponse(a, b, result);
  }
}
