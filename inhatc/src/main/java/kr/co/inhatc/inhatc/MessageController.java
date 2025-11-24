package kr.co.inhatc.inhatc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.dto.ConversationDTO;
import kr.co.inhatc.inhatc.dto.MessageDTO;
import kr.co.inhatc.inhatc.service.MessageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/history")
    public ResponseEntity<List<MessageDTO>> getMessageHistory(
            @RequestParam("sender") String sender,
            @RequestParam("receiver") String receiver) {
        System.out.println("========================================");
        System.out.println("[DEBUG] MessageController.getMessageHistory 호출됨");
        System.out.println("  - sender 파라미터: " + sender);
        System.out.println("  - receiver 파라미터: " + receiver);
        
        try {
            if (sender == null || sender.trim().isEmpty()) {
                System.out.println("  ❌ sender가 null이거나 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            if (receiver == null || receiver.trim().isEmpty()) {
                System.out.println("  ❌ receiver가 null이거나 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            List<MessageDTO> messages = messageService.getMessageHistory(sender, receiver);
            System.out.println("  ✅ 메시지 히스토리 조회 완료: " + messages.size() + "개");
            System.out.println("========================================");
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("  ❌ 메시지 히스토리 조회 중 오류 발생:");
            e.printStackTrace();
            System.out.println("========================================");
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(
            @RequestParam("userEmail") String userEmail) {
        System.out.println("========================================");
        System.out.println("[DEBUG] MessageController.getConversations 호출됨");
        System.out.println("  - userEmail 파라미터: " + userEmail);
        
        try {
            if (userEmail == null || userEmail.trim().isEmpty()) {
                System.out.println("  ❌ userEmail이 null이거나 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            List<ConversationDTO> conversations = messageService.getConversations(userEmail);
            System.out.println("  ✅ 대화 상대 목록 조회 완료: " + conversations.size() + "개");
            System.out.println("========================================");
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            System.err.println("  ❌ 대화 상대 목록 조회 중 오류 발생:");
            e.printStackTrace();
            System.out.println("========================================");
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
            @RequestParam("userEmail") String userEmail) {
        System.out.println("========================================");
        System.out.println("[DEBUG] MessageController.uploadMessageImage 호출됨");
        System.out.println("  - userEmail: " + userEmail);
        System.out.println("  - 파일명: " + (file != null ? file.getOriginalFilename() : "null"));
        
        Map<String, String> response = new HashMap<>();
        
        try {
            if (file == null || file.isEmpty()) {
                System.out.println("  ❌ 파일이 비어있음");
                response.put("error", "파일이 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 메시지 이미지 저장 경로
            String baseDir = "C:/Users/jdajs/spring test/inhatc/src/main/resources/static";
            Path userDir = Paths.get(baseDir, userEmail, "messages");
            
            // 디렉토리 생성
            if (!Files.exists(userDir)) {
                Files.createDirectories(userDir);
                System.out.println("  ✅ 디렉토리 생성: " + userDir);
            }
            
            // 파일 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = "png"; // 기본값
            if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            }
            
            // 새 파일명 생성 (날짜 + 밀리초)
            String newFilename = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "." + extension;
            Path filePath = userDir.resolve(newFilename);
            
            // 파일 저장
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✅ 파일 저장 완료: " + filePath);
            
            // DB에 저장할 상대 경로 (WebConfig에서 /static/** 매핑 사용)
            String imagePath = "/static/" + userEmail + "/messages/" + newFilename;
            System.out.println("  ✅ 이미지 경로: " + imagePath);
            
            response.put("imagePath", imagePath);
            response.put("message", "이미지 업로드 성공");
            System.out.println("========================================");
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            System.err.println("  ❌ 이미지 업로드 중 오류 발생:");
            e.printStackTrace();
            System.out.println("========================================");
            response.put("error", "이미지 업로드 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

