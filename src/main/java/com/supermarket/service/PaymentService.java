package com.supermarket.service;

import com.supermarket.model.Order;
import com.supermarket.model.Promotion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaymentService {

    protected final List<Promotion> availablePromotions = new ArrayList<>();

    protected Map<String, Double> promotionSummary = new HashMap<>();

    public Map<String, Double> optimizePayments(List<Order> orders, List<Promotion> promotions) {
        availablePromotions.addAll(promotions);

        promotionSummary = promotions.stream().collect(Collectors.toMap(Promotion::getId, value -> 0.0));

        orders.sort(Comparator.comparingDouble(Order::getValue).reversed());

        for (Order order : orders) {
            processOrder(order);
        }

        return promotionSummary;
    }

    public void processOrder(Order order) {
//        try to pay with best promotion
        Optional<Promotion> bestPromotion = findBestTotalPromotion(order);
        if (bestPromotion.isPresent()) {
            double valueAfterDiscount = order.getValue() * (1 - bestPromotion.get().getDiscount() / 100.0);
            payWholePriceAfterDiscount(bestPromotion.get(), valueAfterDiscount);
        } else {
//            try to pay with points
            Optional<Promotion> points = availablePromotions.stream().filter(pm -> pm.getId().equals("PUNKTY")).findFirst();
            if (points.isPresent() && points.get().getLimit() > 0) {
                Promotion remainingPoints = points.get();
                if (order.getValue() <= remainingPoints.getLimit()) {
//                    try to pay fully with points
                    double valueAfterDiscount = order.getValue() * (1 - remainingPoints.getDiscount() / 100.0);
                    payWholePriceAfterDiscount(remainingPoints, valueAfterDiscount);
                } else {
//                    try to pay partially with points with discount
//                    10% base discount
                    boolean isPointsAmountSufficientForDiscount = remainingPoints.getLimit() >= order.getValue() * 0.1;
                    if (isPointsAmountSufficientForDiscount) {
                        double valueAfterDiscount = order.getValue() * 0.9;
                        double remainingValue = valueAfterDiscount - remainingPoints.getLimit();
                        updatePaymentUsage(remainingPoints, remainingPoints.getLimit());
                        payRemainingWithCard(remainingValue);

                    }
//                    try to pay partially with points without discount
                    if (!isPointsAmountSufficientForDiscount) {
                        double remainingValue = order.getValue() - remainingPoints.getLimit();
                        updatePaymentUsage(remainingPoints, remainingPoints.getLimit());
                        payRemainingWithCard(remainingValue);
                    }


                }
            } else {
//                try to pay without points and without any discount
                double remainingValue = order.getValue();
                payRemainingWithCard(remainingValue);
            }


        }

    }

    public Optional<Promotion> findBestTotalPromotion(Order order) {
        return availablePromotions.stream()
                .filter(promotion ->
                        order.getPromotions() != null &&
                                order.getPromotions().stream()
                                        .anyMatch(orderPromotion ->
                                                orderPromotion.getId().equals(promotion.getId())
                                        )
                )
                .filter(promotion -> {
                    double valueAfterDiscount = order.getValue() * (1 - promotion.getDiscount() / 100.0);
                    return promotion.getLimit() >= valueAfterDiscount;
                })
                .max(Comparator.comparingInt(Promotion::getDiscount));
    }

    protected void payRemainingWithCard(double remainingValue) {
//        we want to find card with the worst discount cus we can't use that discount anyway
        Promotion card = availablePromotions.stream()
                .filter(pm -> pm.getLimit() >= remainingValue)
                .filter(pm -> !pm.getId().equals("PUNKTY"))
                .min(Comparator.comparingInt(Promotion::getDiscount))
                .orElseThrow(() -> new IllegalStateException("No payment method available with limit >= " + remainingValue));
        updatePaymentUsage(card, remainingValue);
    }

    protected void payWholePriceAfterDiscount(Promotion promotion, double valueAfterDiscount) {
        promotionSummary.put(promotion.getId(), promotionSummary.get(promotion.getId()) + valueAfterDiscount);
        availablePromotions.stream().filter(pm -> pm.getId().equals(promotion.getId()))
                .findFirst()
                .ifPresent(pm -> pm.setLimit(pm.getLimit() - valueAfterDiscount));
    }

    protected void updatePaymentUsage(Promotion promotion, double valueUsed) {
        promotionSummary.put(promotion.getId(), promotionSummary.get(promotion.getId()) + valueUsed);
        promotion.setLimit(promotion.getLimit() - valueUsed);
    }

}


