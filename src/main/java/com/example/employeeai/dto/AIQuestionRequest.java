package com.example.employeeai.dto;
import jakarta.validation.constraints.NotBlank;
public record AIQuestionRequest(@NotBlank String question) {}
