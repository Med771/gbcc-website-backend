package backend.website.gbcc.logic.product.producttype;

import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_type")
@Getter
@Setter
@NoArgsConstructor
public class ProductTypeEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
}
