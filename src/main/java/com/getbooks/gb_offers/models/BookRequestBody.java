package com.getbooks.gb_offers.models;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class BookRequestBody {

    public List<Book> books;
    public String authorization;
}
