package org.edbb.server;

import org.edbb.data.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * JSON service for accepting black box events
 */

@RestController
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
@RequestMapping(value = BlackBoxEventControllerV1.BLACK_BOX_PATH_V1)
public class BlackBoxEventControllerV1 {

    private final DataStore dataStore;

    @Autowired
    public BlackBoxEventControllerV1(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    static final String BLACK_BOX_PATH_V1 = "/blackbox/v1";

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity<LocationReport> submitEvent(
            @RequestBody BoxEvent inputEvent) {
        return new ResponseEntity<>(new LocationReport(), HttpStatus.OK);
    }

    @RequestMapping(value = "/location", method = RequestMethod.GET)
    public ResponseEntity<LocationReport> location(
            @RequestParam int id) {
        return new ResponseEntity<>(new LocationReport(), HttpStatus.OK);
    }

}
