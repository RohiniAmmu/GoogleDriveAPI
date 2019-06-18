package com.drive.client;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;


public class ApplicationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws java.io.IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String uri = httpRequest.getRequestURI();
        httpRequest.setAttribute("ROOT_REQUEST_URI", uri);
        
        RequestDispatcher rd=request.getRequestDispatcher(uri);  
        httpRequest.getRequestDispatcher(uri).forward(request, response);
    }

    @Override
    public void destroy(){}

    @Override
    public void init(FilterConfig filterConfig)
    throws ServletException{}
}

