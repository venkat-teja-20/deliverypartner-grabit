package com.grabit.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.grabit.Utilities.Utility;
import com.grabit.exception.CustomException;

public enum DeliveryPartnerStatus {
    ACTIVE,

    INACTIVE,

    SUSPENDED;

    @JsonCreator
    public static DeliveryPartnerStatus fromValue(String value){
        for(DeliveryPartnerStatus partnerStatus:values()){
            if(partnerStatus.name().equalsIgnoreCase(value))
                return partnerStatus;
        }
        throw new CustomException(Utility.buildErrorObject("BAD_REQUEST","Invalid DeliveryPartnerStatus : "+value,406,"DeliveryPartnerStatus"));
    }

    @JsonValue
    public String isValue(){
        return this.name();
    }
}
