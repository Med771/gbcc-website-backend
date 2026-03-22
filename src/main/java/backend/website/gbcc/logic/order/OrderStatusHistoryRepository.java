package backend.website.gbcc.logic.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, UUID> {

    @Query("""
            select h
            from OrderStatusHistoryEntity h
            left join fetch h.changedByAccount
            where h.order.id = :orderId
            order by h.createdAt asc
            """)
    List<OrderStatusHistoryEntity> findAllByOrderId(UUID orderId);
}
