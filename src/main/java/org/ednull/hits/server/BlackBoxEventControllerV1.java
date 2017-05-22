package org.ednull.hits.server;

import org.ednull.hits.data.BBEvent;
import org.ednull.hits.data.DataStore;
import org.ednull.hits.data.IncidentScanner;
import org.ednull.hits.data.NameNotFoundError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;

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

    static final String BLACK_BOX_PATH_V1 = "/blackbox/v1";

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public void submitEvent(
            @RequestBody BoxEvent inputEvent) throws NameNotFoundError {

        BBEvent evt = new BBEvent();
        evt.setApp(dataStore.lookupApp(inputEvent.app));
        evt.setStarSystem(dataStore.lookupSystem(inputEvent.starSystem));
        evt.setTimestamp(Timestamp.from(Instant.ofEpochSecond(inputEvent.timestamp)));
        evt.setSubmitter(inputEvent.submitter);
        evt.setEventName(inputEvent.eventName);

        dataStore.addEvent(evt);
    }

    @Autowired
    public BlackBoxEventControllerV1(EddnPump dataPump, IncidentScanner incidentScanner, DataStore dataStore) {
        this.dataStore = dataStore;
        this.dataPump = dataPump;
        this.incidentScanner = incidentScanner;

        this.dataPump.start();
    }
}
