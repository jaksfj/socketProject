package edu.network.musicweb.webserver.service;

import edu.network.musicweb.webserver.tcp.TcpClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TcpClientService {

    @Autowired
    private TcpClientManager tcpClientManager; // 클라이언트 상태 관리 클래스 주입

    // 닉네임 등록
    public String register(String nickname) {
        return tcpClientManager.registerNickname(nickname);
    }

    // 명령어 전송
    public String sendCommand(String nickname, String command) {
        return tcpClientManager.sendCommand(nickname, command);
    }
}
