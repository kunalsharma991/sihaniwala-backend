package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.entity.Volunteer;
import com.sihaniwala.foundationbackend.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerRepository volunteerRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Volunteer>> submitVolunteer(@RequestBody Volunteer volunteer) {
        Volunteer saved = volunteerRepository.save(volunteer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Volunteer application submitted", saved));
    }
}
