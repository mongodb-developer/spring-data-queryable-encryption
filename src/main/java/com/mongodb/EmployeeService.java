package com.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final MongoOperations mongoOperations;
    private final EmployeeRepository employeeRepository;

    public EmployeeService(MongoOperations mongoOperations, EmployeeRepository employeeRepository) {
        this.mongoOperations = mongoOperations;
        this.employeeRepository = employeeRepository;
    }

    public Employee createEmployee(Employee employee) {
        logger.info("Creating employee {}", employee);
         return mongoOperations.save(employee);
     }

    public List<Employee> findAll() {
       logger.info("Finding all employee ");
       return mongoOperations.findAll(Employee.class);
    }

    public List<Employee> findBySalaryGreaterThan(double amount) {
        logger.info("<MONGO TEMPLATE> Finding all employee where salary is greater than {} ", amount);
        return mongoOperations.find(new Query().addCriteria(Criteria.where("salary").gt(amount)), Employee.class);
    }

    public List<Employee> findBySalaryLessThan(double amount) {
        logger.info("<MONGO REPOSITORY> Finding all employee where salary is less than {} ", amount);
        return employeeRepository.findBySalaryLessThan(amount);
    }
}


