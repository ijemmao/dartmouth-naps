package edu.dartmouth.cs65.dartmouthnaps.util;

public abstract class Globals {
    public static final String TAG_GLOBAL = "DartmouthNaps";
    public static final boolean DEBUG_GLOBAL = true;

    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

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
