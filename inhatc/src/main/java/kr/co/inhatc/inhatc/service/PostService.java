package kr.co.inhatc.inhatc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.dto.PostRequestDTO;
import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.entity.LikeEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.exception.CustomException;
import kr.co.inhatc.inhatc.exception.ErrorCode;
import kr.co.inhatc.inhatc.repository.LikeRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;


    /**
     * 게시글 찾기
     */
    public List<PostResponseDTO> findByUserID(String writer) {
        List<PostEntity> list = postRepository.findByWriter(writer);
        return list.stream().map(PostResponseDTO::new).collect(Collectors.toList());
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public Long delete(final Long id) {

        PostEntity entity = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTS_NOT_FOUND));
        entity.delete();
        return id;
    }

    /**
     * 게시글 리스트 조회
     */
    public List<PostResponseDTO> findAll() {

        Sort sort = Sort.by(Direction.DESC, "id", "createdDate");
        List<PostEntity> list = postRepository.findAll(sort);
        return list.stream().map(PostResponseDTO::new).collect(Collectors.toList());
    }

    /**
     * 게시글 리스트 조회 - (삭제 여부 기준)
     */
    public List<PostResponseDTO> findAllByDeleteYn(final char deleteYn) {

        Sort sort = Sort.by(Direction.DESC, "id", "createdDate");
        List<PostEntity> list = postRepository.findAllByDeleteYn(deleteYn, sort);
        return list.stream().map(PostResponseDTO::new).collect(Collectors.toList());
    }

    /**
     * 게시글 상세정보 조회
     */
    @Transactional
    public PostResponseDTO findById(final Long id) {

        PostEntity entity = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTS_NOT_FOUND));
        entity.increaseHits();
        postRepository.save(entity);
        return new PostResponseDTO(entity);
    }

    public boolean toggleLove(Long postId, String email) {
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> 
            new CustomException(ErrorCode.POSTS_NOT_FOUND)
        );
        MemberEntity user = memberRepository.findByMemberEmail(email).orElseThrow(() -> 
            new CustomException(ErrorCode.METHOD_NOT_ALLOWED)
        );
        Optional<LikeEntity> existingLike = likeRepository.findByPostAndUser(post, user);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            post.decreaseLoveCount();
        } else {
            LikeEntity newLike = new LikeEntity(post, user);
            likeRepository.save(newLike);
            post.increaseLoveCount();
        }
        postRepository.save(post);
        return existingLike.isEmpty(); // 새로 좋아요를 눌렀다면 true, 해제했다면 false // 08/10 internal server error 에러 해결
    }
    


    
    /*
     * 게시글 이미지 업로드
     */

    public String imgupload(MultipartFile file, String userEmail) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("업로드 실패: 파일이 비어 있습니다.");
        }

        String userDirectory = Path.of("C:", "Users", "jdajs", "Downloads", "spring test", "inhatc", "src",
                "main", "resources", userEmail).toString();
        Path userPath = Path.of(userDirectory);
        if (!Files.exists(userPath)) {
            Files.createDirectories(userPath); // 사용자 디렉토리가 없다면 생성
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedNow = now.format(formatter);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        Path filePath = userPath.resolve(formattedNow + extension);

        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }

    public void save(PostRequestDTO params) {
        PostEntity postEntity = params.toEntity();
        postRepository.save(postEntity);
    }

}
