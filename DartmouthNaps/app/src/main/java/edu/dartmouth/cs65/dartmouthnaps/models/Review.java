package edu.dartmouth.cs65.dartmouthnaps.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private byte[] image;               // Image representation in byte array
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

    public byte[] getImage() { return image; }

    public void setImage(byte[] image) { this.image = image; }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        String formattedDate;
        String ampm = " AM";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS", Locale.US);
            Date newDate = format.parse(timestamp);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(newDate);
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) ampm = " PM";

            format = new SimpleDateFormat("h:mm", Locale.US);
            formattedDate = format.format(newDate);
        } catch (Exception e) {
            return timestamp;
        }

        return formattedDate + ampm;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public static String getTimestampFromCalendar(Calendar timestampCal) {

        System.out.println("TIMESTAMP: " + timestampCal.get(Calendar.AM_PM));
        String ampm;
        if (timestampCal.get(Calendar.AM_PM) == 0) {
            ampm = "AM";
        } else {
            ampm = "PM";
        }

        return String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d:%02d.%03d ",
                timestampCal.get(Calendar.YEAR),
                timestampCal.get(Calendar.MONTH) + 1,
                timestampCal.get(Calendar.DAY_OF_MONTH),
                timestampCal.get(Calendar.HOUR_OF_DAY),
                timestampCal.get(Calendar.MINUTE),
                timestampCal.get(Calendar.SECOND),
                timestampCal.get(Calendar.MILLISECOND)) + ampm;

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

    @Override
    public boolean equals (Object obj) {
        Review objReview;


        if (!(obj instanceof Review)) return false;

        objReview = (Review)obj;

        return (author.equals(objReview.author) &&
                noise == objReview.noise &&
                comfort == objReview.comfort &&
                light == objReview.light &&
                convenience == objReview.convenience &&
                title.equals(objReview.title) &&
                imageName.equals(objReview.imageName) &&
                timestamp.equals(objReview.timestamp));
    }
}
