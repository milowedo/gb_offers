package com.getbooks.gb_offers.tasks;

import com.getbooks.gb_offers.models.Book;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public final class AllegroRequestTask {
    public static final int BOOKS_CATEGORY_ID = 7;
    public static final boolean SHOULD_FALLBACK = false;
    public static final String SELLING_MODE = "BUY_NOW";
    private static HttpClient client = HttpClient.newHttpClient();
    public static String authorizationString;

    public static CompletableFuture<String> callApi(Book book) {

        URI uri = UriComponentsBuilder
                .fromUri(URI.create("https://api.allegro.pl/offers/listing"))
                .queryParam("category.id", BOOKS_CATEGORY_ID)
                .queryParam("phrase", book.writer+" "+book.title)
                .queryParam("price.to", book.price)
                .queryParam("fallback", SHOULD_FALLBACK)
                .queryParam("sellingMode.format", SELLING_MODE)
                .build().toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/vnd.allegro.public.v1+json")
                .header("Authorization", authorizationString)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);

    }
}
