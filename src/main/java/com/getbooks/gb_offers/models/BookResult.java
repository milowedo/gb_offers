package com.getbooks.gb_offers.models;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookResult {
    @JsonRawValue
    private String auction_id;
    @JsonRawValue
    private String imageUrl;
    @JsonRawValue
    private String name;
    @JsonRawValue
    private String priceAmount;
    @JsonRawValue
    private String lowestPriceDelivery;
}
