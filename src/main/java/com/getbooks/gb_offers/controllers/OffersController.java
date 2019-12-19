package com.getbooks.gb_offers.controllers;

import com.getbooks.gb_offers.models.BookRequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OffersController {

    @PostMapping("/calculate")
    public ResponseEntity<Object> receiveLoadOfBooks(@RequestBody BookRequestBody requestBody) {
        return ResponseEntity.ok().build();
    }
}
