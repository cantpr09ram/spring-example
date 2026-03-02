package com.example.demo.sum.service;

import org.springframework.stereotype.Service;

@Service
public class SumService {
    public long add(long a, long b) {
        return Math.addExact(a, b); // 溢位會丟 ArithmeticException
    }
}
