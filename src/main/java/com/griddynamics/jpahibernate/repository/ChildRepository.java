package com.griddynamics.jpahibernate.repository;

import com.griddynamics.jpahibernate.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {

}
