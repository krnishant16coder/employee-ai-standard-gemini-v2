package com.example.employeeai;
import com.example.employeeai.dto.EmployeeRequest;
import com.example.employeeai.repository.EmployeeRepository;
import com.example.employeeai.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class EmployeeServiceTest {
 @Autowired EmployeeService service; @Autowired EmployeeRepository repository;
 @Test void crudWorks(){
  String email="test-"+System.nanoTime()+"@example.com";
  var created=service.create(new EmployeeRequest(null,"Test","User",email,"+911234567890","Developer","Engineering",null,null,null,"Remote","Manager","Java",BigDecimal.valueOf(50000)));
  assertNotNull(created.id()); assertNotNull(created.employeeCode()); assertEquals("Test",created.firstName());
  assertEquals(created.email(),service.findById(created.id()).email());
  service.delete(created.id()); assertFalse(repository.existsById(created.id()));
 }
}
