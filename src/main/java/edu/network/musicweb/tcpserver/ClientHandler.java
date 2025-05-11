package edu.network.musicweb.tcpserver;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends Thread { // Thread 를 상속해서 멀티스레드 방식으로 클라이언트를 각각 처리
    private Socket socket;
    private BufferedReader in; // 문자열 입력
    private PrintWriter out; // 문자열 출력
    private String nickname; // 닉네임 : 클라이언트 식별자
    private final ConcurrentHashMap<String, ClientHandler> clients; // 모든 클라이언트 목록
    private final Set<String> friends = new HashSet<>();// .


    // 생성자로 클라이언트 socket 및 정보 인스턴스 생성
    public ClientHandler(Socket socket, ConcurrentHashMap<String, ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() { // 스레드 실행 시 자동 호출되는 메서드
        System.out.println("run 진입 확인용");
        try {
            // 클라이언트의 입출력 통신을 위해 스트림을 설정
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("🟡 [" + socket.getInetAddress() + "] 클라이언트 처리 시작");

            // 클라이언트가 메세지를 보내면 서버 콘솔에 출력한다.
            String message;
            while ((message = in.readLine()) != null) {
                if(nickname==null) {
                    if(message.startsWith("/register ")){
                        String input = message.substring(10).trim();
                        synchronized (clients) {
                            if (!clients.containsKey(input)) { // 이미 사용중인 닉네임인지 확인
                                nickname = input;
                                clients.put(nickname, this); // client 회원가입
                                out.println("닉네임 등록 완료: " + nickname);
                                out.println("[END]");
                            } else {
                                out.println("이미 사용 중인 닉네임입니다.");
                                out.println("[END]");
                            }
                        }
                    }else {
                        out.println("먼저 '/register [닉네임]' 으로 회원을 등록해주세요. ");
                        out.println("[END]");
                    }continue;
                }
                if (message.startsWith("/addfriend ")) { // 친구 추가
                    System.out.println("🧩 친구 추가 요청 수신: " + message);
                    String target = message.substring(11).trim(); // 친구 닉네임 추출

                    if (target.equals(nickname)) {
                        out.println("자기 자신은 친구로 추가할 수 없습니다.");
                        out.println("[END]");
                    } else if (!clients.containsKey(target)) {
                        out.println("해당 사용자가 접속해 있지 않습니다.");
                        out.println("[END]");
                    } else if (friends.contains(target)) {
                        out.println("이미 친구입니다!");
                        out.println("[END]");
                    } else {
                        friends.add(target); // 나의 친구 목록에 추가
                        clients.get(target).friends.add(nickname); // 상대방도 나를 친구로 등록
                        out.println(target + "님과 친구가 되었습니다!");
                        out.println("[END]");
                        clients.get(target).sendMessage(nickname + "님이 당신을 친구로 추가했습니다!");
                    }
                }

                else if (message.equals("/friendlist")) { // 친구 목록 조회
                    StringBuilder response = new StringBuilder("친구 목록:\n");
                    if (friends.isEmpty()) {
                        response.append("(친구가 없습니다)");
                    } else {
                        for (String f : friends) {
                            response.append("- ").append(f).append("\n");
                        }
                    }
                    out.println(response.toString().trim());
                    out.println("[END]");
                }

                // 채팅 전송 및 채팅 알림 전송!
                else if(message.startsWith("/sendmusic")) {
                    String[] parts = message.split(" ", 4); // /sendmusic 친구닉 곡제목 메시지

                    if (parts.length < 4) {
                        out.println("사용법: /sendmusic [닉네임] [곡제목] [메시지]");
                        out.println("[END]");
                        return;
                    }

                    String target = parts[1].trim();
                    String songTitle = parts[2].trim();
                    String userMessage = parts[3].trim();

                    if (!clients.containsKey(target)) {
                        out.println("해당 사용자가 접속해 있지 않습니다.");
                        out.println("[END]");
                        return;
                    }

                    if (!friends.contains(target)) {
                        out.println("친구가 아닌 사용자에게는 음악을 보낼 수 없습니다.");
                        out.println("[END]");
                        return;
                    }

                    // 1. 상대방에게 알림 메시지 전송
                    ClientHandler receiver = clients.get(target);
                    receiver.sendMessage("📨 [알림] " + nickname + "님이 음악과 메시지를 보냈습니다! (클릭하면 채팅방으로)");
                    receiver.sendMessage("[NEW_CHAT] from " + nickname); // 특별한 키워드로 알림 발생
                    // → 클라이언트는 [NEW_CHAT] 메시지 감지 시 알림 UI 띄움

                    // 2. 전송자 본인에게도 채팅방 열도록 응답
                    out.println("📤 " + target + "님에게 보낸 곡: " + songTitle);
                    out.println("📨 메시지: " + userMessage);
                    out.println("[OPEN_CHAT]");
                    out.println("[END]");
                }

                else if (message.startsWith("/chat ")) { // 상대방에게 문자 전송
                    String[] parts = message.split(" ", 3); // /chat 대상닉네임 메시지
                    if (parts.length < 3) {
                        out.println("사용법: /chat [닉네임] [메시지]");
                        continue;
                    }

                    String target = parts[1];
                    String chatMessage = parts[2];

                    if (!clients.containsKey(target)) {
                        out.println("해당 사용자가 접속해 있지 않습니다.");
                        continue;
                    }

                    if (!friends.contains(target)) {
                        out.println("친구가 아닌 사용자와는 채팅할 수 없습니다.");
                        continue;
                    }

                    ClientHandler receiver = clients.get(target);
                    receiver.sendMessage("[채팅] " + nickname + ": " + chatMessage); // 상대방에게 채팅 전달
                    out.println(target + "님에게 메시지를 보냈습니다.");
                }

                else if (message.startsWith("/like ")) { // 좋아요
                    String song = message.substring(6).trim(); // 곡 제목 파싱
                    if (song.isEmpty()) {
                        out.println("사용법: /like [곡제목]");
                        continue;
                    }

                    // TODO: 서버 내부에서 장르별 랭킹 시스템 반영 (현재는 출력만)
                    out.println(song + "' 곡에 좋아요를 눌렀습니다.");
                    System.out.println(nickname + " → 좋아요: " + song);
                }

                else if (message.startsWith("/dislike ")) { // 싫어요
                    String song = message.substring(9).trim(); // 곡 제목 파싱
                    if (song.isEmpty()) {
                        out.println("사용법: /dislike [곡제목]");
                        continue;
                    }

                    // TODO: 서버 내부에서 장르별 랭킹 시스템 반영 (현재는 출력만)
                    out.println(song + "' 곡에 싫어요를 눌렀습니다.");
                    System.out.println(nickname + " → 싫어요: " + song);
                }

                else if (message.equals("/disconnect")) {
                    out.println("연결을 종료합니다.");
                    out.println("[END]");
                    break; // finally 블록으로 가서 socket 정리
                }
            }

        } catch (IOException e) {
            System.out.println("연결 오류: " + e.getMessage());
        } finally {
            if (nickname != null) {
                clients.remove(nickname); // 클라이언트가 연결을 종료하면 클라이언트를 맵에서 삭제!
                System.out.println("[" + nickname + "] 연결 종료");
            }
            try {
                socket.close(); // 소켓 자원 해제
            } catch (IOException e) {
                System.out.println("소켓 종료 오류: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String msg) { // 친구에게 메시지 전송
        out.println(msg);
    }
}

