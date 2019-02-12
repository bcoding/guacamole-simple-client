package de.bcoding.guacamole;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuacamoleConnectionFactory {
    static final List<ParameterConfiguration> PARAMETERS = new ArrayList<>();

    static final String PARAM_TARGET_PROTOCOL = "targetProtocol";

    static final String PARAM_TARGET_HOST = "targetHost";

    static final String PARAM_TARGET_PORT = "targetPort";

    static final String PARAM_TARGET_PASSWORD = "targetPassword";

    static final String PARAM_GUACD_HOST = "guacdHost";

    static final String PARAM_GUACD_PORT = "guacdPort";

    static Map<String, ParameterConfiguration> PARAMETER_MAP;

    static {
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_TARGET_PROTOCOL", PARAM_TARGET_PROTOCOL, "vnc"));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_TARGET_HOST", PARAM_TARGET_HOST));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_TARGET_PORT", PARAM_TARGET_PORT, "5900"));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_TARGET_PASSWORD", PARAM_TARGET_PASSWORD, null));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_GUACD_HOST", PARAM_GUACD_HOST));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_GUACD_PORT", PARAM_GUACD_PORT, "4822"));
        PARAMETER_MAP = PARAMETERS.stream().collect(Collectors.toMap(ParameterConfiguration::getName, o -> o));
    }

    private final Map<String, String[]> parameterMap;

    GuacamoleConnectionFactory(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public GuacamoleTunnel create() {
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol(getParameter(PARAM_TARGET_PROTOCOL, parameterMap));
        config.setParameter("hostname", getParameter(PARAM_TARGET_HOST, parameterMap));
        config.setParameter("port", getParameter(PARAM_TARGET_PORT, parameterMap));
        if (getParameter(PARAM_TARGET_PASSWORD, parameterMap) != null) {
            config.setParameter("password", getParameter(PARAM_TARGET_PASSWORD, parameterMap));
        }

        // Connect to guacd, proxying a connection to the VNC server above
        try {
            GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                    new InetGuacamoleSocket(getParameter(PARAM_GUACD_HOST, parameterMap),
                            Integer.parseInt(getParameter(PARAM_GUACD_PORT, parameterMap))),
                    config
            );
            // Create tunnel from now-configured socket
            return new SimpleGuacamoleTunnel(socket);
        } catch (GuacamoleException ex) {
            return null;
        }
    }

    static class ParameterConfiguration {

        private final String defaultValue;

        private final boolean optional;

        private final String name;

        private final String environmentVariable;

        ParameterConfiguration(String environmentVariable, String name, boolean optional, String defaultValue) {
            this.environmentVariable = environmentVariable;
            this.defaultValue = defaultValue;
            this.optional = optional;
            this.name = name;
        }

        ParameterConfiguration(String environmentVariable, String name, String defaultValue) {
            this(environmentVariable, name, true, defaultValue);
        }

        ParameterConfiguration(String environmentVariable, String name) {
            this(environmentVariable, name, false, null);
        }

        boolean check(Map<String, String[]> parameters) {
            final String[] values = parameters.get(name);
            return optional || (values != null && values.length != 0) || System.getenv(environmentVariable) != null;
        }

        String getValue(Map<String, String[]> parameters) {
            String[] parameterValue = parameters.get(name);
            if(parameterValue == null) {
                parameterValue = new String[]{
                        Optional.ofNullable(System.getenv(environmentVariable))
                                .orElse(defaultValue)
                };
            }
            return parameterValue[0];
        }

        String getName() {
            return name;
        }
    }

    private String getParameter(String name, Map<String, String[]> parameterMap) {
        return PARAMETER_MAP.get(name).getValue(parameterMap);
    }
}
