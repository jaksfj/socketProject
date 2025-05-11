package edu.network.musicweb.webserver.tcp;

import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TcpClientManager {

    // 닉네임 기준으로 socket, writer, reader 저장
    private final ConcurrentHashMap<String, Socket> socketMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PrintWriter> writerMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BufferedReader> readerMap = new ConcurrentHashMap<>();

    private final String SERVER_HOST = "localhost"; // TCP 서버 주소
    private final int SERVER_PORT = 12345; // TCP 서버 포트

    // 닉네임 등록 및 소켓 연결 유지
    public synchronized String registerNickname(String nickname) {
        try {
            // 이미 등록된 닉네임이면 무시
            if (socketMap.containsKey(nickname)) {
                return "이미 사용중인 닉네임입니다.";
            }

            // 새 socket 생성
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

            // 입출력 스트림 설정
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 닉네임 등록 요청 보내기
            writer.println("/register " + nickname);

            // 서버 응답 받기
            String response = reader.readLine();

            // 성공 시 socket + 스트림 저장
            if (response != null && response.contains("등록 완료")) {
                socketMap.put(nickname, socket);
                writerMap.put(nickname, writer);
                readerMap.put(nickname, reader);
                return response;
            } else {
                socket.close(); // 실패 시 socket 닫기
                return response != null ? response : "서버 응답 없음";
            }

        } catch (IOException e) {
            return "연결 오류: " + e.getMessage();
        }
    }

    // 2등록된 소켓을 통해 명령어 전송
    public synchronized String sendCommand(String nickname, String command) {
        if (!writerMap.containsKey(nickname)) {
            return "닉네임이 먼저 등록되어야 합니다.";
        }

        try {
            PrintWriter writer = writerMap.get(nickname);
            BufferedReader reader = readerMap.get(nickname);

            writer.println(command);
            writer.flush(); // 반드시 필요

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("[END]")) break;
                sb.append(line).append("\n");
            }

            if (command.equals("/disconnect")) { // tcp 서버에서 보낸 소켓 연결해제 요청을 스프링 서버에서도 처리
                Socket socket = socketMap.get(nickname);
                if (socket != null && !socket.isClosed()) socket.close();
                socketMap.remove(nickname);
                writerMap.remove(nickname);
                readerMap.remove(nickname);
                System.out.println("Spring 서버에서" + nickname + " 연결 종료");
            }

            return sb.toString().trim(); // 여러 줄의 응답처리
        } catch (IOException e) {
            return "명령어 전송 오류: " + e.getMessage();
        }
    }
}
