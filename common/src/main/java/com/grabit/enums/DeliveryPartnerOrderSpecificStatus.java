package com.grabit.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.grabit.Utilities.Utility;
import com.grabit.exception.CustomException;

public enum DeliveryPartnerOrderSpecificStatus {
    ACCEPTED(1),

    REJECTED(1),

    PICKED_UP(2),

    ARRIVED_AT_LOCATION(3),

    DELIVERED(4);

    private final int value;

    DeliveryPartnerOrderSpecificStatus(int value){
        this.value=value;
    }

    public int getValue(){
        return this.value;
    }

    public DeliveryPartnerOrderSpecificStatus getStatusFromValue(int value){
        for(DeliveryPartnerOrderSpecificStatus status:values()){
            if(status.getValue()==value)
                return status;
        }
        throw new CustomException(Utility.buildErrorObject("INVALID_STATUS_VALUE","Invalid DeliveryPartnerOrderSpecificStatus value : "+value,406,"DeliveryPartnerOrderSpecificStatus"));
    }

    @JsonCreator
    public static DeliveryPartnerOrderSpecificStatus fromValue(String value){
        for(DeliveryPartnerOrderSpecificStatus status:values()){
            if(value.equalsIgnoreCase(status.name()))
                return status;
        }
        throw new CustomException(Utility.buildErrorObject("BAD_REQUEST","Invalid DeliveryPartnerOrderSpecificStatus : "+value,406,"DeliveryPartnerOrderSpecificStatus"));
    }

    @JsonValue
    public String toValue(){
        return this.name();
    }
}
