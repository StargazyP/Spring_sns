package kr.co.inhatc.inhatc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.exception.CustomException;
import kr.co.inhatc.inhatc.exception.ErrorCode;
import kr.co.inhatc.inhatc.repository.LikeRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PostService postService;

    private MemberEntity testMember;
    private PostEntity testPost;

    @BeforeEach
    void setUp() {
        // 테스트용 MemberEntity 생성
        testMember = MemberEntity.builder()
                .id(1L)
                .memberEmail("test@example.com")
                .memberName("테스트 사용자")
                .memberPassword("encodedPassword")
                .build();

        // 테스트용 PostEntity 생성
        testPost = PostEntity.builder()
                .id(1L)
                .memberEmail("test@example.com")
                .content("테스트 게시글 내용")
                .hits(0)
                .love(0)
                .deleteYn('N')
                .build();

        // @Value 필드 주입 (리플렉션 사용)
        ReflectionTestUtils.setField(postService, "postsUploadDir", "/test/uploads/posts");
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공")
    void findById_Success() {
        // given
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(memberRepository.findByMemberEmail(testPost.getMemberEmail()))
                .thenReturn(Optional.of(testMember));

        // when
        PostResponseDTO result = postService.findById(postId);

        // then
        assertNotNull(result);
        assertEquals(testPost.getId(), result.getId());
        assertEquals(testPost.getContent(), result.getContent());
        verify(postRepository, times(1)).findById(postId);
        verify(memberRepository, times(1)).findByMemberEmail(testPost.getMemberEmail());
    }

    @Test
    @DisplayName("게시글 ID로 조회 실패 - 존재하지 않는 게시글")
    void findById_NotFound() {
        // given
        Long postId = 999L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            postService.findById(postId);
        });

        assertEquals(ErrorCode.POSTS_NOT_FOUND, exception.getErrorCode());
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("삭제되지 않은 게시글 전체 조회")
    void findAll_Success() {
        // given
        List<PostEntity> posts = new ArrayList<>();
        posts.add(testPost);
        
        when(postRepository.findAllWithCommentsByDeleteYn('N')).thenReturn(posts);
        when(memberRepository.findByMemberEmailIn(anyList()))
                .thenReturn(List.of(testMember));

        // when
        List<PostResponseDTO> result = postService.findAll();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postRepository, times(1)).findAllWithCommentsByDeleteYn('N');
        verify(memberRepository, times(1)).findByMemberEmailIn(anyList());
    }

    @Test
    @DisplayName("페이징으로 게시글 조회")
    void findAll_WithPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<PostEntity> posts = new ArrayList<>();
        posts.add(testPost);
        Page<PostEntity> postPage = new PageImpl<>(posts, pageable, 1);

        when(postRepository.findByDeleteYnOrderByCreatedDateDesc('N', pageable))
                .thenReturn(postPage);
        when(memberRepository.findByMemberEmailIn(anyList()))
                .thenReturn(List.of(testMember));

        // when
        Page<PostResponseDTO> result = postService.findAll(pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(postRepository, times(1)).findByDeleteYnOrderByCreatedDateDesc('N', pageable);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void delete_Success() {
        // given
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(PostEntity.class))).thenReturn(testPost);

        // when
        postService.delete(postId);

        // then
        assertEquals('Y', testPost.getDeleteYn());
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).save(testPost);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    void delete_NotFound() {
        // given
        Long postId = 999L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            postService.delete(postId);
        });

        assertEquals(ErrorCode.POSTS_NOT_FOUND, exception.getErrorCode());
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).save(any());
    }
}

