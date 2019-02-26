package edu.dartmouth.cs65.dartmouthnaps.models;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.dartmouth.cs65.dartmouthnaps.util.Globals.*;

public class Place {
    private String name;
    private Map<String, Comment> comments;
    private Map<String, Integer> ratings;

    public Place() {
        this("", new HashMap<String, Comment>(), new HashMap<String, Integer>());
    }

    public Place(String name, Map<String, Comment> comments, Map<String, Integer> ratings) {
        this.name = name;
        this.comments = comments;
        this.ratings = ratings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Comment> getComments() {
        return comments;
    }

    public void setComments(Map<String, Comment> comments) {
        this.comments = comments;
    }

    public Map<String, Integer> getRatings() {
        return ratings;
    }

    public void setRatings(Map<String, Integer> ratings) {
        this.ratings = ratings;
    }

    public double avgRating() {
        double avg = 0;
        int ratingsSize = ratings.size();

        if (ratingsSize == 0) return 0;

        for (Map.Entry<String, Integer> rating : ratings.entrySet()) {
            avg += rating.getValue();
        }

        return avg / ratingsSize;
    }

    public int ratingColor() {
        long color;
        List<Double> redComps = new ArrayList<>();
        List<Double> greenComps = new ArrayList<>();
        List<Double> blueComps = new ArrayList<>();
        int numColors = RATING_COLORS.length;
        double avgRating = avgRating() * (numColors - 1) / 5;
        int ratingIdx = (int)avgRating;
        double ratingDiff = avgRating % 1;

        if (avgRating == numColors - 1) return Color.parseColor(RATING_COLORS[numColors - 1]);

        for (String colorString : RATING_COLORS) {
            color = Color.pack(Color.parseColor(colorString));
            redComps.add((double)Color.red(color));
            greenComps.add((double)Color.green(color));
            blueComps.add((double)Color.blue(color));
        }

        return Color.rgb(
                (float)(redComps.get(ratingIdx) + ratingDiff * (redComps.get(ratingIdx + 1) - redComps.get(ratingIdx))),
                (float)(greenComps.get(ratingIdx) + ratingDiff * (greenComps.get(ratingIdx + 1) - greenComps.get(ratingIdx))),
                (float)(blueComps.get(ratingIdx) + ratingDiff * (blueComps.get(ratingIdx + 1) - blueComps.get(ratingIdx))));
    }
}
