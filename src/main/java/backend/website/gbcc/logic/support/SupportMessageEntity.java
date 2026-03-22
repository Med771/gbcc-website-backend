package backend.website.gbcc.logic.support;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.model.BaseEntity;
import backend.website.gbcc.model.SupportMessageAuthorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "support_message")
@Getter
@Setter
@NoArgsConstructor
public class SupportMessageEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private SupportConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 32)
    private SupportMessageAuthorType authorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_account_id")
    private AccountEntity authorAccount;

    @Column(nullable = false, columnDefinition = "text")
    private String body;
}
