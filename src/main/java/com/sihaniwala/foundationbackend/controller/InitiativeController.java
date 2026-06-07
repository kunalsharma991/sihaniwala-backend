package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.entity.InitiativeApplication;
import com.sihaniwala.foundationbackend.entity.InitiativeApplication.*;
import com.sihaniwala.foundationbackend.service.InitiativeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/initiatives")
@RequiredArgsConstructor
public class InitiativeController {

    private final InitiativeService initiativeService;

    @PostMapping("/hospital")
    public ResponseEntity<ApiResponse<InitiativeApplication>> submitHospital(
            @RequestBody Map<String, String> body, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        InitiativeApplication app = initiativeService.submitApplication(
                InitiativeType.HOSPITAL, body.get("formData"), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application submitted", app));
    }

    @PostMapping("/marriage-support")
    public ResponseEntity<ApiResponse<InitiativeApplication>> submitMarriage(
            @RequestBody Map<String, String> body, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        InitiativeApplication app = initiativeService.submitApplication(
                InitiativeType.MARRIAGE_SUPPORT, body.get("formData"), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application submitted", app));
    }

    @PostMapping("/water-spray")
    public ResponseEntity<ApiResponse<InitiativeApplication>> submitWaterSpray(
            @RequestBody Map<String, String> body, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        InitiativeApplication app = initiativeService.submitApplication(
                InitiativeType.WATER_SPRAY, body.get("formData"), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application submitted", app));
    }

    @PostMapping("/education-bpl")
    public ResponseEntity<ApiResponse<InitiativeApplication>> submitEducation(
            @RequestBody Map<String, String> body, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        InitiativeApplication app = initiativeService.submitApplication(
                InitiativeType.EDUCATION_BPL, body.get("formData"), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application submitted", app));
    }

    @PostMapping("/financial-help")
    public ResponseEntity<ApiResponse<InitiativeApplication>> submitFinancial(
            @RequestBody Map<String, String> body, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        InitiativeApplication app = initiativeService.submitApplication(
                InitiativeType.FINANCIAL_HELP, body.get("formData"), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application submitted", app));
    }

    @PostMapping("/school-adoption")
    public ResponseEntity<ApiResponse<InitiativeApplication>> submitSchoolAdoption(
            @RequestBody Map<String, String> body, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        InitiativeApplication app = initiativeService.submitApplication(
                InitiativeType.SCHOOL_ADOPTION, body.get("formData"), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application submitted", app));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InitiativeApplication>> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(initiativeService.getApplication(id)));
    }
}
