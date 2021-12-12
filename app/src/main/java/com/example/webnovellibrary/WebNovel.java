package com.example.webnovellibrary;

public class WebNovel {
    String title;
    String url;

    //TODO dates for sorting
//    String dateLastOpened;
//    String dateCreated;

    public WebNovel(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
