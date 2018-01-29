package io.github.hyuwah.catatanku.model;

import java.util.Date;

/**
 * Created by hyuwah on 26/01/18.
 */

public class Note {

    private String title;
    private String body;
    private long datetime;

    public Note(String title, String body, long datetime) {
        this.title = title;
        this.body = body;
        this.datetime = datetime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        String result = "Title: " + this.title +
                "\nBody: " + this.body +
                "\nDatetime: " + this.datetime;

        return result;
    }

    /**
     *     Helper Debug / Dummy
     */

    public static Note dbg_addItem(){
        int randomTitleNum = (int) Math.floor(Math.random()*100);
        int randomBodyNum = (int) Math.floor(Math.random()*1000);
        return new Note("Judul "+randomTitleNum,randomBodyNum+". Lorem ipsum dolor sit amet", new Date().getTime());
    }
}
