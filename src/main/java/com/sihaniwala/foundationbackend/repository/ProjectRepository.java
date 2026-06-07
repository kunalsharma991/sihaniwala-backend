package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {}
