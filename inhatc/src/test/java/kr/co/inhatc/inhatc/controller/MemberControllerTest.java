package kr.co.inhatc.inhatc.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.inhatc.inhatc.MemberController;
import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.service.MemberService;

@WebMvcTest(MemberController.class)
@DisplayName("MemberController 통합 테스트")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    private MemberDTO testMemberDTO;

    @BeforeEach
    void setUp() {
        testMemberDTO = MemberDTO.builder()
                .id(1L)
                .memberEmail("test@example.com")
                .memberName("테스트 사용자")
                .build();
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        when(memberService.login("test@example.com", "password123"))
                .thenReturn(testMemberDTO);

        // when & then
        mockMvc.perform(post("/api/members/login")
                .param("email", "test@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));

        verify(memberService, times(1)).login("test@example.com", "password123");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격증명")
    void login_Fail_WrongCredentials() throws Exception {
        // given
        when(memberService.login("test@example.com", "wrongpassword"))
                .thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/members/login")
                .param("email", "test@example.com")
                .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        verify(memberService, times(1)).login("test@example.com", "wrongpassword");
    }

    @Test
    @DisplayName("세션 정보 조회 성공")
    void getSessionMember_Success() throws Exception {
        // given
        // 세션은 MockHttpSession으로 자동 생성됨

        // when & then
        mockMvc.perform(get("/api/members/session")
                .sessionAttr("loginEmail", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginEmail").value("test@example.com"));
    }

    @Test
    @DisplayName("세션 정보 조회 - 세션 없음")
    void getSessionMember_NoSession() throws Exception {
        // when & then
        mockMvc.perform(get("/api/members/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginEmail").isEmpty());
    }
}

