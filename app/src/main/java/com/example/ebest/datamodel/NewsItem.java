package com.example.ebest.datamodel;

import androidx.annotation.NonNull;

public class NewsItem {

    private final String title;
    private final String realkey;

    public NewsItem(String title, String realkey) {
        this.title = title;
        this.realkey = realkey;
    }

    public String getTitle() {
        return title;
    }

    public String getkey() {
        return realkey;
    }

    @NonNull
    @Override
    public String toString() {
        return title;  // ListView에 표시될 텍스트
    }
}
