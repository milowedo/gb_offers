package com.getbooks.gb_offers.tasks;

import com.getbooks.gb_offers.models.BookEntityReceived;
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
import java.util.Optional;
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

    public static CompletableFuture<Void> addOffersForBook(BookEntityReceived bookEntityReceived, ConcurrentHashMap<Seller, HashSet<BookResult>> calculatedResult) {
        return callApi(bookEntityReceived).thenCompose(AllegroRequestHandler::parseResponseToOffers)
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
                            newBook.setBookTitle(bookEntityReceived.title);
                            newBook.setWriter(bookEntityReceived.writer);
                            calculatedResult.putIfAbsent(seller, new HashSet<>());
                            removeOldBookIfMoreExpensive(calculatedResult, seller, newBook);
                            calculatedResult.get(seller).add(newBook);
                        }));
    }

    private static void removeOldBookIfMoreExpensive(ConcurrentHashMap<Seller, HashSet<BookResult>> calculatedResult,
                                                     Seller seller, BookResult newBook) {
        var sellersBooks = calculatedResult.get(seller);
        Optional<BookResult> bookTypeAlreadyPresent = sellersBooks.parallelStream()
                .filter(offer -> offer.getBookTitle().equals(newBook.getBookTitle()) && offer.getWriter().equals(newBook.getWriter()))
                .findFirst();
        bookTypeAlreadyPresent.ifPresent(book -> {
            if (book.getPriceAmount() > newBook.getPriceAmount()) {
                sellersBooks.remove(book);
            }
        });
    }

    private static Seller extractSellerFromJson(JsonObject singleBook) {
        Seller seller = null;
        try {
            seller = new Seller();
            seller.setSeller_id(singleBook.get("seller").getAsJsonObject().get("id").toString());

            var price = singleBook.get("delivery")
                    .getAsJsonObject().get("lowestPrice")
                    .getAsJsonObject().get("amount").getAsDouble();
            seller.setLowestPriceDelivery(price);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return seller;
    }

    private static BookResult extractBookResultFromJson(JsonObject singleBook) {
        try {
            var bookResult = new BookResult();
            bookResult.setAuction_id(singleBook.get("id").toString());
            bookResult.setAuctionName(singleBook.get("name").toString());
            bookResult.setPriceAmount(
                    singleBook.get("sellingMode")
                            .getAsJsonObject().get("price")
                            .getAsJsonObject().get("amount").getAsDouble()
            );
            bookResult.setImageUrl(singleBook.get("images").toString());
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

    private static CompletableFuture<HttpResponse<String>> callApi(BookEntityReceived bookEntityReceived) {

        URI uri = UriComponentsBuilder
                .fromUri(URI.create("https://api.allegro.pl/offers/listing"))
                .queryParam("category.id", BOOKS_CATEGORY_ID)
                .queryParam("phrase", bookEntityReceived.writer + " " + bookEntityReceived.title)
                .queryParam("price.to", bookEntityReceived.price)
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
