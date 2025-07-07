package com.mongodb.application;

import com.mongodb.domain.Employee;
import com.mongodb.domain.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
         this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.createEmployee(employee));
    }

    @GetMapping
    public ResponseEntity<List<Employee>> findAll() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/ages/greater-than")
    public ResponseEntity<List<Employee>> findByAgeGreaterThan(@RequestParam int age) {
        return ResponseEntity.ok(employeeService.findByAgeGreaterThan(age));
    }

    @GetMapping("/salary/greater-than")
    public ResponseEntity<List<Employee>> findByAgeGreaterThan(@RequestParam double salary) {
        return ResponseEntity.ok(employeeService.findBySalaryGreaterThan(salary));
    }
}
