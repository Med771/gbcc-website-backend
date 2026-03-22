package backend.website.gbcc.model;

public final class OrderPaymentMethodLabels {

    private OrderPaymentMethodLabels() {
    }

    public static String ru(OrderPaymentMethod method) {
        if (method == null) {
            return "";
        }
        return switch (method) {
            case CARD_ONLINE -> "Картой онлайн";
            case CARD_OR_ON_RECEIPT -> "Картой / при получении";
        };
    }
}
