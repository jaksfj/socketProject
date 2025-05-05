package edu.network.musicweb.webserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String genre;

    private String artist;

    // 생성자
    public Song(String title, String genre, String artist) {
        this.title = title;
        this.genre = genre;
        this.artist = artist;
    }

    public Song() {

    }

    // getter, setter
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getArtist() {
        return artist;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
