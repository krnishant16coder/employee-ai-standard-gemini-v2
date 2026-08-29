package com.example.employeeai.dto;
import com.example.employeeai.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record EmployeeResponse(
 Long id,String employeeCode,String firstName,String lastName,String email,String phone,String jobTitle,
 String department,EmploymentType employmentType,EmployeeStatus status,LocalDate hireDate,String location,
 String managerName,String skills,BigDecimal salary) {}
