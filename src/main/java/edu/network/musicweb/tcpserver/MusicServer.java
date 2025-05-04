package edu.network.musicweb.tcpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class MusicServer { // TCP 서버의 main 클래스
    public static final int PORT = 12345; // 서버가 열릴 포트번호
    // 현재 서버에 접속한 클라이언틀을 담는 HashMap
    public static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    /*
        한 서버에 여러 사용자가 들어올 수 있어 동시성 문제가 발생할 수 있으므로
        concurrent Hash Map으로 구현
    */

    public static void main(String[] args) { // TCP 서버를 여기서 시작
        // try catch 구문을 써서 자동으로 닫히도록 한다.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // 지정된 PORT 번호로 server socket 을 생성해서 클라이언트의 연결을 기다린다.
            System.out.println("🎵 Music TCP Server started on port " + PORT); // 서버가 정상적으로 열림
            while (true) { // 무한 로프를 돌며 클라이언트의 접속을 기다린다.
                Socket socket = serverSocket.accept(); // Socket 객체를 받아온다.
                // 새로운 클라이언트를 처리할 ClientHandler 객체를 생성
                ClientHandler handler = new ClientHandler(socket, clients);
                handler.start(); // 스레드 시작
            }
        } catch (IOException e) { // 예외 발생 시 오류메시지 출력
            System.err.println("서버 오류: " + e.getMessage());
        }
    }
}

