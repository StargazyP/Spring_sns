package kr.co.inhatc.inhatc;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.service.PostService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final PostService postService;
    
    @Value("${app.upload.posts-dir}")
    private String postsUploadDir;
    
    @Value("${app.upload.profile-dir}")
    private String profileUploadDir;

    @GetMapping("/{email}/{filename:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String email,
            @PathVariable String filename) throws MalformedURLException {

        // 프로필 이미지 경로 (설정에서 가져온 경로 사용)
        Path file = Paths.get(profileUploadDir, email, filename);

        // 파일이 없으면 기본 이미지 사용
        if (!Files.exists(file)) {
            Path defaultImage = Paths.get(profileUploadDir, "default-profile.png");
            if (Files.exists(defaultImage)) {
                file = defaultImage;
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        Resource resource = new UrlResource(file.toUri());
        
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        // 파일 확장자에 따라 Content-Type 결정
        String contentType = "image/png";
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (lowerFilename.endsWith(".gif")) {
            contentType = "image/gif";
        }

        // Content-Type 설정
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }

    /**
     * 게시글 이미지 조회 (게시글 ID로)
     */
    @GetMapping("/api/image/{id}")
    public ResponseEntity<Resource> getPostImage(@PathVariable Long id) throws MalformedURLException {
        try {
            PostResponseDTO post = postService.findById(id);
            
            if (post == null || post.getImgsource() == null || post.getImgsource().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // imgsource는 "/posts/{email}/{filename}" 형식
            // 설정에서 가져온 게시물 이미지 저장 경로 사용
            String imgsource = post.getImgsource();
            String[] parts = imgsource.replace("/posts/", "").split("/", 2);
            
            if (parts.length != 2) {
                return ResponseEntity.notFound().build();
            }

            String email = parts[0];
            String filename = parts[1];
            Path file = Paths.get(postsUploadDir, email, filename);

            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 파일 확장자에 따라 Content-Type 결정
            String contentType = "image/png";
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerFilename.endsWith(".gif")) {
                contentType = "image/gif";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
