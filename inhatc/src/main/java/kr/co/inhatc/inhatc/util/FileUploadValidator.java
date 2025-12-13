package kr.co.inhatc.inhatc.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 업로드 보안 검증 유틸리티
 * 
 * 검증 항목:
 * 1. 파일 확장자 검증
 * 2. MIME 타입 검증
 * 3. 파일명 검증 (경로 탐색 공격 방지)
 * 4. 파일 크기 검증
 */
@Slf4j
public class FileUploadValidator {

    // 허용된 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = 
        Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    // 허용된 MIME 타입
    private static final List<String> ALLOWED_MIME_TYPES = 
        Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
        );

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 파일 업로드 검증
     * 
     * @param file 업로드할 파일
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 1. 파일명 검증 (경로 탐색 공격 방지)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        validateFilename(originalFilename);

        // 2. 파일 확장자 검증
        String extension = getFileExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.warn("허용되지 않은 파일 확장자: {} (파일명: {})", extension, originalFilename);
            throw new IllegalArgumentException(
                String.format("허용되지 않은 파일 형식입니다. 허용된 형식: %s", 
                    String.join(", ", ALLOWED_EXTENSIONS))
            );
        }

        // 3. MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("허용되지 않은 MIME 타입: {} (파일명: {})", contentType, originalFilename);
            throw new IllegalArgumentException(
                String.format("허용되지 않은 파일 형식입니다. 허용된 MIME 타입: %s", 
                    String.join(", ", ALLOWED_MIME_TYPES))
            );
        }

        // 4. 파일 크기 검증
        long fileSize = file.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            log.warn("파일 크기 초과: {} bytes (최대: {} bytes) (파일명: {})", 
                fileSize, MAX_FILE_SIZE, originalFilename);
            throw new IllegalArgumentException(
                String.format("파일 크기가 너무 큽니다. 최대 크기: %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // 5. 파일명 길이 검증
        if (originalFilename.length() > 255) {
            throw new IllegalArgumentException("파일명이 너무 깁니다. (최대 255자)");
        }

        log.debug("파일 검증 성공: {} (크기: {} bytes, 타입: {})", 
            originalFilename, fileSize, contentType);
    }

    /**
     * 파일명 검증 (경로 탐색 공격 방지)
     * 
     * @param filename 파일명
     * @throws IllegalArgumentException 잘못된 파일명인 경우
     */
    private static void validateFilename(String filename) {
        // 경로 탐색 공격 방지
        if (filename.contains("..") || 
            filename.contains("/") || 
            filename.contains("\\") ||
            filename.contains("\0") || // null byte
            filename.contains("%00")) { // URL 인코딩된 null byte
            log.warn("경로 탐색 공격 시도 감지: {}", filename);
            throw new IllegalArgumentException("잘못된 파일명입니다. 경로 문자가 포함되어 있습니다.");
        }

        // Windows 예약 문자 검증
        String[] reservedChars = {":", "*", "?", "\"", "<", ">", "|"};
        for (String reserved : reservedChars) {
            if (filename.contains(reserved)) {
                log.warn("예약 문자 포함 파일명: {}", filename);
                throw new IllegalArgumentException("파일명에 사용할 수 없는 문자가 포함되어 있습니다.");
            }
        }
    }

    /**
     * 파일 확장자 추출
     * 
     * @param filename 파일명
     * @return 확장자 (소문자), 확장자가 없으면 null
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 안전한 파일명 생성 (경로 탐색 방지)
     * 
     * @param originalFilename 원본 파일명
     * @param prefix 접두사 (예: timestamp)
     * @return 안전한 파일명
     */
    public static String generateSafeFilename(String originalFilename, String prefix) {
        String extension = getFileExtension(originalFilename);
        if (extension == null) {
            extension = "png"; // 기본값
        }

        // 접두사와 확장자만 사용하여 안전한 파일명 생성
        return prefix + "." + extension;
    }

    /**
     * 파일 크기 제한 가져오기
     * 
     * @return 최대 파일 크기 (bytes)
     */
    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * 허용된 확장자 목록 가져오기
     * 
     * @return 허용된 확장자 목록
     */
    public static List<String> getAllowedExtensions() {
        return ALLOWED_EXTENSIONS;
    }
}

