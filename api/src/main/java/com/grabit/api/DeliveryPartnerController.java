package com.grabit.api;

import com.grabit.bean.DeliveryPartnerDTO;
import com.grabit.bean.DeliveryPartnerUpdateDTO;
import com.grabit.bean.UpdateOrderStatus;
import com.grabit.service.DeliveryPartnerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/delivery-partner")
public class DeliveryPartnerController {

    @Autowired
    private DeliveryPartnerService deliveryPartnerService;

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeliveryPartnerDTO addDeliveryPartner(@RequestBody DeliveryPartnerDTO request, HttpServletResponse response) {
        response.setStatus(201);
        return deliveryPartnerService.addNewPartner(request);
    }

    @PatchMapping(value = "/{id}/order/{orderId}/status/update",consumes = MediaType.APPLICATION_JSON_VALUE,produces = "application/json")
    public Map<String,Object> acceptOrder(@PathVariable(value = "id") String deliveryPartnerId, @PathVariable(value = "orderId") String orderId,@RequestBody UpdateOrderStatus request){
        return deliveryPartnerService.updateOrderStatus(deliveryPartnerId,orderId,request);
    }

    @GetMapping(value = "/{id}/exists",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> isDeliveryPartnerExists(@PathVariable(value = "id") String deliveryPartnerId){
        return deliveryPartnerService.deliveryPartnerExists(deliveryPartnerId);
    }

    @PatchMapping(value = "/{id}/update",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public DeliveryPartnerDTO updateDeliveryPartner(@PathVariable(value = "id") String deliveryPartnerId,@RequestBody DeliveryPartnerUpdateDTO request){
        return deliveryPartnerService.updateDetails(request,deliveryPartnerId);
    }

    @PostMapping(value = "/{id}/member/{memberId}/call", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public void makeAPhoneCallToCustomer(@PathVariable(value = "id") String deliveryPartnerId,@PathVariable(value = "memberId") String memberId){
        deliveryPartnerService.callCustomer();
        System.out.println("Call Success");
    }

    // get partner by id
}
