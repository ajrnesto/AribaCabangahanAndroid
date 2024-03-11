package com.barangay360.Objects;

import java.util.ArrayList;

public class Announcement {
    String uid;
    String title;
    String content;
    long thumbnail;
    String author;
    ArrayList<String> arrKeywords;
    boolean isArchived;
    long timestamp;

    public Announcement() {
    }

    public Announcement(String uid, String title, String content, long thumbnail, String author, ArrayList<String> arrKeywords, boolean isArchived, long timestamp) {
        this.uid = uid;
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
        this.author = author;
        this.arrKeywords = arrKeywords;
        this.isArchived = isArchived;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(long thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ArrayList<String> getArrKeywords() {
        return arrKeywords;
    }

    public void setArrKeywords(ArrayList<String> arrKeywords) {
        this.arrKeywords = arrKeywords;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}