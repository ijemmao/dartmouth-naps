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
    private int convenience;            // Integer for convenience rating
    private String title;               // String for the title of the review
    private String imageName;           // String for the name of the image
    private String timestamp;           // String for the timestamp of the comment in
                                        // "YYYY-MM-DD HH:MM:SS.SSS" form
    private LatLng location;            // LatLng representing the coordinates of the review

    public Review() {
        this.location = new LatLng();
    }

    public Review(
            String author,
            int noise,
            int comfort,
            int light,
            String title,
            String imageName,
            String timestamp,
            LatLng location) {
        this.author = author;
        this.noise = noise;
        this.comfort = comfort;
        this.light = light;
        this.convenience = 0;
        this.title = title;
        this.imageName = imageName;
        this.timestamp = timestamp;
        this.location = location;
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

    public void setNoise(int noise) {
        this.noise = noise;
    }

    public int getComfort() {
        return comfort;
    }

    public void setComfort(int comfort) {
        this.comfort = comfort;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public int getConvenience() { return convenience; }

    public void setConvenience(int convenience) { this.convenience = convenience; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
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
        result.put("imageName", imageName);
        result.put("timestamp", timestamp);
        result.put("location", location);

        return result;
    }
}
