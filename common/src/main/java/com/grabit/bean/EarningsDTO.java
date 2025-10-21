package com.grabit.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.grabit.enums.DeliveryPartnerOrderSpecificStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class EarningsDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("money_earned")
    private Integer moneyEarned;

    @JsonProperty("order_accepted_time")
    private OffsetDateTime orderAcceptedTime;

    @JsonProperty("order_delivered_time")
    private OffsetDateTime orderDeliveredTime;

    @JsonProperty("delivery_time")
    private Long deliveryTime;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("order_status")
    @Enumerated(EnumType.STRING)
    private DeliveryPartnerOrderSpecificStatus orderStatus;

    @JsonProperty("delivery_partner_id")
    @JsonIgnore
    private DeliveryPartnerDTO deliveryPartner;
}
