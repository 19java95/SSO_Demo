package com.imooc.sso.filter;

import com.sun.org.apache.bcel.internal.util.Objects;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by TangTian on 2018/10/6.
 */
@WebFilter(filterName = "UserFilter")
public class UserFilter implements Filter {
    private String server;
    private String app;
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ticket = null;
        if (Objects.equals("/ssoLogout",((HttpServletRequest)request).getServletPath())){
            ((HttpServletResponse)response).sendRedirect(server + "/ssoLogin?source=" + app);
            return;
        }
        if (null != ((HttpServletRequest)request).getCookies()){
            for (Cookie cookie :((HttpServletRequest)request).getCookies()){
                if (Objects.equals(cookie.getName(),"Ticket_Granting_Ticket")){
                    ticket = cookie.getValue();
                    break;
                }
            }
        }
        if (!Objects.equals(null,ticket)){

            //判断超时时间

            String[] values = ticket.split(":");
            ticket = request.getParameter("ticket");
            if (Long.valueOf(values[1])< System.currentTimeMillis()){
                //超时
                if (Objects.equals(null,ticket)){
                    ((HttpServletResponse)response).sendRedirect(server + "/ssoLogin?source=" + app);
                    return;
                }else {
                    ticket =  ticket +":" + (System.currentTimeMillis() + 10000);
                    ((HttpServletResponse)response).addCookie(new Cookie("Ticket_Granting_Ticket",ticket));
                    filterChain.doFilter(request,response);
                    return;
                }
            }
            filterChain.doFilter(request,response);
            return;
        }



        ticket = request.getParameter("ticket");
        if (!Objects.equals(null,ticket) && !Objects.equals("",ticket.trim())){
            ticket =  ticket +":" + (System.currentTimeMillis() + 10000);
            ((HttpServletResponse)response).addCookie(new Cookie("Ticket_Granting_Ticket",ticket));
            filterChain.doFilter(request,response);
        }
        else {
            ((HttpServletResponse)response).sendRedirect(server + "/ssoLogin?source=" + app);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {

        server = filterConfig.getInitParameter("server");
        app = filterConfig.getInitParameter("app");

    }

}
