package com.getbooks.gb_offers.controllers;

import com.getbooks.gb_offers.models.BookRequestBody;
import com.getbooks.gb_offers.models.CalculatedOffersResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OffersController {

    @PostMapping("/calculate")
    public CalculatedOffersResponseBody receiveLoadOfBooks(@RequestBody BookRequestBody requestBody) {
        CalculatedOffersResponseBody calculated = new CalculatedOffersResponseBody();
        return calculated;
    }
}
