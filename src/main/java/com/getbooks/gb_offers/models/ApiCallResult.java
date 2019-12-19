package com.getbooks.gb_offers.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiCallResult {
    String seller_id;
    String delivery_lowestPrice;
    List<BookResult> book;
}
