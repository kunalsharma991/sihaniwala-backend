package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {}
