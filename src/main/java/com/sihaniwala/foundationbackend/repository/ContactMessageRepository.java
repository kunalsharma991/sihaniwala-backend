package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {}
