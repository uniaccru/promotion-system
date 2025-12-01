package com.grading.repository;

import com.grading.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(Long userId);
    List<Employee> findByDepartment(String department);
    List<Employee> findByRole(String role);
    
    @Query("SELECT e FROM Employee e WHERE e.reviewPeriod = :period")
    List<Employee> findByReviewPeriod(@Param("period") String period);
}