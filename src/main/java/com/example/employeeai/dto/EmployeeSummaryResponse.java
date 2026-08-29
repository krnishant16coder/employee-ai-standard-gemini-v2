package com.example.employeeai.dto;
import java.math.BigDecimal;
import java.util.Map;
public record EmployeeSummaryResponse(long totalEmployees,long activeEmployees,long inactiveEmployees,
 long onLeaveEmployees,BigDecimal averageSalary,Map<String,Long> employeesByDepartment) {}
