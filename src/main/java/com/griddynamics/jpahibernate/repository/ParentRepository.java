package com.griddynamics.jpahibernate.repository;

import com.griddynamics.jpahibernate.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
}
