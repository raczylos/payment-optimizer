package com.supermarket.service;

import com.supermarket.model.Order;
import com.supermarket.model.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;


class PaymentServiceTest {
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();

    }

    @Test
    void shouldFindBestTotalPromotion() {
        List<Promotion> promotions = List.of(
                new Promotion("PUNKTY", 15, 100.00),
                new Promotion("mZysk", 10, 180.00),
                new Promotion("BosBankrut", 5, 200.00)
        );
        Order order = new Order("ORDER3", 150.00, List.of(promotions.get(1), promotions.get(2)));
        paymentService.availablePromotions.addAll(promotions);
        Optional<Promotion> bestPromotion = paymentService.findBestTotalPromotion(order);
        assertEquals(bestPromotion.get(), promotions.get(1));
    }

    @Test
    void shouldUpdatePayment() {
        List<Promotion> promotions = List.of(
                new Promotion("PUNKTY", 15, 100.00),
                new Promotion("mZysk", 10, 180.00),
                new Promotion("BosBankrut", 5, 200.00)
        );

        paymentService.availablePromotions.addAll(promotions);
        paymentService.promotionSummary = promotions.stream().collect(Collectors.toMap(Promotion::getId, value -> 0.0));

        Promotion payment = promotions.get(0);
        paymentService.updatePaymentUsage(payment, 80.00);
        assertEquals(80.00, paymentService.promotionSummary.get(payment.getId()));
        assertEquals(20.00, payment.getLimit());
    }

    @Test
    void shouldPayRemainingWithLowestDiscountCard() {
        List<Promotion> promotions = List.of(
                new Promotion("PUNKTY", 15, 100.00),
                new Promotion("mZysk", 10, 180.00),
                new Promotion("BosBankrut", 5, 200.00)
        );

        paymentService.availablePromotions.addAll(promotions);
        paymentService.promotionSummary = promotions.stream().collect(Collectors.toMap(Promotion::getId, value -> 0.0));

        Promotion payment = promotions.get(2);
        paymentService.payRemainingWithCard(80.00);
        assertEquals(120.00, payment.getLimit());
    }

    @Test
    void shouldPayWholePriceAfterDiscount() {
        List<Promotion> promotions = List.of(
                new Promotion("PUNKTY", 15, 100.00),
                new Promotion("mZysk", 10, 180.00),
                new Promotion("BosBankrut", 5, 200.00)
        );

        paymentService.availablePromotions.addAll(promotions);
        paymentService.promotionSummary = promotions.stream().collect(Collectors.toMap(Promotion::getId, value -> 0.0));

        Promotion payment = promotions.get(2);
        paymentService.payWholePriceAfterDiscount(payment, 80.00);
        assertEquals(120.00, payment.getLimit());
    }

    @Test
    void shouldProcessOrderWithPoints() {
        List<Promotion> promotions = List.of(
                new Promotion("PUNKTY", 15, 100.00),
                new Promotion("mZysk", 10, 180.00),
                new Promotion("BosBankrut", 5, 200.00)
        );

        paymentService.availablePromotions.addAll(promotions);
        paymentService.promotionSummary = promotions.stream().collect(Collectors.toMap(Promotion::getId, value -> 0.0));

        Order order = new Order("ORDER1", 100.00, List.of(promotions.get(0), promotions.get(1)));
        paymentService.processOrder(order);
        Promotion points = promotions.get(0);
        assertEquals(85.00, paymentService.promotionSummary.get(points.getId()));
    }

    @Test
    void shouldOptimizePayments() {
        List<Promotion> promotions = List.of(
                new Promotion("PUNKTY", 15, 100.00),
                new Promotion("mZysk", 10, 180.00),
                new Promotion("BosBankrut", 5, 200.00)
        );

        paymentService.availablePromotions.addAll(promotions);
        paymentService.promotionSummary = promotions.stream().collect(Collectors.toMap(Promotion::getId, value -> 0.0));

        List<Order> orders = new ArrayList<>(List.of(
                new Order("ORDER1", 100.00, List.of(promotions.get(1))),
                new Order("ORDER2", 200.00, List.of(promotions.get(2))),
                new Order("ORDER3", 150.00, List.of(promotions.get(1), promotions.get(2))),
                new Order("ORDER4", 50.00, null)
        ));

        paymentService.optimizePayments(orders, promotions);
        assertEquals(Map.of("PUNKTY",100.0, "BosBankrut",190.0, "mZysk",165.0), paymentService.promotionSummary);
    }


}