package com.example.employeeai.dto;
import com.example.employeeai.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record EmployeeRequest(
 String employeeCode,@NotBlank String firstName,@NotBlank String lastName,@Email @NotBlank String email,
 String phone,String jobTitle,String department,EmploymentType employmentType,EmployeeStatus status,
 LocalDate hireDate,String location,String managerName,String skills,@PositiveOrZero BigDecimal salary) {}
