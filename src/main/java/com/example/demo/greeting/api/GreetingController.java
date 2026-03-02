package com.example.demo.greeting.api;

import com.example.demo.greeting.api.dto.GreetingResponse;
import com.example.demo.greeting.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

  private final GreetingService greetingService;

  public GreetingController(GreetingService greetingService) {
    this.greetingService = greetingService;
  }

  @GetMapping("/hello")
  public GreetingResponse hello(@RequestParam(value = "name", defaultValue = "World") String name) {
    return greetingService.greet(name);
  }
}
