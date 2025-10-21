package com.grabit.feign;

import com.grabit.bean.Order.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "orderservice")
public interface OrderInterface {

    @GetMapping(value = "/orders/{id}/exists",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> checkIfOrderExists(@PathVariable(value = "id") String orderId);

    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDTO updateOrderDetails(@RequestBody OrderDTO orderDTO, @PathVariable(value = "id") String orderId);

    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDTO getOrderById(@PathVariable(value = "id") String orderId);
}
