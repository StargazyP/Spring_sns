package kr.co.inhatc.inhatc.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "post_entity")
@BatchSize(size = 20)  // N+1 문제 해결을 위한 배치 크기 설정
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private int hits;
    private char deleteYn = 'N';
    private String imgsource;
    private int love;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    /** 회원 이메일 직접 저장 */
    @Column(name = "member_email", nullable = false)
    private String memberEmail;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @BatchSize(size = 20)  // Comments를 배치로 로딩하여 N+1 문제 해결
    private List<CommentEntity> comments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "member_email", referencedColumnName = "member_email")
    )
    private Set<MemberEntity> lovedBy = new HashSet<>();

    /** 비즈니스 로직 */
    public void updateContent(String content) {
        this.content = content;
        this.modifiedDate = LocalDateTime.now();
    }

    public void increaseHits() { this.hits++; }

    public void increaseLove() { this.love++; }

    public void decreaseLove() { if (this.love > 0) this.love--; }

    public void delete() { this.deleteYn = 'Y'; }
}

