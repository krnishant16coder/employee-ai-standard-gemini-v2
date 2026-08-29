package com.example.employeeai.controller;
import com.example.employeeai.dto.*; import com.example.employeeai.service.EmployeeService;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/employees") @RequiredArgsConstructor @CrossOrigin(origins="*")
public class EmployeeController{
 private final EmployeeService service;
 @GetMapping public List<EmployeeResponse> getAll(){return service.findAll();}
 @GetMapping("/summary") public EmployeeSummaryResponse summary(){return service.summary();}
 @GetMapping("/{id}") public EmployeeResponse getById(@PathVariable Long id){return service.findById(id);}
 @GetMapping("/search") public List<EmployeeResponse> search(@RequestParam String q){return service.search(q);}
 @PostMapping public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));}
 @PutMapping("/{id}") public EmployeeResponse update(@PathVariable Long id,@Valid @RequestBody EmployeeRequest r){return service.update(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){service.delete(id);}
}
