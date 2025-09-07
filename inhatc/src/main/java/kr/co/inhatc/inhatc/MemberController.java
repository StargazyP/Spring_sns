package kr.co.inhatc.inhatc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.service.MemberService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("loginEmail");
        if (email == null) {
            return "redirect:/"; // 로그인 안 되어 있으면 로그인 페이지로
        }

        MemberDTO member = memberService.getMemberByEmail(email); // DB에서 회원 정보 조회
        model.addAttribute("loggedInMember", member); // 변수명 변경

        return "mypage"; // templates/mypage.html 렌더링
    }

    /**
     * 회원가입
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody MemberDTO memberDTO) {
        memberService.save(memberDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        MemberDTO memberDTO = memberService.login(email, password);

        if (memberDTO != null) {
            // 세션에 이메일 저장
            session.setAttribute("loginEmail", email);
            // 로그인 성공 시 main.html로 리다이렉트
            return "main";
        } else {
            // 로그인 실패 시 에러 메시지 전달 후 로그인 페이지로
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return "login"; // login.html
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    /**
     * 현재 로그인된 사용자 정보 가져오기
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, String>> getSessionMember(HttpSession session) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        Map<String, String> response = new HashMap<>();
        response.put("loginEmail", loginEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * 마이페이지 (이메일 기반 조회)
     */
    @GetMapping("/{email}/mypage")
    public ResponseEntity<MemberDTO> getMyPage(@PathVariable String email) {
        return ResponseEntity.ok(memberService.getMemberByEmail(email));
    }

    /**
     * 프로필 사진 업로드
     */
    @PostMapping("/{email}/upload-profile")
    public ResponseEntity<String> uploadProfile(@PathVariable String email,
            @RequestParam("file") MultipartFile file) {
        try {
            String result = memberService.storeFile(file, email);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("업로드 실패: " + e.getMessage());
        }
    }
}
