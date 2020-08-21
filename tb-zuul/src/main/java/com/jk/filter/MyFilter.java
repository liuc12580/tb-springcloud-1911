package com.jk.filter;

import com.jk.utils.StringUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MyFilter extends ZuulFilter {
    @Override
    public String filterType() {
        System.out.println("路由之前过滤，时间：" + System.currentTimeMillis() );
        return "pre";
    }

    // 在项目启动完成后执行的
    @Override
    public int filterOrder() {
        System.out.println("执行过滤器的顺序，时间：" + System.currentTimeMillis());
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        System.out.println("是否要过滤，true,永远过滤，时间：" + System.currentTimeMillis());
        return true;
    }

    // run方法，是否可以执行，根据shouldFilter返回值来决定的，返回true则执行run方法，返回false不执行run方法
    @Override
    public Object run() throws ZuulException {
        System.out.println("执行业务逻辑，时间：" + System.currentTimeMillis());

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        HttpServletResponse response = requestContext.getResponse();

        String url = request.getRequestURL().toString();
        String method = request.getMethod();
        String token = request.getHeader("token"); // token令牌都会设置在header头部信息中

        System.out.println("请求的地址：" + url + ", 请求的方法：" + method + ", 请求的token令牌: " + token);

        //Object obj = request.getSession().getAttribute("user");

        if(StringUtil.isEmpty(token)){
            try {
                requestContext.setSendZuulResponse(false); // 返回结果false
                requestContext.setResponseStatusCode(401); // 用户未授权，或者未传入指定的token令牌
                //让浏览器用utf8来解析返回的数据
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                //servlet用UTF-8转码，而不是用默认的ISO8859
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("您请求的路径未授权或者缺少token令牌");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
