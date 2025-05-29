package com.mongodb.domain;

import com.mongodb.resources.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService  {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
         this.employeeRepository = employeeRepository;
    }

    public Employee createEmployee(Employee employee) {
        logger.info("Creating employee {}", employee);
         return employeeRepository.save(employee);
     }

    public List<Employee> findAll() {
       logger.info("Finding all employee ");
       return employeeRepository.findAll();
    }

    public List<Employee> findByAgeGreaterThan(int age) {
        logger.info("Finding all employee where age is greater than {} ", age);
        return employeeRepository.findByAgeGreaterThan(age);
    }
}


