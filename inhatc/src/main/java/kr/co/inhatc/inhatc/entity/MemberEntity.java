package kr.co.inhatc.inhatc.entity;



import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table; // Table 어노테이션을 import해야 합니다.
import kr.co.inhatc.inhatc.dto.MemberDTO;
import lombok.Getter;
import lombok.Setter;
@Entity
@Setter
@Getter
@Table(name = "member_entity")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String memberEmail;

    @Column
    private String memberPassword;

    @Column
    private String memberName;
    
    @ManyToMany(mappedBy = "lovedBy")
    private Set<PostEntity> likedPosts = new HashSet<>();

    @Column
    private String profilePicturePath;

    // toMemberEntity 메서드 수정
    public static MemberEntity toMemberEntity(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberDTO.getId());
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberName(memberDTO.getMemberName());
        memberEntity.setProfilePicturePath(memberDTO.getProfilePicturePath());
        return memberEntity;
    }
}
