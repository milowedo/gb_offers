package com.getbooks.gb_offers.controllers;

import com.getbooks.gb_offers.models.BookRequestBody;
import com.getbooks.gb_offers.models.ApiCallResult;
import com.getbooks.gb_offers.tasks.AllegroRequestTask;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.JsonParser;

import java.util.ArrayList;

@RestController
public class OffersController {

    @PostMapping("/calculate")
    public ArrayList<ApiCallResult> receiveLoadOfBooks(@RequestBody BookRequestBody requestBody) {

        AllegroRequestTask.authorizationString = requestBody.authorization;

        var singleResponse = AllegroRequestTask.callApi(requestBody.books.get(0));
        var parser = new JsonParser();
        singleResponse.thenAccept(body -> {
            parser.parse(body)
                    .getAsJsonObject()
                    .get("items")
                    .getAsJsonObject().get("regular");

        });

        var calculated = new ArrayList<ApiCallResult>();
        return calculated;
    }
}
