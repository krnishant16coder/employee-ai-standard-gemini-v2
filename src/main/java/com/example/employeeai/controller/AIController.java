package com.example.employeeai.controller;

import com.example.employeeai.ai.GeminiAssistantService;
import com.example.employeeai.dto.AIAnswerResponse;
import com.example.employeeai.dto.AIQuestionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIController {

 private final GeminiAssistantService assistantService;

 @PostMapping("/ask")
 public AIAnswerResponse ask(
         @Valid @RequestBody AIQuestionRequest request
 ) {
  return assistantService.ask(
          request.question()
  );
 }
}