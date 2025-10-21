package com.grabit.mapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class OrderURLMapper {

    @Getter(AccessLevel.NONE)
    String domainURL;

    public OrderURLMapper(String url){
        this.domainURL=url;
    }

    public String getOrderExistsUrl(String orderId){
        return domainURL+"/orders/"+orderId+"/exists";
    }

    public String getUpdateOrderUrl(String orderId){
        return domainURL+"/orders/"+orderId;
    }

    public String getOrderDetailsUrl(String orderId){
        return domainURL+"/orders/"+orderId;
    }

    public String getOrderExistsUrl(Long orderId){
        return domainURL+"/orders/"+orderId+"/exists";
    }
}
