package com.example.webnovellibrary;

import java.util.ArrayList;
import java.util.List;

public class Folder {
    String name;
    List<WebNovel> webNovels;

    public Folder(String folderName) {
        this.name = folderName;
        this.webNovels = new ArrayList<>();
    }
    public String getFolderName() {
        return name;
    }

    public void setFolderName(String folderName) {
        this.name = folderName;
    }

    public List<WebNovel> getWebNovels() {
        return webNovels;
    }

    public void setWebNovels(List<WebNovel> webNovels) {
        this.webNovels = webNovels;
    }

}
