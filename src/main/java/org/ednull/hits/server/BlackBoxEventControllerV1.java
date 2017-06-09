package org.ednull.hits.server;

import org.ednull.hits.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON service for accepting black box events
 */

@RestController
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
@RequestMapping(value = BlackBoxEventControllerV1.BLACK_BOX_PATH_V1)
public class BlackBoxEventControllerV1 {

    private final DataStore dataStore;
    private final EddnPump dataPump;
    private final IncidentScanner incidentScanner;

    static final String BLACK_BOX_PATH_V1 = "/hits/v1";

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public void submitEvent(
            @RequestBody BoxEvent inputEvent) throws NameNotFoundError {

        BBEvent evt = new BBEvent();
        evt.setApp(dataStore.lookupApp(inputEvent.app));
        evt.setStarSystem(dataStore.lookupSystem(inputEvent.starSystem));
        evt.setTimestamp(Timestamp.from(Instant.ofEpochSecond(inputEvent.timestamp)));
        evt.setSubmitter(inputEvent.submitter);

        if (inputEvent.eventName == BBEvent.ARRIVED || inputEvent.eventName == BBEvent.DESTORYED) {
            evt.setEventName(inputEvent.eventName);

            dataStore.addEvent(evt);
        } else {
            throw new BBEventNotFound("invalid event name given");
        }
    }

    @RequestMapping(value = "/hot", method = RequestMethod.GET)
    public ResponseEntity<List<LocationReport>> getHotspots(
            @RequestParam int hours
    ) {
        List<SystemReport> hotsystems = dataStore.dangerSystems(10, hours);
        List<LocationReport> reports = new ArrayList<>();

        for (SystemReport systemReport : hotsystems) {
            LocationReport report = new LocationReport();
            report.systemName = systemReport.systemName;
            report.totalVisits = systemReport.totalVisits;
            report.periodHours = hours;
            report.destroyed = systemReport.destroyed;
            report.interdicted = systemReport.interdicted;
            report.arrived = systemReport.arrived;
            report.advice = RiskAdviser.getAdvice(systemReport);

            reports.add(report);
        }

        return ResponseEntity.ok(reports);
    }

    /**
     * Check a location's safety stats
     * @param hours
     * @return
     */
    @RequestMapping(value = "/location/{systemName}/**", method = RequestMethod.GET)
    public ResponseEntity<LocationReport> checkLocation(
            @PathVariable String systemName,
            @RequestParam int hours){
        try {
            long sysId = dataStore.lookupSystem(systemName);
            SystemReport systemReport = dataStore.getReport(sysId, hours);
            LocationReport report = new LocationReport();
            report.systemName = systemReport.systemName;
            report.totalVisits = systemReport.totalVisits;
            report.periodHours = hours;
            report.destroyed = systemReport.destroyed;
            report.interdicted = systemReport.interdicted;
            report.arrived = systemReport.arrived;
            report.advice = RiskAdviser.getAdvice(systemReport);

            return ResponseEntity.ok(report);
        } catch (NameNotFoundError err) {
            return new ResponseEntity<LocationReport>(HttpStatus.NOT_FOUND);
        }
    }

    @Autowired
    public BlackBoxEventControllerV1(EddnPump dataPump, IncidentScanner incidentScanner, DataStore dataStore) {
        this.dataStore = dataStore;
        this.dataPump = dataPump;
        this.incidentScanner = incidentScanner;

        this.dataPump.start();
    }
}
