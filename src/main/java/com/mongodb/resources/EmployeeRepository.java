package com.mongodb.resources;

import com.mongodb.domain.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {

    List<Employee> findByAgeGreaterThan(int age);
    Employee findBySsn(int ssn);
    List<Employee> findBySalaryGreaterThan(double salary);

    Employee findByPin(String pin);
}
