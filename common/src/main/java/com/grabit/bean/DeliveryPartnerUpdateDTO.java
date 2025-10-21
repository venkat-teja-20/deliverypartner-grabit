package com.grabit.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.grabit.enums.DeliveryPartnerStatus;
import com.grabit.enums.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DeliveryPartnerUpdateDTO {
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

    @JsonProperty("delivery_partner_status")
    @Enumerated(EnumType.STRING)
    private DeliveryPartnerStatus deliveryPartnerStatus;
}
