package kr.co.inhatc.inhatc.dto;

import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.entity.PostEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostRequestDTO {

    private String content;
    private String imgsource;
    private char deleteYn = 'N';
    private MultipartFile file;
    private int love;

    public PostRequestDTO(String content, String imgsource, char deleteYn, MultipartFile file, int love) {
        this.content = content;
        this.imgsource = imgsource;
        this.deleteYn = deleteYn;
        this.file = file;
        this.love = love;
    }

    public PostEntity toEntity(String memberEmail, String filePath) {
        return PostEntity.builder()
                .content(content)
                .imgsource(filePath != null ? filePath : imgsource)
                .deleteYn(deleteYn)
                .hits(0)
                .love(love)
                .memberEmail(memberEmail)
                .build();
    }
}
