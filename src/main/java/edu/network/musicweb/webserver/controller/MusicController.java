package edu.network.musicweb.webserver.controller;

import edu.network.musicweb.webserver.entity.Song;
import edu.network.musicweb.webserver.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MusicController {

    private final SongRepository songRepository;

    private final Map<String, List<String>> playlistMap = new ConcurrentHashMap<>(); // 사용자의 플레이리스트

    @GetMapping("/genres")
    public List<String> getGenres() { // 장르들 가져오기
        return songRepository.findDistinctGenres();
    }

    @GetMapping("/artists")
    public List<String> getArtistsByGenre(@RequestParam String genre) { // 선택한 장르에 맞는 가수들 가져오기
        return songRepository.findDistinctArtistsByGenre(genre);
    }

    @GetMapping("/songs")
    public List<Song> getSongsByGenreAndArtist(@RequestParam String genre, @RequestParam String artist) { // 가수들의 곡들 가져오기
        return songRepository.findByGenreAndArtist(genre, artist);
    }

    @PostMapping("/playlist/add")
    public String addToPlaylist(@RequestBody Map<String, String> payload) {
        String nickname = payload.get("nickname").trim(); // 해당 소켓의 닉네임 공백 제거
        String songTitle = payload.get("song"); // 음악 제목 가져오기

        // 해당 닉네임의 플레이리스트가 없으면 새로 생성
        playlistMap.putIfAbsent(nickname, new ArrayList<>());

        // 리스트에 곡 추가
        playlistMap.get(nickname).add(songTitle);

        return nickname + "님의 플레이리스트에 '" + songTitle + "' 저장 완료!";
    }

    // 사용자의 플레이리스트 조회
    @GetMapping("/playlist")
    public List<String> getPlaylist(@RequestParam String nickname) {
        return playlistMap.getOrDefault(nickname.trim(), Collections.emptyList());
    }
}
