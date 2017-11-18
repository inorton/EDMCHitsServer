package org.ednull.hits.server;

import org.ednull.hits.data.RiskAdviser;
import org.ednull.hits.data.SystemReport;

/**
 * Safety report on a location
 */
public class LocationReport {
    public String advice;
    public String systemName;
    public int periodHours;
    public long totalVisits;
    public long arrived;
    public long destroyed;
    public long interdicted;
    public String systemNameEscaped;

    public static LocationReport FromSystemReport(SystemReport systemReport, int periodHours) {
        LocationReport report = new LocationReport();
        report.advice = RiskAdviser.getAdvice(systemReport);
        report.totalVisits = systemReport.totalVisits;
        report.arrived = systemReport.docked;
        report.destroyed = systemReport.destroyed;
        report.systemName = systemReport.systemName;
        report.periodHours = periodHours;
        report.systemNameEscaped = systemReport.systemName.replace(" ", "__");
        return report;
    }
}
