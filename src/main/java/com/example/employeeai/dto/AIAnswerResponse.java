package com.example.employeeai.dto;
import java.util.List;
public record AIAnswerResponse(String answer,List<String> toolsUsed) {}
