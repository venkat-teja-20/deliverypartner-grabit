package com.grabit.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.grabit.Utilities.Utility;
import com.grabit.exception.CustomException;

public enum Gender {
    MALE,

    FEMALE,

    PREFER_NOT_TO_DISCLOSE;

    @JsonCreator
    public static Gender fromValue(String value){
        for(Gender gender:values()){
            if(gender.name().equalsIgnoreCase(value))
                return gender;
        }
        throw new CustomException(Utility.buildErrorObject("BAD_REQUEST","Invalid Gender : "+value,406,"Gender"));
    }

    @JsonValue
    public String isValue(){
        return this.name();
    }
}
