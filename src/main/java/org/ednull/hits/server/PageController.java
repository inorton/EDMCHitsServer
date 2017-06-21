package org.ednull.hits.server;

import org.ednull.hits.data.DataStore;
import org.ednull.hits.data.NameNotFoundError;
import org.ednull.hits.data.SystemReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    void setTitle(Model model, String title) {
        model.addAttribute(title);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hotspot(Model model,
                          @RequestParam(value = "hours", defaultValue = "36") int hours) {
        setTitle(model, "Hotspots");

        List<LocationReport> systemReports = new ArrayList<>();
        for(SystemReport systemReport : dataStore.dangerSystems(8, hours)) {
            systemReports.add(LocationReport.FromSystemReport(systemReport, hours));
        }

        model.addAttribute("reports", systemReports);
        return "hotspots";
    }

    @RequestMapping(value = "/systems", method = RequestMethod.GET)
    public String systems(Model model,
                          @RequestParam(value = "hours", defaultValue = "24") int hours)
    {
        setTitle(model, "Active Systems");

        List<LocationReport> systemReports = new ArrayList<>();
        for(SystemReport systemReport : dataStore.busySystems(16, hours)) {
            systemReports.add(LocationReport.FromSystemReport(systemReport, hours));
        }

        model.addAttribute("reports", systemReports);
        return "systems";
    }

    @RequestMapping(value = "/system/{starSystem}", method = RequestMethod.GET)
    public String systemInfo(Model model,
                             @PathVariable(value = "starSystem", required = true) String name) {
        setTitle(model, String.format("The {} system", name));

        try {
            dataStore.lookupSystem(name);
        } catch (NameNotFoundError error) {
            throw new NoSuchLocationError();
        }

        model.addAttribute("starSystem", name);
        return "system";
    }
}
