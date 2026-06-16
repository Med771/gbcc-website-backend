package backend.website.gbcc.logic.customeranalytics;

import backend.website.gbcc.logic.account.AccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_analytics")
@Getter
@Setter
@NoArgsConstructor
public class CustomerAnalyticsEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "account_id")
    private AccountEntity account;

    @Column(name = "manual_total_paid", precision = 14, scale = 2)
    private BigDecimal manualTotalPaid;

    @Column(name = "manual_current_month_paid", precision = 14, scale = 2)
    private BigDecimal manualCurrentMonthPaid;

    @Column(name = "manual_total_orders_count")
    private Integer manualTotalOrdersCount;

    @Column(name = "manual_referred_clients_count")
    private Integer manualReferredClientsCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_account_id")
    private AccountEntity updatedByAccount;
}
