package com.imooc.sso.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * 该Servlet是单点登录的服务端
 *
 */
public class LoginServlet extends HttpServlet {

    private String domains;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        /**
         * domains参数，在web.xml文件中进行了配置，值为http://127.0.0.1:8081,http://127.0.0.1:8082
         * 该参数中模拟两个进行单点登录的应用的地址，用逗号隔开
         * 可以理解成http://127.0.0.1:8081对应天猫的登录，http://127.0.0.1:8082对应淘宝的登录
         */
        domains = config.getInitParameter("domains");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //如果servletPath的值是login，表示是登录界面提交过来的请求
        if (Objects.equals("/login", request.getServletPath())) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            //以WebApp1为例，WebApp1的Module启动后，地址栏显示内容为：http://127.0.0.1:8080/ssoLogin?source=http://127.0.0.1:8081
            //下面source的内容是值地址中的http://127.0.1:8081，标识出哪个Web应用在登录
            String source = request.getParameter("source");

            //如果source没有值的处理
            if (null == source || Objects.equals("", source)) {
                String referer = request.getHeader("referer");//获取来访者的地址refer
                //取出source=后面的内容，也就是形如http://127.0.1:8081这样的地址
                source = referer.substring(referer.indexOf("source=") + 7);
            }
            //如果用户名和密码的值相等，就认为登录成功
            //Objects是JDL 8中的一个类，它的equals()方法用来判断两个参数是否相等
            if (Objects.equals(username, password)) {
                //ticket是作为cookie值的，cookie值只要一致，一个应用登录成功后，另一个应用可以自动登录
                //UUID.randomUUID()用来生成唯一的字符串标识
                //因为UUID生成的字符串中间有-，这里调用replace()方法把空串去掉
                String ticket = UUID.randomUUID().toString().replace("-", "");
                System.out.println("******************************:" + ticket);
                //登录成功后重定向到提交登录请求的Web应用的地址
                //地址形式为：http://127.0.0.1:8081/main?ticket=cee94fc78f3345d6a5c6741695c3d3ad&domains=http://127.0.0.1:8082
                //main是WebApp1或WebApp2中servelt的地址
                //domains是另一个Web应用的地址，之所以有domains=，是希望一个应用登录成功后，通知另外一个应用，那么另外一个应用收到后就可以自动登录了
                //之所以用replace方法，是因为domains变量中含有两个应用的地址，把source地址及分隔的逗号去掉，只留下另一个应用的地址
                response.sendRedirect(source + "/main?ticket=" + ticket + "&domains=" +
                        domains.replace(source + ",", "").replace("," + source, "").replace(source, ""));
            } else {
                //登录不成功，转发到登录页面
                request.setAttribute("source", source);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }
        } else if (Objects.equals("/ssoLogin", request.getServletPath())) {//如果请求的servletPath是ssoLogin，就跳转到登录界面
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        } else if (Objects.equals("/ssoLogout", request.getServletPath())) {//单点登录退出的处理
            String source = request.getParameter("source");

            //由于用户信息没有存储在内存或者类似memcache这样的缓存中，所以这里没有相关的处理
            //在ssoLogout请求时，传过来当前的用户名，根据用户名查找内存或者缓存，删除相应信息，以完成退出
            //用户从哪来？在实行ssoLogin时返回的ticket中，要包含用户的信息（能标识用户唯一性即可，uuid也可以，只是需要在sso的server中记录一下这个uuid和用户的对应关系）
            //webapp1或者webapp2在调用ssoLogout时把ticket传回来即可

            if (null == source || Objects.equals("", source)) {
                String referer = request.getHeader("referer");
                source = referer.substring(referer.indexOf("source=") + 7);
            }

            response.sendRedirect(source + "/logout?domains=" +
                    domains.replace(source + ",", "").replace("," + source, "").replace(source, ""));
        }

    }
}
