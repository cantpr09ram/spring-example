package com.example.demo.greeting.service;

import com.example.demo.greeting.api.dto.GreetingResponse;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

  public GreetingResponse greet(String name) {
    return new GreetingResponse("Hello " + name + "!");
  }
}
