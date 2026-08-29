package com.example.employeeai.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="employees",indexes={
 @Index(name="idx_employee_department",columnList="department"),
 @Index(name="idx_employee_status",columnList="status"),
 @Index(name="idx_employee_job_title",columnList="job_title")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotBlank @Column(name="employee_code",nullable=false,unique=true,length=30) private String employeeCode;
 @NotBlank @Column(nullable=false,length=80) private String firstName;
 @NotBlank @Column(nullable=false,length=80) private String lastName;
 @Email @NotBlank @Column(nullable=false,unique=true,length=180) private String email;
 @Column(length=30) private String phone;
 @Column(name="job_title",length=120) private String jobTitle;
 @Column(length=100) private String department;
 @Enumerated(EnumType.STRING) @Column(name="employment_type",length=20) private EmploymentType employmentType;
 @Enumerated(EnumType.STRING) @Column(length=20) private EmployeeStatus status;
 private LocalDate hireDate;
 @Column(length=120) private String location;
 @Column(name="manager_name",length=160) private String managerName;
 @Column(length=1000) private String skills;
 @PositiveOrZero @Column(precision=14,scale=2) private BigDecimal salary;
}
