package com.example.fullstack.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoRequest(
        @NotBlank @Size(max = 160) String title,
        Boolean completed
) {
}
