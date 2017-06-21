package org.ednull.hits.server;

import org.ednull.hits.data.DataStore;
import org.ednull.hits.data.NameNotFoundError;
import org.ednull.hits.data.SystemReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Control serving data for web apps
 */
@Controller
public class WebDataController {

    DataStore dataStore;

    public WebDataController(@Autowired DataStore dataStore) {
        this.dataStore = dataStore;
    }

    long getReportItem(SystemReport report, String item) throws NameNotFoundError {
        if (item.equals("arrived")) {
            return report.arrived;
        }
        if (item.equals("destroyed")) {
            return report.destroyed;
        }
        if (item.equals("interdicted")) {
            return report.interdicted;
        }
        throw new NameNotFoundError(item);
    }

    @RequestMapping(value = "/webapi/timeline/{starSystem}/{eventType}")
    public ResponseEntity<WebDataSet> getDataSet(
            @PathVariable(name = "starSystem", required = true) String starSystem,
            @PathVariable(name = "eventType", required = true) String eventType,
            @RequestParam(name = "hours", defaultValue = "24") int hours)
    {
        WebDataSet data = new WebDataSet();
        if (eventType == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            long systemId = dataStore.lookupSystem(starSystem);
            data.values = new long[hours];
            long last = getReportItem(dataStore.getReport(systemId,+ 1), eventType);

            for (int h = 1; h < hours; h++){
                SystemReport report = dataStore.getReport(systemId, h);
                long reportValue = getReportItem(report, eventType);
                data.values[h] = reportValue - last;
                last = reportValue;
            }

        } catch (NameNotFoundError error) {
            throw new NoSuchLocationError();
        }

        return ResponseEntity.ok(data);
    }
}
