package com.andhikasrimadeva.myapp.note;

/**
 * Created by Andhika on 08/09/2017.
 */

public class Notes {

    private String title;

    public Notes(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Notes(){}

    private String content;



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


}
