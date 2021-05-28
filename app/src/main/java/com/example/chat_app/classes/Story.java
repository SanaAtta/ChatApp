package com.example.chat_app.classes;

public class Story {
    private String imageurl;
    private String storyid;
    private String userid;
    private long timestart;
    private long timeend;

    public Story(String imageurl, long timestart, long timeend, String storyid, String userid ) {
        this.imageurl = imageurl;
        this.storyid = storyid;
        this.userid = userid;
        this.timestart = timestart;
        this.timeend = timeend;
    }

    public Story() {
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public long getTimestart() {
        return timestart;
    }

    public void setTimestart(long timestart) {
        this.timestart = timestart;
    }

    public long getTimeend() {
        return timeend;
    }

    public void setTimeend(long timeend) {
        this.timeend = timeend;
    }
}
