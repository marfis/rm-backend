package io.remedymatch.shared;

import io.remedymatch.institution.domain.InstitutionStandortEntity;
import io.remedymatch.match.domain.MatchStandortEntity;
import lombok.val;

import java.util.Map;
import java.util.WeakHashMap;

public class GeoCalc {


    private static final Map<String, Double> geoDatenMap = new WeakHashMap<>();


    public static double distanzBerechnen(double lat1, double lon1, double lat2, double lon2, DistanzTyp unit) {

        if ((lat1 == lat2) && (lon1 == lon2)) {

            return 0;
        }

        val key = calcKey(lat1, lon1, lat2, lon2, unit);
        if (geoDatenMap.containsKey(key)) {
            return geoDatenMap.get(key);
        }

        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;
        if (unit == DistanzTyp.Kilometer) {
            dist = dist * 1.609344;
        }

        geoDatenMap.put(key, dist);

        return (dist);
    }

    public static double kilometerBerechnen(double lat1, double lon1, double lat2, double lon2) {
        return distanzBerechnen(lat1, lon1, lat2, lon2, DistanzTyp.Kilometer);
    }

    public static double kilometerBerechnen(InstitutionStandortEntity von, InstitutionStandortEntity nach) {
        return kilometerBerechnen(von.getLatitude(), von.getLongitude(), nach.getLatitude(), nach.getLongitude());
    }

    public static double kilometerBerechnen(MatchStandortEntity von, MatchStandortEntity nach) {
        return kilometerBerechnen(von.getLatitude(), von.getLongitude(), nach.getLatitude(), nach.getLongitude());
    }

    private static String calcKey(double lat1, double lon1, double lat2, double lon2, DistanzTyp unit) {
        return lat1 + "/" + lon1 + "/" + lat2 + "/" + lon2 + "/" + unit.toString();
    }
}
