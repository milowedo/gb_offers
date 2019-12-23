package com.getbooks.gb_offers.models;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookResult implements Serializable {
    @JsonRawValue
    private String auction_id;

    @JsonRawValue
    private String imageUrl;

    @JsonRawValue
    private String auctionName;

    private String writer;

    private String bookTitle;

    @JsonRawValue
    private Double priceAmount;
}
