package kr.co.inhatc.inhatc.service;

import org.springframework.stereotype.Service;

@Service
public class LikeService {
    // @Autowired
    // private LikeRepository likeRepository;

    // @Autowired
    // private PostRepository postRepository;

    // @Autowired
    // private MemberRepository memberRepository;

    //   public List<PostLikeResponseDTO> postCheck(String userId) {
    //     List<PostLikeResponseDTO> postLikeDetails = new ArrayList<>();
        
    //     // 이메일을 통해 사용자 찾기
    //     Optional<MemberEntity> memberOptional = memberRepository.findByMemberEmail(userId);
        
    //     if (memberOptional.isEmpty()) {
    //         postLikeDetails.add(new PostLikeResponseDTO(null, "사용자를 찾을 수 없습니다."));
    //         return postLikeDetails;
    //     }
        
    //     MemberEntity member = memberOptional.get();
        
    //     // 사용자가 작성한 게시물 찾기
    //     List<PostEntity> posts = postRepository.findByWriter(userId);
        
    //     for (PostEntity post : posts) {
    //         // 각 게시물에 대한 좋아요 찾기
    //         List<LikeEntity> likes = likeRepository.findByPost(post);
            
    //         for (LikeEntity like : likes) {
    //             if (!like.getUser().equals(member)) {
    //                 String message = "좋아요를 " + like.getUser().getMemberName() + "가 ID " + like.getPost().getId() + " 게시물에 " + like.getLikedDate().toString() + "에 눌렀습니다.";
    //                 Long postId = like.getPost().getId();
                    
    //                 // 메시지와 postId를 담은 DTO 생성
    //                 PostLikeResponseDTO postLikeDetail = new PostLikeResponseDTO(postId, message);
                    
    //                 // 리스트에 추가
    //                 postLikeDetails.add(postLikeDetail);
    //             }
    //         }
    //     }
        
    //     if (postLikeDetails.isEmpty()) {
    //         postLikeDetails.add(new PostLikeResponseDTO(null, "해당 게시물에 좋아요를 누른 사용자가 없습니다."));
    //     }
        
    //     return postLikeDetails;
    // }

    
    
    
}
