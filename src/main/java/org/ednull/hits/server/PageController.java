package org.ednull.hits.server;

import org.ednull.hits.data.DataStore;
import org.ednull.hits.data.SystemReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Control rendering of the home page
 */
@Controller
public class PageController {

    DataStore dataStore;

    public PageController(@Autowired DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hotspot(String fred, Model model) {
        List<LocationReport> locationReports36 = new ArrayList<>();
        for(SystemReport systemReport : dataStore.dangerSystems(6, 36)) {
            locationReports36.add(LocationReport.FromSystemReport(systemReport, 36));
        }

        model.addAttribute("fred", "bob");
        model.addAttribute("hot36", locationReports36);
        return "hotspots";
    }
}
