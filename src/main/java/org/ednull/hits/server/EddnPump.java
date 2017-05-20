package org.ednull.hits.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import jdk.nashorn.internal.parser.JSONParser;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Selector;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


/**
 * Subscribe to zmq relay from EDDN
 */
public class EddnPump {

    public static final String RELAY = "tcp://eddn-relay.elite-markets.net:9500";
    public static final String SCHEMA_KEY = "$schemaRef";
    public static final String SCHEMA_JOURNAL = "http://schemas.elite-markets.net/eddn/journal/1";

    public void pump() {

        ZContext ctx = new ZContext();
        ZMQ.Socket client = ctx.createSocket(ZMQ.SUB);
        client.subscribe("".getBytes());
        client.setReceiveTimeOut(30000);

        client.connect(RELAY);
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
                                if (map.containsKey(SCHEMA_KEY)){
                                    if (map.get(SCHEMA_KEY).equals(SCHEMA_JOURNAL)){
                                        LinkedHashMap msg = (LinkedHashMap) map.getOrDefault("message", null);
                                        if (msg != null){

                                        }
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
        new EddnPump().pump();
    }
}
