package to.sparks.mtgox.net;

import to.sparks.mtgox.service.MtGoxWebSocketClient;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.RawPacket;
import to.sparks.mtgox.model.Depth;
import to.sparks.mtgox.model.DynaBean;
import to.sparks.mtgox.model.OpPrivateDepth;
import to.sparks.mtgox.model.OpPrivateTicker;

/**
 *
 * @author SparksG
 */
public class MtGoxSocketListener implements WebSocketClientListener {

    private MtGoxWebSocketClient apiClient;
    private Logger logger;

    public MtGoxSocketListener(Logger logger, MtGoxWebSocketClient apiClient) {
        this.apiClient = apiClient;
        this.logger = logger;
    }

    @Override
    public void processOpened(WebSocketClientEvent aEvent) {
        // The websocket has been opened
    }

    @Override
    public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
        if (aEvent != null) {
            if (aPacket != null && aPacket.getFrameType() == RawPacket.FRAMETYPE_UTF8) {
                try {
                    // System.out.println(aPacket.getUTF8());

                    JsonFactory factory = new JsonFactory();
                    ObjectMapper mapper = new ObjectMapper();

                    JsonParser jp = factory.createJsonParser(aPacket.getUTF8());
                    DynaBean op = mapper.readValue(jp, DynaBean.class);

                    if (op.get("op") != null && op.get("op").equals("private")) {
                        String messageType = op.get("private").toString();
                        if (messageType.equalsIgnoreCase("ticker")) {
                            OpPrivateTicker opPrivateTicker = mapper.readValue(factory.createJsonParser(aPacket.getUTF8()), OpPrivateTicker.class);
                            apiClient.tickerEvent(opPrivateTicker.getTicker());
                            //  logger.log(Level.INFO, "Ticker: currency: {0}", new Object[]{ticker.getAvg().getCurrency()});
                        } else if (messageType.equalsIgnoreCase("depth")) {
                            OpPrivateDepth opPrivateDepth = mapper.readValue(factory.createJsonParser(aPacket.getUTF8()), OpPrivateDepth.class);
                            Depth depth = opPrivateDepth.getDepth();
                            apiClient.depthEvent(depth);
                            //    logger.log(Level.INFO, "Depth currency: {0}", new Object[]{depth.getCurrency()});
                        } else {
                            logger.log(Level.INFO, "Unknown private operation: {0}", new Object[]{aPacket.getUTF8()});
                        }

                        // logger.log(Level.INFO, "messageType: {0}, payload: {1}", new Object[]{messageType, dataPayload});
                    } else {
                        logger.log(Level.INFO, "Unknown operation: {0}, payload: {1}", new Object[]{op.get("op")});
                        // TODO:  Process the following types
                        // subscribe
                        // unsubscribe
                        // remark
                        // result
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } else {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void processClosed(WebSocketClientEvent aEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}