package com.example.demo.service;

import com.example.demo.dto.GreetingResponse;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

  public GreetingResponse greet(String name) {
    return new GreetingResponse("Hello " + name + "!");
  }
}