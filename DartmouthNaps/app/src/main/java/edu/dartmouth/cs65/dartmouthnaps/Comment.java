package edu.dartmouth.cs65.dartmouthnaps;

import java.util.Date;

public class Comment {
    private String author;
    private Date dateTime;
    private String location;
    private String body;

    public Comment() {};

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAuthor() {
        return this.author;
    }

    public Date getDateTime() {
        return this.dateTime;
    }

    public String getLocation() {
        return this.location;
    }

    public String getBody() {
        return this.body;
    }
}
