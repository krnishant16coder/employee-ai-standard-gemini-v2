package com.example.employeeai.ai;
import com.example.employeeai.dto.*; import com.example.employeeai.entity.*; import com.example.employeeai.service.EmployeeService;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Component; import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;
@Component @RequiredArgsConstructor
public class EmployeeAiTools{
 private final EmployeeService service;
 public List<EmployeeResponse> listEmployees(){return service.findAll();}
 public EmployeeResponse getEmployee(Long id){return service.findById(id);}
 public List<EmployeeResponse> searchEmployees(String q){return service.search(q);}
 public EmployeeSummaryResponse employeeSummary(){return service.summary();}
 public EmployeeResponse createEmployee(String employeeCode,String firstName,String lastName,String email,String phone,String jobTitle,String department,EmploymentType employmentType,EmployeeStatus status,LocalDate hireDate,String location,String managerName,String skills,BigDecimal salary){
  return service.create(new EmployeeRequest(employeeCode,firstName,lastName,email,phone,jobTitle,department,employmentType,status,hireDate,location,managerName,skills,salary));
 }
 public EmployeeResponse updateEmployee(Long id,String employeeCode,String firstName,String lastName,String email,String phone,String jobTitle,String department,EmploymentType employmentType,EmployeeStatus status,LocalDate hireDate,String location,String managerName,String skills,BigDecimal salary){
  EmployeeResponse current=service.findById(id);
  return service.update(id,new EmployeeRequest(
   employeeCode!=null?employeeCode:current.employeeCode(),
   firstName!=null?firstName:current.firstName(),
   lastName!=null?lastName:current.lastName(),
   email!=null?email:current.email(),
   phone!=null?phone:current.phone(),
   jobTitle!=null?jobTitle:current.jobTitle(),
   department!=null?department:current.department(),
   employmentType!=null?employmentType:current.employmentType(),
   status!=null?status:current.status(),
   hireDate!=null?hireDate:current.hireDate(),
   location!=null?location:current.location(),
   managerName!=null?managerName:current.managerName(),
   skills!=null?skills:current.skills(),
   salary!=null?salary:current.salary()));
 }
 public Map<String,Object> deleteEmployee(Long id){service.delete(id);return Map.of("success",true,"deletedEmployeeId",id);}
}
