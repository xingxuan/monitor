package cn.thextrader.monitor;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CharacterEncodingFilter implements Filter {
    private String encoding;

    public void init(FilterConfig fConfig) throws ServletException {
        if (encoding == null) {
            encoding = Constant.CHARSET_DEFAULT;
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (!"/favicon.ico".equalsIgnoreCase(httpServletRequest.getRequestURI())) {
            if (httpServletRequest.getCharacterEncoding() == null) {
                httpServletRequest.setCharacterEncoding(encoding);
                httpServletResponse.setCharacterEncoding(encoding);
            }
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    public void destroy() {

    }

}
