package backend.website.gbcc.logic.order;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.model.BaseEntity;
import backend.website.gbcc.model.OrderStatus;
import backend.website.gbcc.model.convector.OrderStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusHistoryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "from_status")
    private OrderStatus fromStatus;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "to_status", nullable = false)
    private OrderStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_account_id")
    private AccountEntity changedByAccount;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "estimated_delivery_at")
    private Instant estimatedDeliveryAt;
}
