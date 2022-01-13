package com.meshenger;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class CorsFilter implements Filter{

    /**
     * Authorisiert die Verarbeitung der Daten fuer alle Domains.
     * HTTP OPTIONS antwortet mit ACCEPTED (202) status code via Cross-Origin Resource Sharing (CORS).
     */
	@Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
 
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        System.out.println("CORSFilter HTTP Request: " + request.getMethod());
 
        ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST, DELETE");
 
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
 
        if (request.getMethod().equals("OPTIONS")) {
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            return;
        }
 
        chain.doFilter(request, servletResponse);
    }

    @Override
    public void destroy() {}

    @Override
    public void init(FilterConfig filterConfig)throws ServletException{}
}