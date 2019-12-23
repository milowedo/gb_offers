package com.getbooks.gb_offers.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class OffersEndpointRequestBody {

    public List<BookEntityReceived> books;
    public String authorization;
}
