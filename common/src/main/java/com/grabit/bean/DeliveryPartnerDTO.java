package com.grabit.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.grabit.enums.DeliveryPartnerStatus;
import com.grabit.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DeliveryPartnerDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @JsonProperty("date_of_birth")
    private LocalDate dob;

    @JsonProperty("vehicle_name")
    private String vehicleName;

    @JsonProperty("licence_plate_number")
    private String licencePlateNumber;

    @JsonProperty("orders_delivered")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long ordersDelivered;

    @JsonProperty("total_money_earned")
    private Long totalMoneyEarned;

    @JsonProperty("current_active_orders")
    private Long currentActiveOrders=0L;

    @JsonProperty("delivery_partner_status")
    @Enumerated(EnumType.STRING)
    private DeliveryPartnerStatus deliveryPartnerStatus;

    @JsonProperty("earnings")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<EarningsDTO> earnings=new ArrayList<>();
}
