package edu.dartmouth.cs65.dartmouthnaps.util;

public abstract class Globals {
    public static final String TAG_GLOBAL = "DartmouthNaps";
    public static final boolean DEBUG_GLOBAL = true;

    public static final int NOTIFICATION_ID_LOCATION_MONITOR = 1;
    public static final int NOTIFICATION_ID_REVIEW_PROMPT = 2;
    public static final int NOTIFICATION_ID_STARRED_REVIEW = 3;
    public static final String[] NOTIFICATION_CHANNEL_IDS = new String[]{
            "Location Monitor",
            "Review Prompt",
            "Starred Review"};
    public static final String[] NOTIFICATION_CHANNEL_NAMES = new String[]{
            TAG_GLOBAL + ": " + NOTIFICATION_CHANNEL_IDS[NOTIFICATION_ID_LOCATION_MONITOR - 1],
            TAG_GLOBAL + ": " + NOTIFICATION_CHANNEL_IDS[NOTIFICATION_ID_REVIEW_PROMPT - 1],
            TAG_GLOBAL + ": " + NOTIFICATION_CHANNEL_IDS[NOTIFICATION_ID_STARRED_REVIEW - 1]};
    public static final String[] NOTIFICATION_TITLES = new String[]{
            "Just so you know, we're monitoring your location", // Location Monitor
            "Hey, sleepy head! We noticed you've been at  for a couple hours.", // Review Prompt
            "Someone just napped in !"}; // Starred Review
    public static final String[] NOTIFICATION_TEXTS = new String[]{
            "Tap here to return to the app.", // Location Monitor
            "Tap here to review your nap.", // Review Prompt
            "Tap here to see their review."}; // Starred Review
    public static final String CHANNEL_ID = "DartmouthNaps Notifications";

    public static final String KEY_REVIEW_PROMPT = "review prompt";
    public static final String KEY_STARRED_REVIEW = "starred review";
    public static final String KEY_REVIEW_KEY = "review key";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    // Constants for messages to LSHandler
    private static final int OFFSET_LS_HANDLER = 0x10;
    public static final int MSG_WHAT_SEND_MESSENGER = 1 | OFFSET_LS_HANDLER;

    // Constants for messages to CMFHandler
    private static final int OFFSET_CMF_HANDLER = 0x20;
    public static final int MSG_WHAT_SEND_LOCATION  = 1 | OFFSET_CMF_HANDLER;

    public static final String CAMPUS_MAP_STYLE_JSON =
            "   [" +
            "       {" +
            "           \"elementType\": \"geometry\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#1d2c4d\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"elementType\": \"labels.text.fill\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#8ec3b9\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"elementType\": \"labels.text.stroke\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#1a3646\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"administrative\"," +
            "           \"elementType\": \"geometry\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"visibility\": \"off\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"administrative.country\"," +
            "           \"elementType\": \"geometry.stroke\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#4b6878\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"administrative.land_parcel\"," +
            "           \"elementType\": \"labels.text.fill\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#64779e\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"administrative.neighborhood\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"visibility\": \"off\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"administrative.province\"," +
            "           \"elementType\": \"geometry.stroke\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#4b6878\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"landscape.man_made\"," +
            "           \"elementType\": \"geometry.stroke\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#334e87\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"landscape.man_made\"," +
            "           \"elementType\": \"labels\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"visibility\": \"off\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"landscape.natural\"," +
            "           \"elementType\": \"geometry\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#023e58\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"poi\"," +
            "           \"elementType\": \"geometry\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#283d6a\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"poi\"," +
            "           \"elementType\": \"labels\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"visibility\": \"off\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"road\"," +
            "           \"elementType\": \"geometry\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#304a7d\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"road\"," +
            "           \"elementType\": \"labels.icon\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"visibility\": \"off\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"road\"," +
            "           \"elementType\": \"labels.text.fill\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#98a5be\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"road\"," +
            "           \"elementType\": \"labels.text.stroke\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#1d2c4d\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"road.highway\"," +
            "           \"elementType\": \"geometry\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#2c6675\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"road.highway\"," +
            "           \"elementType\": \"geometry.stroke\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#255763\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"road.highway\"," +
            "           \"elementType\": \"labels.text.fill\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#b0d5ce\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"road.highway\"," +
            "           \"elementType\": \"labels.text.stroke\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#023e58\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"transit\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"visibility\": \"off\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"water\"," +
            "           \"elementType\": \"geometry\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#0e1626\"" +
            "               }" +
            "           ]" +
            "       }," +
            "       {" +
            "           \"featureType\": \"water\"," +
            "           \"elementType\": \"labels.text.fill\"," +
            "           \"stylers\": [" +
            "               {" +
            "                   \"color\": \"#4e6d70\"" +
            "               }" +
            "           ]" +
            "       }" +
            "   ]";
}
