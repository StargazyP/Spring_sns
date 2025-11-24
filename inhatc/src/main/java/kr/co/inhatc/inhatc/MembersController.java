package kr.co.inhatc.inhatc;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.service.MemberService;
import kr.co.inhatc.inhatc.service.PostService;
import lombok.RequiredArgsConstructor;

/**
 * /members/* 경로를 처리하는 컨트롤러
 * 호환성을 위해 유지
 */
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MembersController {

    private final MemberService memberService;
    private final PostService postService;

    /**
     * 다른 사용자 프로필 조회 (이름으로 - 호환성을 위해 유지)
     * /members/mypage?email={name} 요청을 처리
     * 주의: 파라미터명이 email이지만 실제로는 이름(name)을 받을 수 있음
     */
    @GetMapping("/mypage")
    public String viewUserProfileByName(@RequestParam(value = "email", required = false) String nameOrEmail, 
                                        HttpSession session, Model model) {
        // 로그인 확인
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return "redirect:/";
        }

        if (nameOrEmail == null || nameOrEmail.isEmpty()) {
            return "redirect:/main";
        }

        try {
            MemberDTO viewedMember = null;
            String userEmail = null;

            // 먼저 이메일 형식인지 확인 (@ 포함 여부)
            if (nameOrEmail.contains("@")) {
                // 이메일 형식이면 이메일로 찾기
                try {
                    viewedMember = memberService.getMemberByEmail(nameOrEmail);
                    if (viewedMember != null) {
                        userEmail = viewedMember.getMemberEmail();
                    }
                } catch (RuntimeException e) {
                    System.err.println("이메일로 사용자를 찾을 수 없습니다: " + nameOrEmail);
                    e.printStackTrace();
                    return "redirect:/main";
                } catch (Exception e) {
                    System.err.println("예상치 못한 에러 발생: " + e.getMessage());
                    e.printStackTrace();
                    return "redirect:/main";
                }
            } else {
                // 이름 형식이면 이름으로 찾기
                try {
                    viewedMember = memberService.getMemberByName(nameOrEmail);
                    if (viewedMember != null) {
                        userEmail = viewedMember.getMemberEmail();
                    }
                } catch (RuntimeException e) {
                    System.err.println("이름으로 사용자를 찾을 수 없습니다: " + nameOrEmail);
                    e.printStackTrace();
                    return "redirect:/main";
                } catch (Exception e) {
                    System.err.println("예상치 못한 에러 발생: " + e.getMessage());
                    e.printStackTrace();
                    return "redirect:/main";
                }
            }

            if (viewedMember == null || userEmail == null || userEmail.isEmpty()) {
                System.err.println("사용자 정보를 가져올 수 없습니다. nameOrEmail: " + nameOrEmail);
                return "redirect:/main";
            }

            model.addAttribute("viewedMember", viewedMember);
            model.addAttribute("loggedInMember", viewedMember); // mypage.html 호환성을 위해 추가

            // 세션 유저와 조회 중인 유저가 같은지 확인
            boolean isOwnerPrimitive = loginEmail != null && loginEmail.equals(userEmail);
            Boolean isOwnerValue = Boolean.valueOf(isOwnerPrimitive); // 명시적으로 Boolean 객체로 변환
            model.addAttribute("isOwner", isOwnerValue);
            model.addAttribute("loginEmail", loginEmail);
            model.addAttribute("viewedEmail", userEmail); // 디버깅을 위해 추가
            
            // 상세 디버깅을 위한 로그
            System.out.println("========================================");
            System.out.println("[DEBUG] /members/mypage 호출");
            System.out.println("  - 파라미터 nameOrEmail: " + nameOrEmail);
            System.out.println("  - 세션 loginEmail: " + loginEmail);
            System.out.println("  - 조회 userEmail: " + userEmail);
            System.out.println("  - viewedMember.memberEmail: " + (viewedMember != null ? viewedMember.getMemberEmail() : "null"));
            System.out.println("  - loginEmail.equals(userEmail): " + (loginEmail != null ? loginEmail.equals(userEmail) : "loginEmail is null"));
            System.out.println("  - isOwner (primitive): " + isOwnerPrimitive);
            System.out.println("  - isOwner (Boolean 객체): " + isOwnerValue);
            System.out.println("  - isOwner 타입: " + (isOwnerValue != null ? isOwnerValue.getClass().getName() : "null"));
            System.out.println("========================================");

            // 해당 사용자의 게시글 목록 조회
            try {
                List<PostResponseDTO> userPosts = postService.findByMemberEmail(userEmail);
                model.addAttribute("userPosts", userPosts);
                model.addAttribute("userpost", userPosts); // mypage.html 호환성을 위해 추가
            } catch (Exception e) {
                System.err.println("게시글 조회 중 에러 발생: " + e.getMessage());
                e.printStackTrace();
                // 게시글 조회 실패해도 프로필은 표시
                model.addAttribute("userPosts", java.util.Collections.emptyList());
                model.addAttribute("userpost", java.util.Collections.emptyList());
            }

            return "mypage";
        } catch (Exception e) {
            System.err.println("전체 에러 발생: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/main";
        }
    }
}

