/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.net.example;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simple tunnel example with hard-coded configuration parameters.
 */
public class SimpleGuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {

    public final static long serialVersionUID = 1;

    private static final String SERVER_PARAMETER_ERROR = "Please specify server parameter in form HOSTNAME:PORT";

    private static final List<ParameterConfiguration> PARAMETERS = new ArrayList<>();

    private static final String PARAM_TARGET_PROTOCOL = "targetProtocol";

    private static final String PARAM_TARGET_HOST = "targetHost";

    private static final String PARAM_TARGET_PORT = "targetPort";

    private static final String PARAM_TARGET_PASSWORD = "targetPassword";

    private static final String PARAM_GUACD_HOST = "guacdHost";

    private static final String PARAM_GUACD_PORT = "guacdPort";

    private static final Map<String, ParameterConfiguration> PARAMETER_MAP;

    static {
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_TARGET_PROTOCOL", PARAM_TARGET_PROTOCOL, "vnc"));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_TARGET_HOST", PARAM_TARGET_HOST));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_TARGET_PORT", PARAM_TARGET_PORT, "5900"));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_TARGET_PASSWORD", PARAM_TARGET_PASSWORD, null));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_GUACD_HOST", PARAM_GUACD_HOST));
        PARAMETERS.add(new ParameterConfiguration("DEFAULT_GUACD_PORT", PARAM_GUACD_PORT, "4822"));
        PARAMETER_MAP = PARAMETERS.stream().collect(Collectors.toMap(ParameterConfiguration::getName, o -> o));
    }

    private static class ParameterConfiguration {

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        if (request.getQueryString().equals("connect") && !PARAMETERS.stream().allMatch(config -> config.check(request.getParameterMap()))) {
            setErrorResponse(response);
            return;
        }
        super.doPost(request, response);
    }

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) {

        // VNC connection information
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol(getParameter(PARAM_TARGET_PROTOCOL, request));
        config.setParameter("hostname", getParameter(PARAM_TARGET_HOST, request));
        config.setParameter("port", getParameter(PARAM_TARGET_PORT, request));
        if (getParameter(PARAM_TARGET_PASSWORD, request) != null) {
            config.setParameter("password", getParameter(PARAM_TARGET_PASSWORD, request));
        }

        // Connect to guacd, proxying a connection to the VNC server above
        try {
            GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                    new InetGuacamoleSocket(getParameter(PARAM_GUACD_HOST, request),
                            Integer.parseInt(getParameter(PARAM_GUACD_PORT, request))),
                    config
            );
            // Create tunnel from now-configured socket
            return new SimpleGuacamoleTunnel(socket);
        } catch (GuacamoleException ex) {
            return null;
        }
    }

    private String getParameter(String name, HttpServletRequest request) {
        return PARAMETER_MAP.get(name).getValue(request.getParameterMap());
    }

    private void setErrorResponse(HttpServletResponse response) throws ServletException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try {
            response.setContentType("text/plain");
            response.getWriter().println(SERVER_PARAMETER_ERROR);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

}
