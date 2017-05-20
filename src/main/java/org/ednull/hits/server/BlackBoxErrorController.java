package org.ednull.hits.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping(BlackBoxErrorController.ERROR_PATH)
public class BlackBoxErrorController implements ErrorController {

    static final String ERROR_PATH = "/error";

    private final ErrorAttributes errors;

    @Autowired
    public BlackBoxErrorController(ErrorAttributes errors) {
        this.errors = errors;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest aRequest, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(aRequest);
        return errors.getErrorAttributes(requestAttributes, includeStackTrace);
    }

    @RequestMapping
    public Map<String, Object> error(HttpServletRequest aRequest){
        Map<String, Object> body = getErrorAttributes(aRequest, true);
        String trace = (String) body.get("trace");
        if(trace != null){
            String[] lines = trace.split("\n\t");
            body.put("trace", lines);
        }
        return body;
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
