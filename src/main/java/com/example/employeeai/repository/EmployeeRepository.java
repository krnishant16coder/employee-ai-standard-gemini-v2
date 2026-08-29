package com.example.employeeai.repository;
import com.example.employeeai.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EmployeeRepository extends JpaRepository<Employee,Long>{
 Optional<Employee> findByEmailIgnoreCase(String email);
 Optional<Employee> findByEmployeeCodeIgnoreCase(String employeeCode);
 List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrEmployeeCodeContainingIgnoreCase(
  String firstName,String lastName,String email,String employeeCode);
}
