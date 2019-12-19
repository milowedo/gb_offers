package com.getbooks.gb_offers.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookResult {
    String book_id;
    String auction_id;
    String imageUrl;
    String priceAmount;
}
