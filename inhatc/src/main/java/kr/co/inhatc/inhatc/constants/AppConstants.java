package kr.co.inhatc.inhatc.constants;

/**
 * 애플리케이션 전역 상수 클래스
 * 하드코딩된 값들을 중앙 집중식으로 관리
 */
public final class AppConstants {

    private AppConstants() {
        // 인스턴스화 방지
        throw new AssertionError("Cannot instantiate constants class");
    }

    /**
     * 파일 업로드 관련 상수
     */
    public static final class FileUpload {
        private FileUpload() {}

        // 파일 크기 제한 (바이트 단위)
        public static final long MAX_FILE_SIZE_DEV = 2 * 1024 * 1024; // 2MB (개발 환경)
        public static final long MAX_FILE_SIZE_PROD = 10 * 1024 * 1024; // 10MB (프로덕션 환경)
        
        // 파일 크기 제한 (사용자 친화적 메시지용)
        public static final String MAX_FILE_SIZE_DEV_STR = "2MB";
        public static final String MAX_FILE_SIZE_PROD_STR = "10MB";
    }

    /**
     * 에러 메시지 상수
     */
    public static final class ErrorMessage {
        private ErrorMessage() {}

        // 파일 업로드 관련
        public static final String FILE_EMPTY = "파일이 비어있습니다.";
        public static final String FILE_VALIDATION_FAILED = "파일 검증 실패: %s";
        public static final String UPLOAD_FAILED = "업로드 실패: %s";
        public static final String UPLOAD_SERVER_ERROR = "업로드 실패: 서버 오류가 발생했습니다.";
        public static final String IMAGE_UPLOAD_FAILED = "이미지 업로드 실패: 서버 오류가 발생했습니다.";
        public static final String IMAGE_UPLOAD_SUCCESS = "이미지 업로드 성공";
        
        // 게시물 관련
        public static final String POST_UPLOAD_FAILED = "게시물 업로드 실패: %s";
        public static final String POST_UPLOAD_SUCCESS = "게시물 업로드 성공";
        
        // 프로필 관련
        public static final String PROFILE_UPLOAD_FAILED = "프로필 이미지 업로드 실패 (검증 오류): %s";
        
        // 인증 관련
        public static final String LOGIN_REQUIRED = "로그인이 필요합니다.";
        public static final String LOGIN_FAILED = "이메일 또는 비밀번호가 올바르지 않습니다.";
        public static final String UNAUTHORIZED_ACCESS = "본인의 프로필만 수정할 수 있습니다.";
        
        // 디렉토리 관련
        public static final String DIRECTORY_CREATION_FAILED = "사용자 디렉토리 생성 실패: %s";
    }

    /**
     * 성공 메시지 상수
     */
    public static final class SuccessMessage {
        private SuccessMessage() {}

        public static final String POST_UPLOAD_SUCCESS = "게시물 업로드 성공";
        public static final String IMAGE_UPLOAD_SUCCESS = "이미지 업로드 성공";
        public static final String PROFILE_UPLOAD_SUCCESS = "프로필 이미지 업로드 성공";
    }

    /**
     * 파일 경로 관련 상수
     */
    public static final class FilePath {
        private FilePath() {}

        // URL 경로
        public static final String POSTS_URL_PREFIX = "/posts/";
        public static final String PROFILE_URL_PREFIX = "/images/";
        public static final String MESSAGES_URL_PREFIX = "/static/";
        public static final String MESSAGES_SUBDIR = "messages";
        
        // 파일명 형식
        public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";
    }

    /**
     * 로그 메시지 상수
     */
    public static final class LogMessage {
        private LogMessage() {}

        // 파일 업로드
        public static final String FILE_SAVED = "파일 저장 완료: 사용자={}, URL={}";
        public static final String DIRECTORY_CREATED = "디렉토리 생성: {}";
        public static final String FILE_VALIDATION_FAILED = "파일 검증 실패: {}";
        
        // 인증
        public static final String LOGIN_SUCCESS = "로그인 성공: {}";
        public static final String LOGIN_FAILED = "로그인 실패: {}";
        public static final String LOGOUT_SUCCESS = "로그아웃: 세션 무효화 완료";
        
        // 게시물
        public static final String POST_UPLOAD_SUCCESS = "게시물 업로드 성공: 사용자={}, 파일={}";
        public static final String POST_UPLOAD_FAILED = "게시물 업로드 실패 (잘못된 요청): {}";
    }
}

