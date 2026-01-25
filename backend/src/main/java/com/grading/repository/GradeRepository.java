package com.grading.repository;

import com.grading.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByName(String name);
    
    @Query("SELECT g FROM Grade g ORDER BY g.id DESC")
    @Override
    List<Grade> findAll();
}