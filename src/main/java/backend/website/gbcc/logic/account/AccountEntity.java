package backend.website.gbcc.logic.account;

import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.BaseEntity;
import backend.website.gbcc.model.convector.AccountRoleConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
public class AccountEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Convert(converter = AccountRoleConverter.class)
    @Column(nullable = false)
    private AccountRole role;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;
}
