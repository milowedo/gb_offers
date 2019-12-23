package com.getbooks.gb_offers.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SellerWithOffersContainer {

    Seller seller;

    HashSet<BookResult> bookResult;
}
