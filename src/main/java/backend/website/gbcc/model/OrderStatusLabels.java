package backend.website.gbcc.model;

/**
 * Подписи статусов заказа для UI (история заказов).
 */
public final class OrderStatusLabels {

    private OrderStatusLabels() {
    }

    public static String ru(OrderStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case CREATED -> "Оформлен";
            case PROCESSING -> "В обработке";
            case PACKED -> "Собран";
            case SHIPPED -> "Доставляется";
            case DELIVERED -> "Доставлен";
            case CANCELLED -> "Отменён";
        };
    }
}
