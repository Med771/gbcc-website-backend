package backend.website.gbcc.logic.product.productphoto;

import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "product_photo")
@Getter
@Setter
@NoArgsConstructor
public class ProductPhotoEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
