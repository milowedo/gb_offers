package com.getbooks.gb_offers.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    public String _id;
    public String writer;
    public String title;
    public Double price;

    @Override
    public String toString() {
        return "Book{" +
                "_id='" + _id + '\'' +
                ", writer='" + writer + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                "}\n";
    }
}
