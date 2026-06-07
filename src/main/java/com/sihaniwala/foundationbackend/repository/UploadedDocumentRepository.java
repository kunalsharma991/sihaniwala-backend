package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {}
