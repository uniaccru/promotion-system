package com.grading.repository;

import com.grading.entity.GradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeHistoryRepository extends JpaRepository<GradeHistory, Long> {
    List<GradeHistory> findByEmployeeIdOrderByChangedAtDesc(Long employeeId);
    
    @Query("SELECT gh FROM GradeHistory gh WHERE gh.employee.id = :employeeId ORDER BY gh.changedAt DESC LIMIT 1")
    Optional<GradeHistory> findLatestByEmployeeId(@Param("employeeId") Long employeeId);
}