package org.ednull.hits.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.print.DocFlavor;
import javax.xml.crypto.Data;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Accept EDDN Journal events to chart system popularity and infer ship destruction incidents
 */
public class IncidentScanner {

    Logger logger = LoggerFactory.getLogger(IncidentScanner.class);

    public static final String SCHEMA_KEY = "$schemaRef";

    public static final String EVENT_FSDJUMP = "FSDJump";
    public static final String EVENT_DOCKED = "Docked";
    public static final String EVENNT_SCAN = "Scan";

    public static final String[] WHITELIST_APPS = new String[]{
            "E:D Market Connector"
    };

    public void clean() {
        dataStore.prune(65000);
    }

    // store ships last fsd jump and last docked
    private HashMap<String, JournalEvent> records = new HashMap<>();

    // stored docked events that "might" be a resurrection
    private HashMap<String, BaseEvent> possible = new HashMap<>();

    // store the last system name from Companion api events (from edmc)
    private HashMap<String, CompanionEvent> non_journal = new HashMap<>();

    public static final Duration DOCKED_GRACE_PERIOD = Duration.ofMinutes(6);
    private final DataStore dataStore;

    @Autowired
    public IncidentScanner(DataStore dataStore) {
        logger.info("scanner created");
        this.dataStore = dataStore;
    }

    static <T> T get(LinkedHashMap map, String key) {
        Object o = map.getOrDefault(key, null);
        if (o != null) {
            return (T) o;
        }
        return null;
    }

    void updateRecord(JournalEvent update, String eventType) {
        synchronized (records) {
            if (records.containsKey(update.identity)) {
                JournalEvent existing = records.get(update.identity);
                existing.systemId = update.systemId;
                existing.time = update.time;
                existing.eventType = eventType;
            } else {
                records.put(update.identity, update);
            }
        }
    }

    private static boolean checkAppWhitelist(String appname) {
        for (String appcheck : WHITELIST_APPS) {
            if (appname.startsWith(appcheck)) return true;
        }
        return false;
    }


    public void input(LinkedHashMap eddnmessage) {
        if (eddnmessage != null) {
            /*

            Use FSDJump and Scan events to update where an individual uploader is.

            Use Docked events to check to see if they match the last FSDJump or Scan.
            If the location does not match then the individual ship was destroyed (You get Docked right after
            Died)

            If however the last FSDJump or Scan was more than DOCKED_GRACE_PERIOD then disregard
            as the individual may have just not started their companion app.

            Discard all events older than DOCKED_GRACE_PERIOD.

            When Docked, discard everything after processing.

             */

            LinkedHashMap header = get(eddnmessage, "header");
            if (header != null) {
                Instant timestamp = null;
                String uploader = (String) header.getOrDefault("uploaderID", null);
                String app = (String) header.getOrDefault("softwareName", null);
                long appId = 0;
                if (app != null)
                    appId = dataStore.lookupApp(app);

                if (!checkAppWhitelist(app))
                    return;

                JournalEvent lastevt = records.getOrDefault(uploader, null);

                LinkedHashMap message = get(eddnmessage, "message");
                if (message != null) {
                    String datestamp = (String) message.getOrDefault("timestamp", null);
                    if (datestamp != null) {
                        timestamp = Instant.parse(datestamp);
                    } else {
                        // invalid timestamp
                        return;
                    }

                    String event = get(message, "event");
                    if (event == null) {
                        // this is not a journal event, if this is EDMC we can be sure that EDMC
                        // will have only sent this if the commander was not part of a crew
                        String system = get(message, "systemName");
                        if (system != null) {
                            CompanionEvent evt = new CompanionEvent();
                            try {
                                evt.systemId = dataStore.lookupSystem(system);
                                evt.identity = uploader;
                                evt.time = timestamp;
                                synchronized (non_journal) {
                                    non_journal.put(uploader, evt);
                                }

                                if (lastevt != null) {
                                    // was last journal event in a different system?
                                    if (lastevt.systemId != evt.systemId) {
                                        possibleDestruction(app, lastevt, evt);
                                    } else {
                                        saveArrival(app, system, evt);
                                    }
                                }
                            } catch (NameNotFoundError err) {
                                // no fsdjump yet
                                return;
                            }
                        }

                        return;
                    }

                    JournalEvent evt = new JournalEvent();
                    evt.identity = uploader;
                    evt.eventType = event;
                    evt.time = timestamp;

                    String system = get(message, "StarSystem");
                    if (system != null) {
                        try {
                            evt.systemId = dataStore.lookupSystem(system);
                        } catch (NameNotFoundError nameNotFoundError) {
                            // ok, add the system later if we get an FSD jump
                        }

                        ArrayList<Double> starpos = (ArrayList<Double>) message.getOrDefault("StarPos", null);
                        if (starpos != null) {
                            evt.systemId = dataStore.addSystem(system, starpos.get(0), starpos.get(1), starpos.get(2));
                        }

                        if (evt.systemId > 0){
                            // jump
                            if (event.equals(EVENT_FSDJUMP)) {
                                logger.info("jumped {} {}", system, uploader);
                                recordSimpleEvent(appId, evt, BBEvent.JUMPEDIN);
                            }

                            // Scan
                            if (event.equals(EVENNT_SCAN)) {

                            }

                            // docked
                            if (event.equals(EVENT_DOCKED)) {
                                logger.info("docked {} {}", system, uploader);
                                recordSimpleEvent(appId, evt, BBEvent.DOCKED);
                            }

                            // memoize scan/location/fsdjump for the system id information
                            updateRecord(evt, event);
                        }
                    }
                }
            }
        }
    }

    private void recordSimpleEvent(long appId, JournalEvent evt, String docked) {
        BBEvent bbe = new BBEvent();
        bbe.setEventName(docked);
        bbe.setSubmitter(evt.identity);
        bbe.setTimestamp(new Timestamp(evt.time.toEpochMilli()));
        bbe.setApp(appId);
        bbe.setStarSystem(evt.systemId);
        dataStore.addEvent(bbe);
    }

    private void saveArrival(String app, String system, CompanionEvent evt) {
        // safe arrival since last jump
        logger.info(String.format("%s arrived safely at %s", evt.identity, system));
        clearIdentityRecord(evt);

        BBEvent arrived = new BBEvent();
        arrived.setEventName(BBEvent.ARRIVED);
        arrived.setSubmitter(evt.identity);
        arrived.setTimestamp(new Timestamp(evt.time.toEpochMilli()));
        arrived.setApp(dataStore.lookupApp(app));
        arrived.setStarSystem(evt.systemId);

        dataStore.addEvent(arrived);
    }

    private void possibleDestruction(String app, JournalEvent lastevt, CompanionEvent evt) throws NameNotFoundError {
        Duration since = Duration.between(lastevt.time, evt.time);
        if (since.toMinutes() < DOCKED_GRACE_PERIOD.toMinutes()) {
            // not quite enough time to restart the game and forget to launch edmc

            BBEvent destroyed = new BBEvent();
            destroyed.setEventName(BBEvent.DESTORYED);
            destroyed.setSubmitter(evt.identity);
            destroyed.setTimestamp(new Timestamp(evt.time.toEpochMilli()));
            destroyed.setApp(dataStore.lookupApp(app));
            destroyed.setStarSystem(evt.systemId);

            dataStore.addEvent(destroyed);
            logger.info(
                    String.format(
                            "%s possibly destroyed at %s",
                            evt.identity, dataStore.lookupSystem(lastevt.systemId)));

            clearIdentityRecord(evt);
        }
    }

    private void clearIdentityRecord(CompanionEvent evt) {
        synchronized (records) {
            // delete the earlier data for this uploader
            records.remove(evt.identity);
        }
    }
}
