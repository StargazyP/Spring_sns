package kr.co.inhatc.inhatc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 기존 static/images 접근 유지
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        // 게시물 업로드 이미지 외부 경로 매핑
        registry.addResourceHandler("/posts/**")
                .addResourceLocations(
                    "file:///C:/Users/jdajs/spring%20test/inhatc/src/main/java/kr/co/inhatc/inhatc/"
                );
        
        // 프로필 이미지 접근 (static 폴더의 사용자별 폴더)
        registry.addResourceHandler("/static/**")
                .addResourceLocations(
                    "file:///C:/Users/jdajs/spring%20test/inhatc/src/main/resources/static/"
                );
    }
}