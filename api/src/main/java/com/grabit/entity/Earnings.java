package com.grabit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grabit.enums.DeliveryPartnerOrderSpecificStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity(name = "earnings")
@Setter
@Getter
@Table(name = "earnings")
public class Earnings extends AuditDetails<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "money_earned")
    private Integer moneyEarned;

    @Column(name = "money_to_be_earned")
    private Integer moneyToBeEarned=0;

    @Column(name = "order_accepted_time",nullable = false)
    private OffsetDateTime orderAcceptedTime;

    @Column(name = "order_delivered_time")
    private OffsetDateTime orderDeliveredTime;

    @Column(name = "delivery_time")
    private Long deliveryTime;

    @Column(name = "order_id",unique = true,nullable = false)
    private Long orderId;

    @Column(name = "order_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryPartnerOrderSpecificStatus orderStatus;

    @JoinColumn(name = "delivery_partner_id", nullable = false)
    @ManyToOne
    @JsonIgnore
    private DeliveryPartner deliveryPartner;
}
