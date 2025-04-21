package com.example.demo.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Order;
import com.example.demo.domain.response.ResultPaginationDTO;
import com.example.demo.domain.response.order.ResOrderDTO;
import com.example.demo.domain.response.order.ResOrderStatusDTO;
import com.example.demo.service.OrderService;
import com.example.demo.util.exception.InvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    // Dependency Inject: Constructor
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<ResOrderDTO> createNewOrder(@Valid @RequestBody Order createOrder) {
        Order newOrder = this.orderService.handleCreateOrder(createOrder);

        // convert Order -> ResCreateOrderDTO
        ResOrderDTO orderDTO = this.orderService.convertToResOrderDTO(newOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ResOrderDTO> getOrderById(@PathVariable("id") long id) throws InvalidException {
        Order order = this.orderService.handleGetOrderById(id);
        if (order == null) {
            throw new InvalidException("Order with id = " + id + " not exist");
        }

        ResOrderDTO orderDTO = this.orderService.convertToResOrderDTO(order);
        return ResponseEntity.status(HttpStatus.OK).body(orderDTO);
    }

    @GetMapping("/orders")
    public ResponseEntity<ResultPaginationDTO> getAllOrders(@Filter Specification<Order> spec, Pageable pageable) {
        ResultPaginationDTO result = this.orderService.fetchAllOrders(spec, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping("/orders/status")
    public ResponseEntity<ResOrderStatusDTO> updateOrderStatus(@Valid @RequestBody Order requestOrder) throws InvalidException {
        Order order = this.orderService.handleUpdateOrderStatus(requestOrder);
        if (order == null) {
            throw new InvalidException("Order with id = " + requestOrder.getId() + " not exist");
        }
        // convert Order -> ResOrderStatusDTO
        ResOrderStatusDTO statusDTO = this.orderService.convertToResOrderStatusDTO(order);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(statusDTO);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable("id") long id) throws InvalidException {
        Order currentOrder = this.orderService.handleGetOrderById(id);
        if (currentOrder == null) {
            throw new InvalidException("Order with id = " + id + " not exist");
        }
        this.orderService.handleDeleteOrder(id);
        return ResponseEntity.status(HttpStatus.OK).body("Delete Order Success!");
    }

}
