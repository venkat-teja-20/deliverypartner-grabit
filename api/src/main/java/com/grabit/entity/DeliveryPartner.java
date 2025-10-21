package com.grabit.entity;

import com.grabit.enums.DeliveryPartnerStatus;
import com.grabit.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "delivery_partner")
@Setter
@Getter
@Table(name = "delivery_partner")
public class DeliveryPartner extends AuditDetails<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotNull
    @Size(min = 3, max = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    @Size(max = 50)
    private String lastName;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dob;

    @Column(name = "vehicle_name", length = 30, nullable = false)
    @NotNull
    @Size(max = 30)
    private String vehicleName;

    @Column(name = "licence_plate_number", length = 14, nullable = false,unique = true)
    @Size(max = 14)
    @NotNull
    private String licencePlateNumber;

    @Column(name = "orders_delivered")
    private Long ordersDelivered;

    @Column(name = "total_money_earned")
    private Long totalMoneyEarned;

    @Column(name = "delivery_partner_status",nullable = false,length = 10)
    @Enumerated(EnumType.STRING)
    private DeliveryPartnerStatus deliveryPartnerStatus;

    @Column(name = "current_active_orders")
    @Min(0)
    @Max(3)
    private Long currentActiveOrders=0L;

    @OneToMany(mappedBy = "deliveryPartner", cascade = CascadeType.ALL)
    private List<Earnings> earnings = new ArrayList<>();

    @PrePersist
    public void newPartnerDefaultDetails() {
        this.ordersDelivered = 0L;
        this.totalMoneyEarned=0L;
        if(deliveryPartnerStatus==null)
            this.deliveryPartnerStatus= DeliveryPartnerStatus.ACTIVE;
    }
}
