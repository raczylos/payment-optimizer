package com.supermarket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.model.Order;
import com.supermarket.model.OrderInput;
import com.supermarket.model.Promotion;
import com.supermarket.service.PaymentService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Wrong number of arguments. Should be 2 arguments.");
            System.exit(1);
        }
        ObjectMapper objectMapper = new ObjectMapper();

        List<Promotion> promotions = objectMapper.readValue(new File(args[1]), new TypeReference<>() {
        });

        List<OrderInput> ordersInput = objectMapper.readValue(new File(args[0]), new TypeReference<>() {
        });

        List<Order> orders = ordersInput.stream().map(orderInput -> {
            Order order = new Order();
            order.setId(orderInput.getId());
            order.setValue(orderInput.getValue());

            if (orderInput.getPromotions() == null) {
                return order;
            }

            List<Promotion> orderPromotions = orderInput.getPromotions().stream().map(promotionName ->
                    promotions.stream().filter(promotion ->
                            promotion.getId().equals(promotionName)).findFirst().orElse(null)
            ).filter(Objects::nonNull).collect(Collectors.toList());

            order.setPromotions(orderPromotions);
            return order;

        }).collect(Collectors.toList());

        PaymentService paymentService = new PaymentService();
        System.out.println(paymentService.optimizePayments(orders, promotions));


    }
}
