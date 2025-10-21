package com.grabit.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabit.Utilities.ModelMapperUtility;
import com.grabit.Utilities.Utility;
import com.grabit.bean.DeliveryPartnerDTO;
import com.grabit.bean.DeliveryPartnerUpdateDTO;
import com.grabit.bean.Order.OrderDTO;
import com.grabit.bean.UpdateOrderStatus;
import com.grabit.entity.DeliveryPartner;
import com.grabit.entity.Earnings;
import com.grabit.enums.*;
import com.grabit.exception.CustomException;
import com.grabit.feign.OrderInterface;
import com.grabit.mapper.OrderURLMapper;
import com.grabit.repository.DeliveryPartnerRepository;
import com.grabit.repository.EarningsRepository;
import com.squareup.okhttp.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Log4j2
@Service
public class DeliveryPartnerService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private EarningsRepository earningsRepository;

    @Autowired
    private OrderInterface orderInterface;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderURLMapper orderURLMapper;

    private OkHttpClient client;

    public DeliveryPartnerDTO addNewPartner(DeliveryPartnerDTO request) {
        DeliveryPartner deliveryPartner = setDeliveryPartnerDetails(request, new DeliveryPartner());
        DeliveryPartner savedDeliveryPartner = deliveryPartnerRepository.save(deliveryPartner);
        log.info("Saved Delivery Partner Details : " + Utility.toJson(savedDeliveryPartner));
        return ModelMapperUtility.map(savedDeliveryPartner, DeliveryPartnerDTO.class);
    }

    private DeliveryPartner setDeliveryPartnerDetails(DeliveryPartnerDTO request, DeliveryPartner deliveryPartner) {
        if(Utility.isNullOrEmpty(request.getGender()))
            throw new CustomException(Utility.buildErrorObject("GENDER_MISSING","Required Delivery Partner Gender in the Request",400,"addNewPartner"));
        BeanUtils.copyProperties(request, deliveryPartner);
//        List<Earnings> earningsList=request.getEarnings().stream().map(earningsDTO -> {
//            Earnings earnings=new Earnings();
//            BeanUtils.copyProperties(earningsDTO,earnings);
//            earnings.setDeliveryPartner(deliveryPartner);
//            return earnings;
//        }).toList();
//        deliveryPartner.setEarnings(earningsList);

        return deliveryPartner;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,isolation = Isolation.READ_COMMITTED)
    public Map<String,Object> updateOrderStatus(String deliveryPartnerId, String orderId, UpdateOrderStatus request) {
        isValidDeliveryPartnerId(deliveryPartnerId,"updateOrderStatus");
        validateUpdateRequest(request);

        DeliveryPartner deliveryPartner=deliveryPartnerRepository.findById(Long.valueOf(deliveryPartnerId)).orElseThrow(()->new EntityNotFoundException("Delivery partner not found"));
        if(!deliveryPartner.getDeliveryPartnerStatus().equals(DeliveryPartnerStatus.ACTIVE))
            throw new CustomException(Utility.buildErrorObject("INACTIVE_DELIVERY_PARTNER","Delivery partner requested is not active",406,"updateOrderStatus"));

        Earnings earnings=earningsRepository.findByOrderId(Long.valueOf(orderId)).orElse(null);
        DeliveryPartnerOrderSpecificStatus currentDeliveryPartnerOrderStatus=null;
        if(earnings!=null)
            currentDeliveryPartnerOrderStatus=earnings.getOrderStatus();
        if(currentDeliveryPartnerOrderStatus!=null){
            if(currentDeliveryPartnerOrderStatus.equals(request.getOrderStatus()))
                throw new CustomException(Utility.buildErrorObject("ORDER_ALREADY_"+currentDeliveryPartnerOrderStatus,"Order is already "+currentDeliveryPartnerOrderStatus.toString().toLowerCase(),400,"updateOrderStatus"));
            if(request.getOrderStatus().getValue()<currentDeliveryPartnerOrderStatus.getValue())
                throw new CustomException(Utility.buildErrorObject(request.getOrderStatus()+"_NOT_ALLOWED","Order marked as "+currentDeliveryPartnerOrderStatus+" cannot be allowed to mark as "+request.getOrderStatus(),409,"updateOrderStatus"));
            if(request.getOrderStatus().getValue()-currentDeliveryPartnerOrderStatus.getValue()>1)
                throw new CustomException(Utility.buildErrorObject(request.getOrderStatus()+"_NOT_ALLOWED","Order marked as "+currentDeliveryPartnerOrderStatus+" cannot be allowed to mark as "+request.getOrderStatus()+" before "+currentDeliveryPartnerOrderStatus.getStatusFromValue(currentDeliveryPartnerOrderStatus.getValue()+1),409,"updateOrderStatus"));
        }
        else{
            if(request.getOrderStatus().getValue()!=1)
                throw new CustomException(Utility.buildErrorObject(request.getOrderStatus()+"_NOT_ALLOWED","Order cannot be allowed to "+request.getOrderStatus()+" before accepting it",409,"updateOrderStatus"));
        }

        //        Map<String, Object> orderCheckResult=orderInterface.checkIfOrderExists(orderId);
        String url=orderURLMapper.getOrderExistsUrl(orderId);
        Map<String,Object> orderCheckResult=restTemplate.getForObject(url,Map.class);
        if(Utility.isNullOrEmpty(orderCheckResult) || !(boolean) orderCheckResult.get("is_present")){
            throw new CustomException(Utility.buildErrorObject("ORDER_NOT_FOUND","Order doesn't exists with provided order id",404,"updateOrderStatus"));
        }

        if(earnings==null) {
            earnings = new Earnings();
            earnings.setOrderId(Long.valueOf(orderId));
        }
        earnings.setOrderStatus(request.getOrderStatus());
        Earnings savedEarnings;
        OrderDTO orderDTO = new OrderDTO();
        if(request.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.ACCEPTED)){
            deliveryPartner.setCurrentActiveOrders(deliveryPartner.getCurrentActiveOrders()+1);
            earnings.setMoneyToBeEarned(request.getAmountToBeCredited());
            earnings.setDeliveryPartner(deliveryPartner);
            earnings.setOrderAcceptedTime(OffsetDateTime.now(ZoneOffset.UTC));
            savedEarnings = earningsRepository.save(earnings);
            log.info("Order accepted successfully at earnings");
            deliveryPartnerRepository.save(deliveryPartner);
            orderDTO.setDeliveryPartnerId(Long.valueOf(deliveryPartnerId));
        }
        else if(request.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.REJECTED)){
            earnings.setMoneyToBeEarned(request.getAmountToBeCredited());
            earnings.setDeliveryPartner(deliveryPartner);
            savedEarnings = earningsRepository.save(earnings);
            log.info("Order rejected successfully at earnings");
        }
        else if(request.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.DELIVERED)){
            deliveryPartner.setCurrentActiveOrders(deliveryPartner.getCurrentActiveOrders()-1);
            deliveryPartner.setTotalMoneyEarned(deliveryPartner.getTotalMoneyEarned()+earnings.getMoneyToBeEarned());
            deliveryPartner.setOrdersDelivered(deliveryPartner.getOrdersDelivered()+1);
            earnings.setMoneyEarned(earnings.getMoneyToBeEarned());
            earnings.setMoneyToBeEarned(0);
            earnings.setOrderDeliveredTime(OffsetDateTime.now(ZoneOffset.UTC));
            Long deliveryTimeInSeconds=Duration.between(earnings.getOrderAcceptedTime(),earnings.getOrderDeliveredTime()).getSeconds();
            earnings.setDeliveryTime(deliveryTimeInSeconds);
            savedEarnings = earningsRepository.save(earnings);
            log.info("Order delivered successfully at earnings");
            deliveryPartnerRepository.save(deliveryPartner);
            orderDTO.setOrderDeliveredTime(savedEarnings.getOrderDeliveredTime());
            orderDTO.setDeliveryTime(deliveryTimeInSeconds);
        }
        else
            savedEarnings = earningsRepository.save(earnings);

        if(!request.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.REJECTED)) {
            try {
                client = new OkHttpClient();
                if (!request.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.ACCEPTED))
                    orderDTO.setOrderStatus(OrderStatus.valueOf(String.valueOf(savedEarnings.getOrderStatus())));
                Request updateRequest = new Request.Builder()
                        .url(orderURLMapper.getUpdateOrderUrl(orderId))
                        .patch(RequestBody.create(MediaType.parse("application/json"), Utility.toJson(orderDTO)))
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(updateRequest).execute();
                if (Utility.isNullOrEmpty(response))
                    throw new CustomException(Utility.buildErrorObject("SOMETHING_WENT_WRONG", "No Response from delivery partner id update in order request", 500, "updateOrderStatus"));
                log.info("Update Delivery Partner Id Response Code : " + response.code());
                log.info("Update Delivery Partner Id Response Body : " + response.body().string());
                if (Utility.isNullOrEmpty(Utility.isNullOrEmpty(response)))
                    throw new CustomException(Utility.buildErrorObject("ORDER_UPDATE_FAILED", "Error while updating delivery partner id for order", 500, "updateOrderStatus"));
                if (response.code() != 200)
                    throw new CustomException(Utility.buildErrorObject("ORDER_UPDATE_FAILED", response.body().string(), response.code(), "updateOrderStatus"));
            } catch (Exception ex) {
                if (ex instanceof CustomException)
                    throw (CustomException) ex;
                log.info(ex);
                throw new CustomException(Utility.buildErrorObject("ORDER_UPDATE_FAILED", "Something went wrong while updating delivery partner id for order", 500, "updateOrderStatus"));
            }
        }
        return Map.of("status","SUCCESS","order_acceptance_id",savedEarnings.getId());
        
    }

    private void validateUpdateRequest(UpdateOrderStatus request){
        if(Utility.isNullOrEmpty(request.getOrderStatus()) || ((request.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.PICKED_UP) || request.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.ARRIVED_AT_LOCATION) || request.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.DELIVERED)) && Utility.isNullOrEmpty(request.getOrderAcceptanceId()))
        || (request.getOrderStatus().getValue()==1 && Utility.isNullOrEmpty(request.getAmountToBeCredited())))
            throw new CustomException(Utility.buildErrorObject(CommonErrors.MANDATORY_ATTRIBUTES_MISSING.toString(),CommonErrors.MANDATORY_ATTRIBUTES_MISSING.getMessage(), 400,"updateOrderStatus"));

    }

    public Map<String,Object> deliveryPartnerExists(String deliveryPartnerId){
        isValidDeliveryPartnerId(deliveryPartnerId,"deliveryPartnerExists");
        Map<String,Object> deliveryPartnerExistence=new HashMap<>();
        deliveryPartnerExistence.put("delivery_partner_id",Long.valueOf(deliveryPartnerId));
        deliveryPartnerExistence.put("is_present",deliveryPartnerRepository.existsById(Long.valueOf(deliveryPartnerId)));
        return deliveryPartnerExistence;
    }

    /*@Transactional
    public Map<String,Object> pickUpOrder(String deliveryPartnerId,String orderId,String orderAcceptanceId){
        isValidDeliveryPartnerId(deliveryPartnerId,"pickUpOrder");
        if(!Utility.isNumeric(orderAcceptanceId))
            throw new CustomException(Utility.buildErrorObject("INVALID_ORDER_ACCEPTANCE_ID","Order Acceptance id provided is not valid",400,"pickUpOrder"));
        //Map<String, Object> orderCheckResult=orderInterface.checkIfOrderExists(orderId);
        String url=orderURLMapper.getOrderDetailsUrl(orderId);
        OrderDTO orderDetails=restTemplate.getForObject(url,OrderDTO.class);
        if(Utility.isNullOrEmpty(orderDetails)){
            throw new CustomException(Utility.buildErrorObject("ORDER_NOT_FOUND","Order doesn't exists with provided order id",404,"pickUpOrder"));
        }
        if(Utility.isNullOrEmpty(orderDetails.getOrderStatus())){
            throw new CustomException(Utility.buildErrorObject("ORDER_STATUS_MISSING","Order Status is missing in order details",404,"pickUpOrder"));
        }

        DeliveryPartner deliveryPartner=deliveryPartnerRepository.findById(Long.valueOf(deliveryPartnerId)).orElseThrow(()->new EntityNotFoundException("Delivery partner not found"));
        if(!"ACTIVE".equals(deliveryPartner.getDeliveryPartnerStatus()))
            throw new CustomException(Utility.buildErrorObject("INACTIVE_DELIVERY_PARTNER","Delivery partner requested is not active",406,"pickUpOrder"));
        Earnings earnings=earningsRepository.findById(Long.valueOf(orderAcceptanceId)).orElseThrow(()->new EntityNotFoundException("No data found with order acceptance id"));
        if(earnings.getOrderStatus().equals(DeliveryPartnerOrderSpecificStatus.PICKED_UP))
            throw new CustomException(Utility.buildErrorObject("ORDER_PICKED_ACCEPTED","Order is already picked up",400,"pickUpOrder"));
        earnings.setOrderStatus(DeliveryPartnerOrderSpecificStatus.PICKED_UP);
        Earnings savedEarnings=earningsRepository.save(earnings);
        try {
            client = new OkHttpClient();
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderStatus(OrderStatus.PICKED_UP);
            Request request = new Request.Builder()
                    .url(orderURLMapper.getUpdateOrderUrl(orderId))
                    .patch(RequestBody.create(MediaType.parse("application/json"), Utility.toJson(orderDTO)))
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response=client.newCall(request).execute();
            if(Utility.isNullOrEmpty(response))
                throw new CustomException(Utility.buildErrorObject("SOMETHING_WENT_WRONG","No Response from delivery partner id update in order request",500,"orderAccept"));
            log.info("Update Delivery Partner Id Response Code : "+response.code());
            log.info("Update Delivery Partner Id Response Body : "+response.body().string());
            if(response.code()!=200)
                throw new CustomException(Utility.buildErrorObject("ORDER_UPDATE_FAILED","Error while updating delivery partner id for order",500,"orderAccept"));
        } catch (IOException ex){
            log.info(ex.getMessage());
        }
        return Map.of("status","SUCCESS","order_acceptance_id",savedEarnings.getId());

    }*/

    @Transactional
    public DeliveryPartnerDTO updateDetails(DeliveryPartnerUpdateDTO request, String deliveryPartnerId){
        isValidDeliveryPartnerId(deliveryPartnerId,"updateDetails");
        DeliveryPartner deliveryPartner=deliveryPartnerRepository.findById(Long.valueOf(deliveryPartnerId)).orElseThrow(()->new EntityNotFoundException("Delivery Partner not found"));
        setDeliveryPartnerDetailsToUpdate(request,deliveryPartner);
        DeliveryPartner savedDeliveryPartner=deliveryPartnerRepository.save(deliveryPartner);
        log.info("Updated Delivery Partner Details : "+Utility.toJson(savedDeliveryPartner));
        return ModelMapperUtility.map(savedDeliveryPartner,DeliveryPartnerDTO.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void callCustomer(){
        System.out.println("Calling Customer...");
    }

    private void setDeliveryPartnerDetailsToUpdate(DeliveryPartnerUpdateDTO request,DeliveryPartner deliveryPartner){
        try {
            DeliveryPartnerDTO existingDetails = ModelMapperUtility.map(deliveryPartner, DeliveryPartnerDTO.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String requestString = objectMapper.writeValueAsString(request);
            objectMapper.readerForUpdating(existingDetails).readValue(requestString);
            BeanUtils.copyProperties(existingDetails,deliveryPartner);
        } catch (JsonProcessingException e) {
            log.info("Json Parsing Error : "+e);
            throw new CustomException(Utility.buildErrorObject(CommonErrors.JSON_PARSING_ERROR.toString(),CommonErrors.JSON_PARSING_ERROR.getMessage(),500,"updateDetails"));
        }
        catch (Exception e) {
            log.info("Something went wrong while processing update request : "+e);
            throw new CustomException(Utility.buildErrorObject(CommonErrors.unknown_error.toString(),CommonErrors.unknown_error.getMessage(), 500,"updateDetails"));
        }
    }

    private void isValidDeliveryPartnerId(String deliveryPartnerId,String service){
        if(!Utility.isNumeric(deliveryPartnerId))
            throw new CustomException(Utility.buildErrorObject("INVALID_DELIVERY_PARTNER_ID","Delivery Partner Id provided is not valid",400,service));
    }
}
