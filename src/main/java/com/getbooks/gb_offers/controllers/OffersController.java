package com.getbooks.gb_offers.controllers;

import com.getbooks.gb_offers.models.OffersEndpointRequestBody;
import com.getbooks.gb_offers.models.BookResult;
import com.getbooks.gb_offers.models.Seller;
import com.getbooks.gb_offers.models.Shop;
import com.getbooks.gb_offers.tasks.AllegroRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

        logger.error("Received " + receivedBooks.size() + " books with ath of: \n" + receivedAth);

        AllegroRequestHandler.authorizationString = receivedAth;
        var calculatedResult = new ConcurrentHashMap<Seller, HashSet<BookResult>>();
        try {
            CompletableFuture
                    .allOf(receivedBooks.parallelStream()
                            .map(book -> AllegroRequestHandler.addOffersForBook(book, calculatedResult))
                            .toArray(CompletableFuture[]::new)).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Something went wrong while analyzing tasks " + Arrays.toString(e.getStackTrace()));
        }
        var listOfShops = mapSellersAndBookSetsToShops(calculatedResult);
        separateDuplicatedBooks(listOfShops);
        calculateAndPutTotalPrice(listOfShops);
        listOfShops.sort(sellersCompareByCollectionSize().thenComparing(sellersCompareByCollectionTotalPrice()));

        return ResponseEntity.ok().body(listOfShops);
    }

    private void calculateAndPutTotalPrice(List<Shop> listOfShops) {
        listOfShops
                .parallelStream()
                .forEach(shop -> shop.getSeller().setTotal(shop.getBookResult()
                        .parallelStream()
                        .map(BookResult::getPriceAmount)
                        .reduce(0.0, Double::sum)));
    }

    private List<Shop> mapSellersAndBookSetsToShops(ConcurrentHashMap<Seller, HashSet<BookResult>> calculatedResult) {
        return calculatedResult.entrySet().parallelStream()
                .map(entry -> new Shop(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Comparator<Shop> sellersCompareByCollectionTotalPrice() {
        return Comparator.comparingDouble(o -> o.getSeller().getTotal());
    }

    private Comparator<Shop> sellersCompareByCollectionSize() {
        return (o1, o2) -> o2.getBookResult().size() - o1.getBookResult().size();
    }

    private void separateDuplicatedBooks(List<Shop> shopList) {
        shopList.parallelStream()
                .forEach(shop -> shop.getBookResult().stream()
                        .collect(Collectors.groupingBy(book -> book.getBookTitle().concat(book.getWriter())))
                        .values().stream()
                        .filter(anyList -> anyList.size() > 1)
                        .forEach(listWithDuplicates -> {
                            listWithDuplicates.remove(findBookWithLowestPrice(listWithDuplicates));
                            shop.getBookResult().removeAll(listWithDuplicates);
                            shop.setBookDuplicates(listWithDuplicates);
                        })
                );
    }

    private BookResult findBookWithLowestPrice(List<BookResult> listWithDuplicates) {
        return listWithDuplicates.stream().min(Comparator.comparingDouble(BookResult::getPriceAmount)).get();
    }
}
