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

package de.bcoding.guacamole;

import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Simple tunnel example with hard-coded configuration parameters.
 */
public class SimpleGuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {

    public final static long serialVersionUID = 1;

    private static final String SERVER_PARAMETER_ERROR = "Please specify server parameter in form HOSTNAME:PORT";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        if (request.getQueryString().equals("connect") && !GuacamoleConnectionFactory.PARAMETERS.stream().allMatch(config -> config.check(request.getParameterMap()))) {
            setErrorResponse(response);
            return;
        }
        super.doPost(request, response);
    }

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) {
        final Map<String, String[]> parameterMap = request.getParameterMap();
        return new GuacamoleConnectionFactory(parameterMap).create();
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
