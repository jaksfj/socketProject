package edu.network.musicweb.webserver.repository;

import edu.network.musicweb.webserver.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    // 모든 장르를 중복없이 조회
    @Query("SELECT DISTINCT song.genre from Song song")
    List<String> findDistinctGenres();

    // 특정 장르에 속한 가수들만 종복없이 조회
    @Query("SELECT DISTINCT song.artist from Song song WHERE song.genre = :genre")
    List<String> findDistinctArtistsByGenre(String genre);

    // 특정 장르 + 가수에 해당하는 곡 전체 조회
    List<Song> findByGenreAndArtist(String genre, String artist);
}

