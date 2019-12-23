package com.getbooks.gb_offers.models;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.*;
import org.springframework.boot.jackson.JsonComponent;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonComponent
public class Seller implements Serializable {

    @JsonRawValue
    public String seller_id;

    @JsonRawValue
    private String lowestPriceDelivery;

    @JsonRawValue
    private Double total;

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
}
