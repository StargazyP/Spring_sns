package kr.co.inhatc.inhatc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.posts-dir}")
    private String postsUploadDir;
    
    @Value("${app.upload.profile-dir}")
    private String profileUploadDir;
    
    @Value("${app.upload.messages-dir}")
    private String messagesUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 기존 static/images 접근 유지
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        // 게시물 업로드 이미지 외부 경로 매핑
        // Windows와 Linux 모두 지원하도록 file:// 프로토콜 사용
        String postsFileUrl = convertToFileUrl(postsUploadDir);
        registry.addResourceHandler("/posts/**")
                .addResourceLocations(postsFileUrl);
        
        // 프로필 이미지 및 메시지 이미지 접근 (static 폴더의 사용자별 폴더)
        // /static/** 경로로 profile과 messages 디렉토리 모두 접근 가능
        String profileFileUrl = convertToFileUrl(profileUploadDir);
        String messagesFileUrl = convertToFileUrl(messagesUploadDir);
        registry.addResourceHandler("/static/**")
                .addResourceLocations(profileFileUrl, messagesFileUrl);
    }
    
    /**
     * 파일 경로를 file:// URL 형식으로 변환
     * Windows와 Linux 경로 모두 지원
     */
    private String convertToFileUrl(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("경로가 비어있습니다.");
        }
        
        // 백슬래시를 슬래시로 변환
        String normalizedPath = path.replace("\\", "/");
        
        // 경로 끝에 슬래시 추가
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }
        
        // 절대 경로인 경우 (Linux: /로 시작, Windows: C:/ 등으로 시작)
        if (normalizedPath.startsWith("/")) {
            // Linux 경로: file:// 형식
            return "file://" + normalizedPath;
        } else if (normalizedPath.matches("^[A-Za-z]:/.*")) {
            // Windows 경로: file:/// 형식
            return "file:///" + normalizedPath;
        } else {
            // 상대 경로인 경우 (일반적으로 발생하지 않음)
            return "file:///" + normalizedPath;
        }
    }
}