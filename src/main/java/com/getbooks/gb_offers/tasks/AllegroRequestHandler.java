package com.getbooks.gb_offers.tasks;

import com.getbooks.gb_offers.models.BookRequest;
import com.getbooks.gb_offers.models.BookResult;
import com.getbooks.gb_offers.models.Seller;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AllegroRequestHandler {
    static Logger logger = LoggerFactory.getLogger(AllegroRequestHandler.class.getName());

    private static final int BOOKS_CATEGORY_ID = 7;
    private static final boolean SHOULD_FALLBACK = false;
    private static final String SELLING_MODE = "BUY_NOW";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final JsonParser parser = new JsonParser();
    public static String authorizationString;

    public static CompletableFuture<Void> addOffersForBook(BookRequest bookRequest, ConcurrentHashMap<Seller, HashSet<BookResult>> calculatedResult) {
        return callApi(bookRequest).thenCompose(AllegroRequestHandler::parseResponseToOffers)
                .thenApplyAsync(books ->
                        IntStream.range(0, books.size() - 1)
                                .mapToObj(books::get)
                                .collect(Collectors.toList())
                ).thenAcceptAsync(list ->
                        list.parallelStream().map(JsonElement::getAsJsonObject).forEach(singleBook -> {
                            var seller = extractSellerFromJson(singleBook);
                            var newBook = extractBookResultFromJson(singleBook);
                            if (seller == null || newBook == null) {
                                AllegroRequestHandler.logger.info("Could not create book or seller from json offer object");
                                return;
                            }
                            calculatedResult.putIfAbsent(seller, new HashSet<>());
                            calculatedResult.get(seller).add(newBook);
                        }));
    }

    private static Seller extractSellerFromJson(JsonObject singleBook) {
        Seller seller = null;
        try {
            seller = new Seller();
            seller.setSeller_id(singleBook.get("seller").getAsJsonObject().get("id").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return seller;
    }

    private static BookResult extractBookResultFromJson(JsonObject singleBook) {
        try {
            var bookResult = new BookResult();
            bookResult.setAuction_id(singleBook.get("id").toString());
            bookResult.setName(singleBook.get("name").toString());
            bookResult.setPriceAmount(singleBook.get("sellingMode")
                    .getAsJsonObject().get("price")
                    .getAsJsonObject().get("amount").toString());
            bookResult.setImageUrl(singleBook.get("images").toString());
            bookResult.setLowestPriceDelivery(singleBook.get("delivery")
                    .getAsJsonObject().get("lowestPrice")
                    .getAsJsonObject().get("amount").toString());
            return bookResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static CompletableFuture<JsonArray> parseResponseToOffers(HttpResponse<String> response) {
        return CompletableFuture.supplyAsync(() -> parser.parse(response.body())
                .getAsJsonObject()
                .get("items")
                .getAsJsonObject()
                .get("regular")
                .getAsJsonArray());
    }

    private static CompletableFuture<HttpResponse<String>> callApi(BookRequest bookRequest) {

        URI uri = UriComponentsBuilder
                .fromUri(URI.create("https://api.allegro.pl/offers/listing"))
                .queryParam("category.id", BOOKS_CATEGORY_ID)
                .queryParam("phrase", bookRequest.writer + " " + bookRequest.title)
                .queryParam("price.to", bookRequest.price)
                .queryParam("fallback", SHOULD_FALLBACK)
                .queryParam("sellingMode.format", SELLING_MODE)
                .build().toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/vnd.allegro.public.v1+json")
                .header("Authorization", "Bearer " + authorizationString)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

}
