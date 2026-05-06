package backend.website.gbcc.logic.crm.metrics;

import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.lead.CrmLeadEntity;
import backend.website.gbcc.logic.crm.lead.CrmLeadRepository;
import backend.website.gbcc.logic.crm.lead.CrmLeadSpecification;
import backend.website.gbcc.logic.crm.lead.CrmLeadStatus;
import backend.website.gbcc.logic.crm.metrics.dto.CrmMetricsSummaryDto;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationSpecification;
import backend.website.gbcc.logic.crm.supply.CrmSupplyEntity;
import backend.website.gbcc.logic.crm.supply.CrmSupplyRepository;
import backend.website.gbcc.logic.crm.supply.CrmSupplyVisibility;
import backend.website.gbcc.logic.crm.task.CrmTaskEntity;
import backend.website.gbcc.logic.crm.task.CrmTaskRepository;
import backend.website.gbcc.logic.crm.task.CrmTaskSpecification;
import backend.website.gbcc.logic.crm.task.CrmTaskStatus;
import backend.website.gbcc.logic.crm.task.CrmTaskVisibility;
import backend.website.gbcc.model.AccountPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmMetricsServiceImpl implements CrmMetricsService {

    private final CrmOrganizationRepository organizationRepository;
    private final CrmLeadRepository leadRepository;
    private final CrmTaskRepository taskRepository;
    private final CrmSupplyRepository supplyRepository;
    private final CrmAccessPolicy crmAccessPolicy;

    @Override
    @Transactional(readOnly = true)
    public CrmMetricsSummaryDto summary() {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();

        Specification<CrmOrganizationEntity> orgSpec = CrmOrganizationSpecification.accessibleBy(principal);
        long orgCount = organizationRepository.count(orgSpec);

        Specification<CrmLeadEntity> leadBase = CrmLeadSpecification.accessibleBy(principal);
        long leadCount = leadRepository.count(leadBase);

        Map<String, Long> byStatus = new HashMap<>();
        for (CrmLeadStatus st : CrmLeadStatus.values()) {
            Specification<CrmLeadEntity> spec = Specification.allOf(leadBase, (root, q, cb) -> cb.equal(root.get("status"), st));
            byStatus.put(st.name(), leadRepository.count(spec));
        }

        Specification<CrmTaskEntity> taskVis = CrmTaskVisibility.visibleFor(principal);
        long openTasks = taskRepository.count(Specification.allOf(taskVis, CrmTaskSpecification.statusEq(CrmTaskStatus.OPEN)));
        Instant now = Instant.now();
        long overdue = taskRepository.count(Specification.allOf(
                taskVis,
                CrmTaskSpecification.statusEq(CrmTaskStatus.OPEN),
                (root, q, cb) -> cb.lessThan(root.get("dueAt"), now)
        ));

        long supplies = supplyRepository.count(Specification.allOf(CrmSupplyVisibility.visibleFor(principal)));

        return new CrmMetricsSummaryDto(orgCount, leadCount, Map.copyOf(byStatus), openTasks, overdue, supplies);
    }
}
