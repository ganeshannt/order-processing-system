package io.ganeshannt.asm.ops.exception;


public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public static OrderNotFoundException withId(Long id) {
        return new OrderNotFoundException("Order not found with ID: " + id);
    }
}
