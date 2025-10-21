package com.grabit.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.grabit.enums.DeliveryPartnerOrderSpecificStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateOrderStatus {

    @JsonProperty("order_acceptance_id")
    private Long orderAcceptanceId;

    @JsonProperty("partner_specific_status")
    @Enumerated(EnumType.STRING)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DeliveryPartnerOrderSpecificStatus orderStatus;

    @JsonProperty("status")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updateStatus;

    @JsonProperty("amount_to_be_credited")
    private Integer amountToBeCredited;
}
