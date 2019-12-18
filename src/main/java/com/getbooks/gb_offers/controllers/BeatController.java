package com.getbooks.gb_offers.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BeatController {

    @GetMapping("/beat")
    public ResponseEntity<Object> liveBeat() {
        return ResponseEntity.ok().build();
    }
}
