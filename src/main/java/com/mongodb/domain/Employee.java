package com.mongodb.domain;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.RangeEncrypted;

public class Employee {
        @Id String id;
        String name;
        @RangeEncrypted(contentionFactor = 8, rangeOptions = "{ 'min' : 0, 'max' : 150 }")
        double salary;
        int age;

        public Employee() {}

        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public double getSalary() {
                return salary;
        }

        public void setSalary(double salary) {
                this.salary = salary;
        }

        public int getAge() {
                return age;
        }

        public void setAge(int age) {
                this.age = age;
        }
}
