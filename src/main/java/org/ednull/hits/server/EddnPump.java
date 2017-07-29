package org.ednull.hits.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ednull.hits.data.IncidentScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


/**
 * Subscribe to zmq relay from EDDN
 */
public class EddnPump extends Thread {

    Logger logger = LoggerFactory.getLogger(EddnPump.class);

    Thread pumpThread;
    private boolean started = false;

    public static final String RELAY = "tcp://eddn.edcd.io:9500";
    private final IncidentScanner scanner;

    @Autowired
    public EddnPump(IncidentScanner scanner) {
        this.scanner = scanner;
    }

    private long eventCount = 0;

    @Override
    public void run() {
        pump();
    }

    private void summary() {
        if (eventCount % 200 == 0) {
            logger.info(String.format("pumped %d events", eventCount));
            scanner.clean();
        }
    }

    public synchronized void pump() {
        started = true;
        ZContext ctx = new ZContext();
        ZMQ.Socket client = ctx.createSocket(ZMQ.SUB);
        client.subscribe("".getBytes());
        client.setReceiveTimeOut(30000);

        client.connect(RELAY);
        logger.info("EDDN Relay connected");
        ZMQ.Poller poller = ctx.createPoller(2);
        poller.register(client, ZMQ.Poller.POLLIN);
        byte[] output = new byte[256 * 1024];
        while (true) {
            int poll = poller.poll(10);
            if (poll == ZMQ.Poller.POLLIN) {
                ZMQ.PollItem item = poller.getItem(poll);

                if (poller.pollin(0)) {
                    byte[] recv = client.recv(ZMQ.NOBLOCK);
                    if (recv.length > 0) {
                        // decompress
                        Inflater inflater = new Inflater();
                        inflater.setInput(recv);
                        try {
                            int outlen = inflater.inflate(output);
                            String outputString = new String(output, 0, outlen, "UTF-8");
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Object> map = mapper.readValue(
                                    outputString,
                                    new TypeReference<Map<String, Object>>() {
                                    });

                            if (!map.isEmpty()) {
                                if (map.containsKey(IncidentScanner.SCHEMA_KEY)){
                                    eventCount++;
                                    summary();
                                    if (scanner != null) {
                                        scanner.input((LinkedHashMap) map);
                                    }
                                }
                            }

                        } catch (DataFormatException | IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    public static void main(String[] args){
        new EddnPump(null).pump();
    }
}
