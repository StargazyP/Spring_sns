package kr.co.inhatc.inhatc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import kr.co.inhatc.inhatc.dto.ConversationDTO;
import kr.co.inhatc.inhatc.dto.MessageDTO;
import kr.co.inhatc.inhatc.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MessageController {
    
    @Value("${app.upload.messages-dir}")
    private String messagesUploadDir;

    private final MessageService messageService;

    @GetMapping("/history")
    public ResponseEntity<List<MessageDTO>> getMessageHistory(
            @RequestParam("sender") String sender,
            @RequestParam("receiver") String receiver) {
        log.debug("메시지 히스토리 조회 요청: sender={}, receiver={}", sender, receiver);
        
        try {
            if (sender == null || sender.trim().isEmpty()) {
                log.warn("메시지 히스토리 조회 실패: sender가 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            if (receiver == null || receiver.trim().isEmpty()) {
                log.warn("메시지 히스토리 조회 실패: receiver가 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            List<MessageDTO> messages = messageService.getMessageHistory(sender, receiver);
            log.debug("메시지 히스토리 조회 완료: {}개", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("메시지 히스토리 조회 중 오류 발생: sender={}, receiver={}", sender, receiver, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(
            @RequestParam("userEmail") String userEmail) {
        log.debug("대화 상대 목록 조회 요청: userEmail={}", userEmail);
        
        try {
            if (userEmail == null || userEmail.trim().isEmpty()) {
                log.warn("대화 상대 목록 조회 실패: userEmail이 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            List<ConversationDTO> conversations = messageService.getConversations(userEmail);
            log.debug("대화 상대 목록 조회 완료: {}개", conversations.size());
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("대화 상대 목록 조회 중 오류 발생: userEmail={}", userEmail, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 메시지 이미지 업로드
     * 저장 경로: C:/Users/jdajs/spring test/inhatc/src/main/resources/static/{userEmail}/messages/{timestamp}.{ext}
     * DB 저장 경로: /images/{userEmail}/messages/{filename}
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadMessageImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userEmail") @jakarta.validation.constraints.NotBlank(message = "이메일은 필수입니다.") 
            @jakarta.validation.constraints.Email(message = "올바른 이메일 형식이 아닙니다.") String userEmail) {
        log.debug("MessageController.uploadMessageImage 호출됨 - userEmail: {}, 파일명: {}", 
            userEmail, file != null ? file.getOriginalFilename() : "null");
        
        Map<String, String> response = new HashMap<>();
        
        try {
            if (file == null || file.isEmpty()) {
                log.warn("파일이 비어있음");
                response.put("error", kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.FILE_EMPTY);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 공통 유틸리티 사용으로 중복 코드 제거
            String imagePath = kr.co.inhatc.inhatc.util.FileUploadService.uploadMessageImage(file, userEmail, messagesUploadDir);
            
            response.put("imagePath", imagePath);
            response.put("message", kr.co.inhatc.inhatc.constants.AppConstants.SuccessMessage.IMAGE_UPLOAD_SUCCESS);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생: userEmail={}", userEmail, e);
            response.put("error", kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.IMAGE_UPLOAD_FAILED);
            return ResponseEntity.status(500).body(response);
        }
    }
}

