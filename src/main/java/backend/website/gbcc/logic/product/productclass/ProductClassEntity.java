package backend.website.gbcc.logic.product.productclass;

import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_class")
@Getter
@Setter
@NoArgsConstructor
public class ProductClassEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
}
