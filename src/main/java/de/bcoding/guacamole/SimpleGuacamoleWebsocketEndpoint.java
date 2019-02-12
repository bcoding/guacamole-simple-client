package de.bcoding.guacamole;

import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.websocket.GuacamoleWebSocketTunnelEndpoint;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ServerEndpoint("/ws-tunnel")
public class SimpleGuacamoleWebsocketEndpoint extends GuacamoleWebSocketTunnelEndpoint {
    @Override
    protected GuacamoleTunnel createTunnel(Session session, EndpointConfig config) {
        final Map<String, List<String>> parameterMap = session.getRequestParameterMap();
        return new GuacamoleConnectionFactory(parameterMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toArray(new String[0])))).create();
    }
}
