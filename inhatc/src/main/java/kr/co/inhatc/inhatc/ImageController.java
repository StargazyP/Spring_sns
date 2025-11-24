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

import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.service.PostService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final PostService postService;

    @GetMapping("/{email}/{filename:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String email,
            @PathVariable String filename) throws MalformedURLException {

        // 실제 로컬 경로
        Path file = Paths.get("C:/uploads/images", email, filename);

        // 파일이 없으면 기본 이미지 사용
        if (!Files.exists(file)) {
            file = Paths.get("C:/uploads/images/default-profile.png");
        }

        Resource resource = new UrlResource(file.toUri());

        // Content-Type 설정
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
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
            // 실제 파일 경로는 "C:/Users/jdajs/spring test/inhatc/src/main/java/kr/co/inhatc/inhatc/{email}/{filename}"
            String imgsource = post.getImgsource();
            String[] parts = imgsource.replace("/posts/", "").split("/", 2);
            
            if (parts.length != 2) {
                return ResponseEntity.notFound().build();
            }

            String email = parts[0];
            String filename = parts[1];
            Path file = Paths.get("C:/Users/jdajs/spring test/inhatc/src/main/java/kr/co/inhatc/inhatc", email, filename);

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
