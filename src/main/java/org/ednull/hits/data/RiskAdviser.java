package org.ednull.hits.data;

/**
 * Comment on locations
 */
public class RiskAdviser {

    static final double NOTICE = 0.02;
    static final double WARN = 0.05;

    public static String getAdvice(SystemReport location) {
        String advice = null;

        double deathRate = ((float)location.destroyed)/location.jumpedin;

        if (deathRate > NOTICE) {
            advice = String.format(
                    "System notice for %s: %d ships overdue",
                    location.systemName,
                    location.destroyed);
        }
        if (deathRate > WARN) {
            advice = String.format(
                    "WARNING for %s, %d ships recently destroyed!",
                    location.systemName,
                    location.destroyed);
        }

        return advice;
    }
}
