package backend.website.gbcc.logic.managercommission;

import backend.website.gbcc.logic.managercommission.dto.ManagerCommissionAdminDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ManagerCommissionService {

    void onOrderDelivered(UUID orderId);

    Page<ManagerCommissionAdminDto> listForAdmin(UUID managerId, Pageable pageable);
}
