package edu.dartmouth.cs65.dartmouthnaps.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.dartmouth.cs65.dartmouthnaps.util.PlaceUtil.*;

public class User {
    private Map<String, String> reviews;
    private List<Boolean> starred;

    public User() {
        reviews = new HashMap<>();
        starred = new ArrayList<>();
    }

    public List<Boolean> getStarred() {
        return starred;
    }

    public void setStarred(List<Boolean> starred) {
        this.starred = starred;
    }

    public Map<String, String> getReviews() {
        return reviews;
    }

    public void setReviews(Map<String, String> reviews) {
        this.reviews = reviews;
    }

    public boolean[] toBooleanArr() {
        boolean[] starredArr = new boolean[PLACE_COUNT];

        for (int i = 0; i < PLACE_COUNT; i++) {
            starredArr[i] = starred.get(i);
        }

        return starredArr;
    }
}
