package edu.dartmouth.cs65.dartmouthnaps.models;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Review {
    private String author;              // String for the author user ID
    private int noise;                  // Integer for noise rating
    private int comfort;                // Integer for comfort rating
    private int light;                  // Integer for light rating
    private String title;               // String for the title of the review
    private String timestamp;           // String for the timestamp of the comment in
                                        // "YYYY-MM-DD HH:MM:SS.SSS" form

    public Review(
            String author,
            int noise,
            int comfort,
            int light,
            String title,
            String timestamp) {
        this.author = author;
        this.noise = noise;
        this.comfort = comfort;
        this.light = light;
        this.title = title;
        this.timestamp = timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getNoise() {
        return noise;
    }

    public int getComfort() { return comfort; }

    public int getLight() { return light; }

    public String getTitle() { return title; }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("author", author);
        result.put("noise", noise);
        result.put("comfort", comfort);
        result.put("light", light);
        result.put("title", title);
        result.put("timestamp", timestamp);

        return result;
    }
}
