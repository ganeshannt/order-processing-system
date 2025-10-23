package io.ganeshannt.asm.ops.scheduler;

import io.ganeshannt.asm.ops.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final IOrderService orderService;

    /**
     * Runs every 5 minutes to process pending orders.
     * The business logic is delegated to the service layer.
     */
    @Scheduled(fixedRateString = "PT5M")
    public void promotePendingOrders() {
        log.info("Starting scheduled order status update task");
        int updatedCount = orderService.promotePendingOrdersToProcessing();
        log.info("Completed scheduled order update: {} orders moved to PROCESSING", updatedCount);
    }
}
