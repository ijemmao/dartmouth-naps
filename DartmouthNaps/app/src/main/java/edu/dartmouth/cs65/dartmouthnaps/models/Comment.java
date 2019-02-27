package edu.dartmouth.cs65.dartmouthnaps.models;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Comment {
    private String author;              // String for the author user ID
    private String body;                // String for the body of the comment
    private String location;            // String for the location ID
    private String subLocation;         // String for the secondary location descriptor (floor, etc.)
    private String timestamp;           // String for the timestamp of the comment in
                                        // "YYYY-MM-DD HH:MM:SS.SSS" form
    private Map<String, Integer> votes; // Map for dictionary of votes, where the String key is the
                                        // user ID, and the Integer value is either 1 or -1

    public Comment() {
        this("", "", "", "", "", new HashMap<String, Integer>());
    }

    public Comment(
            String author,
            String body,
            String location,
            String subLocation,
            String timestamp,
            Map<String, Integer> votes) {
        this.author = author;
        this.body = body;
        this.location = location;
        this.subLocation = subLocation;
        this.timestamp = timestamp;
        this.votes = votes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubLocation() {
        return subLocation;
    }

    public void setSubLocation(String subLocation) {
        this.subLocation = subLocation;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Integer> getVotes() {
        return votes;
    }

    public void setVotes(Map<String, Integer> votes) {
        this.votes = votes;
    }

    public static String getTimestampFromCalendar(Calendar timestampCal) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d:%02d.%03d",
                timestampCal.get(Calendar.YEAR),
                timestampCal.get(Calendar.MONTH) + 1,
                timestampCal.get(Calendar.DAY_OF_MONTH),
                timestampCal.get(Calendar.HOUR_OF_DAY),
                timestampCal.get(Calendar.MINUTE),
                timestampCal.get(Calendar.SECOND),
                timestampCal.get(Calendar.MILLISECOND));
    }
}