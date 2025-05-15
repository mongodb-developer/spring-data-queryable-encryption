package com.mongodb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
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

    @GetMapping("/salaryLessThan")
    public ResponseEntity<List<Employee>> findBySalaryLessThan(@RequestParam double amount) {
        return ResponseEntity.ok(employeeService.findBySalaryLessThan(amount));
    }

    @GetMapping("/salaryGreaterThan")
    public ResponseEntity<List<Employee>> salaryGreaterThan(@RequestParam double amount) {
        return ResponseEntity.ok(employeeService.findBySalaryGreaterThan(amount));
    }
}
