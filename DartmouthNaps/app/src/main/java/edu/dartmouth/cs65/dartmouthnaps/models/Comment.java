package edu.dartmouth.cs65.dartmouthnaps.models;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class Comment {
    private String author;              // String for the author user ID
    private String body;                // String for the body of the comment
    private String place;               // String for the place ID
    private String subPlace;            // String for the secondary place descriptor (floor, etc.)
    private String timestamp;           // String for the timestamp of the comment in
                                        // "YYYY-MM-DD HH:MM:SS.SSS" form
    private Map<String, Integer> votes; // Map for dictionary of votes, where the String key is the
                                        // user ID, and the Integer value is either 1 or -1

    public Comment() {
        this("", "", "", "", "", null);
    }

    public Comment(
            String author,
            String body,
            String place,
            String subPlace,
            String timestamp,
            Map<String, Integer> votes) {
        this.author = author;
        this.body = body;
        this.place = place;
        this.subPlace = subPlace;
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

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getSubPlace() {
        return subPlace;
    }

    public void setSubPlace(String subPlace) {
        this.subPlace = subPlace;
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
