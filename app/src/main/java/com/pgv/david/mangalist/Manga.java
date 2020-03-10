package com.pgv.david.mangalist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Manga implements Serializable{
    private String id;
    private String title;
    private String author;
    private String cover;
    private String type;
    private ArrayList<String> genres;
    private String status;
    private String synopsis;

    public Manga() {
    }

    public Manga(String id, String author, String cover , ArrayList<String> genres, String status , String synopsis, String title, String type) {
        this.id = id;
        this.author = author;
        this.genres = genres;
        this.status = status;
        this.synopsis = synopsis;
        this.title = title;
        this.type = type;
        this.cover = cover;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }
}