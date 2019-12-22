package com.getbooks.gb_offers.models;

import lombok.*;

import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Seller {
    public String seller_id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seller seller = (Seller) o;
        return seller_id.equals(seller.seller_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seller_id);
    }


    @Override
    public String toString() {
        return seller_id;
    }
}
