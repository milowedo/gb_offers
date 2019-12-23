package com.getbooks.gb_offers.models;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookEntityReceived {
    public String _id;
    public String writer;
    public String title;
    public Double price;
}
