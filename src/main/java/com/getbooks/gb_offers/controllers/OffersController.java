package com.getbooks.gb_offers.controllers;

import com.getbooks.gb_offers.models.OffersEndpointRequestBody;
import com.getbooks.gb_offers.models.BookResult;
import com.getbooks.gb_offers.models.Seller;
import com.getbooks.gb_offers.tasks.AllegroRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@RestController
public class OffersController {

    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @PostMapping("/calculate")
    public ResponseEntity<Object> getCalculatedOffers(@RequestBody OffersEndpointRequestBody requestBody) {
        var receivedAth = requestBody.authorization;
        var receivedBooks = requestBody.books;

        if (receivedBooks == null || receivedAth == null) {
            logger.error("Request body had wrong structure");
            return ResponseEntity.status(400).body("Request body should contain fields: authorization and books");
        }

        AllegroRequestHandler.authorizationString = receivedAth;
        var calculatedResult = new ConcurrentHashMap<Seller, HashSet<BookResult>>();
        try {
            CompletableFuture
                    .allOf(receivedBooks.parallelStream()
                            .map(book -> AllegroRequestHandler.addOffersForBook(book, calculatedResult))
                            .toArray(CompletableFuture[]::new)).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Something went wrong while analyzing tasks" + e.getMessage());
        }

        return ResponseEntity.ok().body(calculatedResult);
    }
}
