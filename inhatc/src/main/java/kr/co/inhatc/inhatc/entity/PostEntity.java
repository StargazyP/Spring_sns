package kr.co.inhatc.inhatc.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    private String content; // 내용

    private String writer; // 작성자

    private int hits; // 조회 수

    private char deleteYn; // 삭제 여부

    private String imgsource;

    private LocalDateTime createdDate = LocalDateTime.now(); // 생성일

    private LocalDateTime modifiedDate; // 수정일

    private int love; // 좋아요
    
    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @OrderBy("id asc")
    private List<CommentEntity> comments;

    @ManyToMany
    @JoinTable(
        name = "post_likes",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    
    private Set<MemberEntity> lovedBy = new HashSet<>(); // 좋아요 누른 사용자 목록

    @Builder
    public PostEntity(Long id, String content, String writer, int hits, char deleteYn, String imgsource, int love) {
        this.id = id;
        this.content = content;
        this.writer = writer;
        this.hits = hits;
        this.deleteYn = deleteYn;
        this.imgsource = imgsource;
        this.love = love;
        this.lovedBy = new HashSet<>();
    }

    /**
     * 게시글 수정
     */
    public void update(String content) {
        this.content = content;
        this.modifiedDate = LocalDateTime.now();
    }

    public void increaseLoveCount() {
        this.love++;
    }

    public void decreaseLoveCount() {
        if (this.love > 0) {
            this.love--;
        }
    }

    /**
     * 조회 수 증가
     */
    public void increaseHits() {
        this.hits++;
    }

    /**
     * 게시글 삭제
     */
    public void delete() {
        this.deleteYn = 'Y';
    }

    /**
     * 특정 사용자가 좋아요를 눌렀는지 확인
     */
    public boolean isLovedBy(MemberEntity member) {
        return this.lovedBy.contains(member);
    }
}
