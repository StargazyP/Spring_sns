package kr.co.inhatc.inhatc;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.dto.MemberDTO;



@Controller
public class HomeController {

    @GetMapping("/")
    public String login() {
        return "login";  // static/main.html 로 리다이렉트
    }

    @GetMapping("/main")
    public String main() {
        return "main";
    }
    

    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("loginEmail");
        if (email == null) {
            return "redirect:/"; // 로그인 안 되어 있으면 로그인 페이지로
        }

        MemberDTO member = memberService.findByEmail(email); // DB에서 회원 정보 조회
        model.addAttribute("member", member);

        return "mypage"; // templates/mypage.html 렌더링
    }
    
}
