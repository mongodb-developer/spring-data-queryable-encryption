package com.mongodb.resources;

import com.mongodb.domain.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public interface EmployeeRepository extends MongoRepository<Employee, String> {

    List<Employee> findByAgeGreaterThan(int age);

}
