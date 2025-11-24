package kr.co.inhatc.inhatc;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



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
    


    
}
