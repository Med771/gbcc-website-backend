package backend.website.gbcc.logic.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

    @Query("""
            select distinct oi
            from OrderItemEntity oi
            join fetch oi.product p
            left join fetch p.productClass
            left join fetch p.productSeries
            where oi.order.id = :orderId
            order by oi.createdAt asc
            """)
    List<OrderItemEntity> findAllByOrderId(UUID orderId);
}
