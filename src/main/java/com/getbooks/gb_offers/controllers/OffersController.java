package com.getbooks.gb_offers.controllers;

import com.getbooks.gb_offers.models.OffersEndpointRequestBody;
import com.getbooks.gb_offers.models.BookResult;
import com.getbooks.gb_offers.models.Seller;
import com.getbooks.gb_offers.models.SellerWithOffersContainer;
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
import java.util.function.Consumer;
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

        logger.error("Received books " + receivedBooks.size() + " with id of: " + receivedAth);

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

        combDuplicatesOut(calculatedResult);

        List<SellerWithOffersContainer> zippedAndSorted = calculatedResult
                .entrySet().parallelStream()
                .peek(addTotalPrice())
                .map(entry -> new SellerWithOffersContainer(entry.getKey(), entry.getValue()))
                .sorted(sellersCompareByCollectionSize().thenComparing(sellersCompareByCollectionTotalPrice()))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(zippedAndSorted);
    }

    private Comparator<SellerWithOffersContainer> sellersCompareByCollectionSize() {
        return (o1, o2) -> (int) (o1.getSeller().getTotal() - o2.getSeller().getTotal());
    }

    private Comparator<SellerWithOffersContainer> sellersCompareByCollectionTotalPrice() {
        return (o1, o2) -> o2.getBookResult().size() - o1.getBookResult().size();
    }

    private void combDuplicatesOut(ConcurrentHashMap<Seller, HashSet<BookResult>> calculatedResult) {
        calculatedResult.values().parallelStream()
                .forEach(bookSet -> {
                    bookSet.stream()
                            .collect(Collectors.groupingBy(book -> book.getBookTitle().concat(book.getWriter())))
                            .values().stream()
                            .filter(anyList -> anyList.size() > 1)
                            .forEach(listWithDuplicates -> { // these lists contain duplicates
                                        var minimumPriceBook = listWithDuplicates.stream()
                                                .min(Comparator.comparingDouble(BookResult::getPriceAmount)).get();
                                        listWithDuplicates.remove(minimumPriceBook);
                                        bookSet.removeAll(listWithDuplicates);
                                    }
                            );
                });
    }

    private Consumer<Map.Entry<Seller, HashSet<BookResult>>> addTotalPrice() {
        return entry -> entry.getKey().setTotal(
                entry.getValue().parallelStream()
                        .map(BookResult::getPriceAmount).reduce(0.0, Double::sum)
        );
    }
}
