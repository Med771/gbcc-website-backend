package backend.website.gbcc.logic.crm.map;

import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crm_company_object")
@Getter
@Setter
@NoArgsConstructor
public class CrmCompanyObjectEntity extends BaseEntity {

    @Column(nullable = false, length = 512)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "comment_text", columnDefinition = "text")
    private String commentText;
}
