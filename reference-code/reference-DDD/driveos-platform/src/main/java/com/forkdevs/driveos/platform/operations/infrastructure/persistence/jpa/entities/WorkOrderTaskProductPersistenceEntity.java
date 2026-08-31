package com.forkdevs.driveos.platform.operations.infrastructure.persistence.jpa.entities;

import com.forkdevs.driveos.platform.operations.domain.model.valueobjects.ProductId;
import com.forkdevs.driveos.platform.operations.domain.model.valueobjects.Quantity;
import com.forkdevs.driveos.platform.operations.infrastructure.persistence.jpa.converters.QuantityAttributeConverter;
import com.forkdevs.driveos.platform.shared.domain.model.valueobjects.BranchId;
import com.forkdevs.driveos.platform.shared.domain.model.valueobjects.Money;
import com.forkdevs.driveos.platform.shared.infrastructure.persistence.jpa.converters.MoneyAttributeConverter;
import com.forkdevs.driveos.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import org.springframework.data.domain.Persistable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "work_order_task_products")
@SQLDelete(sql = "UPDATE work_order_task_products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkOrderTaskProductPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_id", nullable = false))
    private ProductId productId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "branch_id", nullable = false))
    private BranchId branchId;

    @Column(nullable = false)
    @Convert(converter = QuantityAttributeConverter.class)
    private Quantity quantity;

    @Column(name = "unit_price", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money unitPrice;

    @Column(name = "total_amount", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money totalAmount;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;
}
