package com.example.employeeai.service;
import com.example.employeeai.dto.*;
import com.example.employeeai.entity.*;
import com.example.employeeai.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class EmployeeService {
 private final EmployeeRepository repository;
 @Transactional(readOnly=true) public List<EmployeeResponse> findAll(){return repository.findAll().stream().map(this::toResponse).toList();}
 @Transactional(readOnly=true) public EmployeeResponse findById(Long id){return toResponse(getEntity(id));}
 @Transactional(readOnly=true) public List<EmployeeResponse> search(String q){
  if(q==null||q.isBlank()) return findAll();
  return repository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrEmployeeCodeContainingIgnoreCase(q,q,q,q)
   .stream().map(this::toResponse).toList();
 }
 @Transactional public EmployeeResponse create(EmployeeRequest r){
  String code=(r.employeeCode()==null||r.employeeCode().isBlank())?generateEmployeeCode():r.employeeCode();
  repository.findByEmailIgnoreCase(r.email()).ifPresent(e->{throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");});
  repository.findByEmployeeCodeIgnoreCase(code).ifPresent(e->{throw new ResponseStatusException(HttpStatus.CONFLICT,"Employee code already exists");});
  Employee e=fromRequest(new Employee(),r); e.setEmployeeCode(code);
  if(e.getStatus()==null)e.setStatus(EmployeeStatus.ACTIVE);
  return toResponse(repository.save(e));
 }
 @Transactional public EmployeeResponse update(Long id,EmployeeRequest r){
  Employee e=getEntity(id);
  repository.findByEmailIgnoreCase(r.email()).filter(x->!Objects.equals(x.getId(),id))
   .ifPresent(x->{throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");});
  if(r.employeeCode()!=null&&!r.employeeCode().isBlank()){
   repository.findByEmployeeCodeIgnoreCase(r.employeeCode()).filter(x->!Objects.equals(x.getId(),id))
    .ifPresent(x->{throw new ResponseStatusException(HttpStatus.CONFLICT,"Employee code already exists");});
   e.setEmployeeCode(r.employeeCode());
  }
  fromRequest(e,r); return toResponse(repository.save(e));
 }
 @Transactional public void delete(Long id){repository.delete(getEntity(id));}
 @Transactional(readOnly=true) public EmployeeSummaryResponse summary(){
  List<Employee> es=repository.findAll();
  long active=es.stream().filter(e->e.getStatus()==EmployeeStatus.ACTIVE).count();
  long inactive=es.stream().filter(e->e.getStatus()==EmployeeStatus.INACTIVE).count();
  long leave=es.stream().filter(e->e.getStatus()==EmployeeStatus.ON_LEAVE).count();
  BigDecimal sum=es.stream().map(Employee::getSalary).filter(Objects::nonNull).reduce(BigDecimal.ZERO,BigDecimal::add);
  long count=es.stream().filter(e->e.getSalary()!=null).count();
  BigDecimal avg=count==0?BigDecimal.ZERO:sum.divide(BigDecimal.valueOf(count),2,RoundingMode.HALF_UP);
  Map<String,Long> byDept=es.stream().collect(Collectors.groupingBy(e->e.getDepartment()==null||e.getDepartment().isBlank()?"Unassigned":e.getDepartment(),Collectors.counting()));
  return new EmployeeSummaryResponse(es.size(),active,inactive,leave,avg,byDept);
 }
 private Employee fromRequest(Employee e,EmployeeRequest r){
  e.setFirstName(r.firstName());e.setLastName(r.lastName());e.setEmail(r.email());e.setPhone(r.phone());
  e.setJobTitle(r.jobTitle());e.setDepartment(r.department());e.setEmploymentType(r.employmentType());e.setStatus(r.status());
  e.setHireDate(r.hireDate());e.setLocation(r.location());e.setManagerName(r.managerName());e.setSkills(r.skills());e.setSalary(r.salary());return e;
 }
 private String generateEmployeeCode(){long n=repository.count()+1;String c;do{c=String.format("EMP-%05d",n++);}while(repository.findByEmployeeCodeIgnoreCase(c).isPresent());return c;}
 private Employee getEntity(Long id){return repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee not found: "+id));}
 private EmployeeResponse toResponse(Employee e){return new EmployeeResponse(e.getId(),e.getEmployeeCode(),e.getFirstName(),e.getLastName(),e.getEmail(),e.getPhone(),e.getJobTitle(),e.getDepartment(),e.getEmploymentType(),e.getStatus(),e.getHireDate(),e.getLocation(),e.getManagerName(),e.getSkills(),e.getSalary());}
}
