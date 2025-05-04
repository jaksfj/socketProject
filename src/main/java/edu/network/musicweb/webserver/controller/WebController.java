package edu.network.musicweb.webserver.controller;

import edu.network.musicweb.webserver.service.TcpClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {

    @Autowired
    private TcpClientService tcpClientService;

    // 첫 진입 → 닉네임 등록 화면
    @GetMapping("/")
    public String showRegisterPage() {
        return "register"; // register.html
    }

    // 닉네임 등록 처리
    @PostMapping("/register")
    public String registerNickname(@RequestParam String nickname, Model model) {
        String response = tcpClientService.register(nickname);

        // 등록 응답이 아닐경우 main 페이지로 넘어갈 수 없음!
        if (!response.contains("등록 완료")) {
            model.addAttribute("error", response); // 에러 메세지 전달
            return "register"; // 다시 register.html 로 이동
        }

        // 정상 등록 시 main.html로 이동
        model.addAttribute("nickname", nickname);
        model.addAttribute("response", response);
        return "main";
    }

    // 실제 명령어 전송
    @PostMapping("/command")
    public String sendCommand(@RequestParam String nickname,
                              @RequestParam String command,
                              Model model) {
        String response = tcpClientService.sendCommand(nickname, command);

        model.addAttribute("nickname", nickname); // 닉네임 계속 유지
        model.addAttribute("response", response);
        return "main"; // 다시 main 페이지로
    }
}
