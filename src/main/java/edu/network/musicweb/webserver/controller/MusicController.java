package edu.network.musicweb.webserver.controller;

import edu.network.musicweb.webserver.entity.Song;
import edu.network.musicweb.webserver.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MusicController {

    private final SongRepository songRepository;

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
        String nickname = payload.get("nickname");
        String songTitle = payload.get("song");
        // TODO: 플레이리스트에 저장하는 로직 구현 (nickname 기준으로 사용자별 리스트 관리)
        return nickname + "님의 플레이리스트에 '" + songTitle + "' 저장 완료!";
    }
}
