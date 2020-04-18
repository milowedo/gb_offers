package com.getbooks.gb_offers.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Shop {

    Seller seller;

    HashSet<BookResult> bookResult;

    List<BookResult> bookDuplicates;

    public Shop(Seller _seller, HashSet<BookResult> _bookset) {
        seller = _seller;
        bookResult = _bookset;
    }
}
