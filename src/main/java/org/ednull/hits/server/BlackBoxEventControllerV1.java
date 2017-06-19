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

    static final String CLIENT_LATEST_RELEASE = "0.2.5";
    static final String BLACK_BOX_PATH_V1 = "/hits/v1";

    @RequestMapping(value = "/crime", method = RequestMethod.POST)
    public void submitEvent(
            @RequestBody BBCrime crime) {

        dataStore.addCrime(crime);
    }

    @RequestMapping(value = "/hot", method = RequestMethod.GET)
    public ResponseEntity<List<LocationReport>> getHotspots(
            @RequestParam int hours
    ) {
        List<SystemReport> hotsystems = dataStore.dangerSystems(5, hours);
        List<LocationReport> reports = new ArrayList<>();

        for (SystemReport systemReport : hotsystems) {
            LocationReport report = LocationReport.FromSystemReport(systemReport, hours);
            reports.add(report);
        }

        return ResponseEntity.ok(reports);
    }

    @RequestMapping(value = "/latest", method = RequestMethod.GET)
    public ResponseEntity<String> checkClientUpdate(
            @RequestHeader(value = "User-Agent") String userAgent) {
        return ResponseEntity.ok(CLIENT_LATEST_RELEASE);
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

            LocationReport report = LocationReport.FromSystemReport(systemReport, hours);
            return ResponseEntity.ok(report);
        } catch (NameNotFoundError err) {
            return new ResponseEntity<LocationReport>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * check the crime status of a pilot.
     * @param pilot
     * @return
     */
    @RequestMapping(value = "/checkPilot/{pilot}", method = RequestMethod.GET)
    public ResponseEntity<CriminalRecord> checkPilot(
            @PathVariable String pilot,
            @RequestParam(required = false, defaultValue = "") String currentSystem) {
        // If this pilot has recently committed a crime, return their offences.
        // If they have committed any, record their current location (if given)
        long pilotId = dataStore.lookupPilot(pilot);
        CriminalRecord record = dataStore.lookupCrimes(pilotId, 24 * 14);

        return ResponseEntity.ok(record);
    }

    /**
     * Upload a crime report happening right now
     * @param crime
     * @return
     */
    @RequestMapping(value = "/reportCrime", method = RequestMethod.POST)
    public ResponseEntity<String> reportCrime(
            @RequestBody Crime crime){
        BBCrime datacrime = new BBCrime();
        datacrime.pilot = dataStore.lookupPilot(crime.criminal);
        datacrime.offence = crime.offence;
        datacrime.timestamp = crime.timestamp;
        String result;

        dataStore.addCrime(datacrime);
        try {
            datacrime.starSystem = dataStore.lookupSystem(crime.starSystem);
            dataStore.addSighting(datacrime.pilot, datacrime.starSystem, datacrime.timestamp);
            result = "accepted";

        } catch (NameNotFoundError nameNotFoundError) {
            // it's not an "error" if the system doesn't exist yet.
            result = "unknown system";
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/reportSighting/{starSystem}/{commander}", method = RequestMethod.POST)
    public ResponseEntity<String> reportSighting(
            @PathVariable String commander,
            @PathVariable String starSystem
    )
    {
        try {
            dataStore.addSighting(dataStore.lookupPilot(commander), dataStore.lookupSystem(starSystem),
                    new Timestamp(System.currentTimeMillis()));
        } catch (NameNotFoundError error) {
            return ResponseEntity.ok("unknown pilot/system");
        }
        return ResponseEntity.ok("accepted");
    }

    @Autowired
    public BlackBoxEventControllerV1(EddnPump dataPump, IncidentScanner incidentScanner, DataStore dataStore) {
        this.dataStore = dataStore;
        this.dataPump = dataPump;
        this.incidentScanner = incidentScanner;
        this.dataPump.start();
    }
}
