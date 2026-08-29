package com.example.employeeai.controller;
import org.springframework.web.bind.annotation.*; import java.time.OffsetDateTime; import java.util.*;
@RestController public class HealthController{
 @GetMapping("/api/health") public Map<String,Object> health(){return Map.of("status","UP","service","employee-ai-standard","timestamp",OffsetDateTime.now().toString());}
}
