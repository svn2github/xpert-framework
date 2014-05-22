package com.xpert.security.filter;

import com.xpert.faces.utils.FacesUtils;
import com.xpert.security.SecuritySessionManager;
import com.xpert.security.session.AbstractUserSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ayslan
 */
public abstract class AbstractSecurityFilter implements Filter {
    
    private static final Logger logger = Logger.getLogger(AbstractSecurityFilter.class.getName());

    /**
     * Name of session bean to get from HttpSession
     *
     * @return
     */
    public abstract String getUserSessionName();

    /**
     * Custom autentication. Define here more authetication logic
     *
     * @param request
     * @param response
     * @return
     */
    public boolean getMoreAuthentication(ServletRequest request, ServletResponse response) {
        return true;
    }

    /**
     * Page to redirect if autentication fails
     *
     * @return
     */
    public abstract String getHomePage();

    /**
     * Define a logic to error. Called on exception in method
     * "chain.doFilter(request, response);"
     */
    public void onError() {
    }

    /**
     * URLs the are ignored on requests after user authenticated
     *
     * @return
     */
    public abstract String[] getIgnoredUrls();
    
    
    public AbstractUserSession getSessionBean(ServletRequest request){
        return (AbstractUserSession) getFromSession(request, getUserSessionName());
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        
        AbstractUserSession userSession = getSessionBean(request);
        
        if (userSession == null || !isAuthenticated(userSession)) {
            if (isDebug()) {
                logger.log(Level.INFO, "User not authenticated redirecting to: {0}", getHomePage());
            }
            redirectHome(request, response);
            return;
        }
        
        if (!hasUrl((HttpServletRequest) request)) {
            if (isDebug()) {
                logger.log(Level.INFO, "User {0} not authorized to url: {1}", new Object[]{userSession.getUser().getUserLogin(), ((HttpServletRequest) request).getRequestURI()});
            }
            redirectHome(request, response);
            return;
        }
        
        if (getMoreAuthentication(request, response)) {
            try {
                chain.doFilter(request, response);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, null, ex);
                onError();
            }
        }
        
    }
    
    public Object getFromSession(ServletRequest request, String attribute) {
        return ((HttpServletRequest) request).getSession().getAttribute(attribute);
    }
    
    public boolean hasUrl(HttpServletRequest request) {
        String currentView = request.getRequestURI().replaceFirst(request.getContextPath(), "");
        if (getIgnoredUrls() != null && Arrays.asList(getIgnoredUrls()).contains(currentView)) {
            return true;
        }
        return SecuritySessionManager.hasURL(currentView, request);
    }
    
    public void redirectHome(ServletRequest request, ServletResponse response) {
        //create faces context
        FacesUtils.getFacesContext((HttpServletRequest) request, (HttpServletResponse) response);
        FacesUtils.redirect(getHomePage());
    }

    /**
     * Log events on filter
     *
     * @return
     */
    public boolean isDebug() {
        return true;
    }
    
    public boolean isAuthenticated(AbstractUserSession userSession) {
        return userSession.isAuthenticated();
    }
}
