package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Order;
import com.example.demo.domain.OrderDetail;
import com.example.demo.domain.User;
import com.example.demo.domain.response.Meta;
import com.example.demo.domain.response.ResultPaginationDTO;
import com.example.demo.domain.response.order.ResOrderDTO;
import com.example.demo.domain.response.order.ResOrderStatusDTO;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.util.constant.StatusEnum;

@Service
public class OrderService {
    // Dependency Inject: Constructor
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderDetailRepository orderDetailRepository;

    public OrderService(OrderRepository orderRepository, UserService userService, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Order handleCreateOrder(Order order) {
        // check user
        if (order.getUser() != null) {
            User user = this.userService.handleGetUserById(order.getUser().getId());
            if (user != null) {
                order.setUser(user);
            } else {
                order.setUser(null);
            }
        }
        order.setStatus(StatusEnum.PENDING);
        return this.orderRepository.save(order);
    }

    public ResOrderDTO convertToResOrderDTO(Order order) {
        ResOrderDTO resOrderDTO = new ResOrderDTO();
        resOrderDTO.setId(order.getId());
        resOrderDTO.setTotalPrice(order.getTotalPrice());
        resOrderDTO.setStatus(order.getStatus());
        resOrderDTO.setShippingAddress(order.getShippingAddress());
        resOrderDTO.setPaymentMethod(order.getPaymentMethod());
        resOrderDTO.setTrackingNumber(order.getTrackingNumber());
        resOrderDTO.setOrderDate(order.getOrderDate());

        ResOrderDTO.UserOrder userDTO = new ResOrderDTO.UserOrder();

        if (order.getUser() != null) {
            userDTO.setId(order.getUser().getId());
            userDTO.setName(order.getUser().getName());

            resOrderDTO.setUser(userDTO);
        }

        return resOrderDTO;
    }

    public Order handleGetOrderById(long id) {
        Optional<Order> orderOptional = this.orderRepository.findById(id);
        if (orderOptional.isPresent()) {
            return orderOptional.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> pageOrder = this.orderRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageOrder.getTotalPages());
        meta.setTotal(pageOrder.getTotalElements());

        result.setMeta(meta);

        List<ResOrderDTO> listOrders = pageOrder.getContent().stream().map(item -> this.convertToResOrderDTO(item))
                .collect(Collectors.toList());

        result.setResult(listOrders);

        return result;
    }

    public Order handleUpdateOrderStatus(Order requestOrder) {
        Order currentOrder = this.handleGetOrderById(requestOrder.getId());
        if (currentOrder != null) {
            currentOrder.setStatus(requestOrder.getStatus());
            currentOrder = this.orderRepository.save(currentOrder);
        }

        return currentOrder;
    }

    public ResOrderStatusDTO convertToResOrderStatusDTO(Order order) {
        ResOrderStatusDTO statusDTO = new ResOrderStatusDTO();
        statusDTO.setId(order.getId());
        statusDTO.setStatus(order.getStatus());
        return statusDTO;
    }

    public void handleDeleteOrder(long id) {
        Order currentOrder = this.handleGetOrderById(id);
        List<OrderDetail> orderDetails = currentOrder.getOrderDetails();
        for (OrderDetail orderDetail : orderDetails) {
            // Delete Order-detail
            this.orderDetailRepository.deleteById(orderDetail.getId());
        }

        // Delete Order
        this.orderRepository.deleteById(id);
    }

}
