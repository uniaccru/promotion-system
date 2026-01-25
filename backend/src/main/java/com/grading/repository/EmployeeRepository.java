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
    
    @Query("SELECT e FROM Employee e WHERE e.department = :department ORDER BY e.createdAt DESC")
    List<Employee> findByDepartment(@Param("department") String department);
    
    @Query("SELECT e FROM Employee e WHERE e.role = :role ORDER BY e.createdAt DESC")
    List<Employee> findByRole(@Param("role") String role);
    
    @Query("SELECT e FROM Employee e WHERE e.reviewPeriod = :period ORDER BY e.createdAt DESC")
    List<Employee> findByReviewPeriod(@Param("period") String period);
    
    @Query("SELECT e FROM Employee e ORDER BY e.createdAt DESC")
    @Override
    List<Employee> findAll();
}