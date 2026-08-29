package com.example.employeeai.exception;
import org.springframework.http.*; import org.springframework.web.bind.*; import org.springframework.web.bind.annotation.*; import java.time.OffsetDateTime; import java.util.*;
@RestControllerAdvice public class GlobalExceptionHandler{
 @ExceptionHandler(MethodArgumentNotValidException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public Map<String,Object> validation(MethodArgumentNotValidException e){
  Map<String,String> fields=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->fields.put(x.getField(),x.getDefaultMessage()));
  return Map.of("timestamp",OffsetDateTime.now(),"status",400,"error","Validation failed","fields",fields);
 }
 @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
 public Map<String,Object> general(Exception e){return Map.of("timestamp",OffsetDateTime.now(),"status",500,"error",e.getMessage()==null?e.getClass().getSimpleName():e.getMessage());}
}
