package com.xpert.security;

import com.xpert.Constants;
import com.xpert.security.model.Role;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ayslan
 */
public class SecuritySessionManager {

    public static void clearRoles(ServletRequest request) {
        ((HttpServletRequest) request).getSession().removeAttribute(Constants.USER_ROLES);
    }

    public static void clearRoles() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(Constants.USER_ROLES);
    }

    /**
     * Same as putRoles(ServletRequest request, List<Role> roles) but uses
     * FacesContext
     *
     * @param roles
     */
    public static void putRoles(List<Role> roles) {
        putRoles((ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest(), roles);
    }

    /**
     * Put roles in session Map
     *
     * @param request
     * @param roles
     */
    public static void putRoles(ServletRequest request, List<Role> roles) {


        if (roles == null || roles.isEmpty()) {
            return;
        }

        /**
         * Put keys and urls in a Map to optmize perfomance
         */
        Map<String, Role> rolesMap = new HashMap<String, Role>();
        for (Role role : roles) {
            if (role.getKey() != null) {
                for (String string : role.getKey().split(",")) {
                    rolesMap.put(string, role);
                }
            }
        }
        Map<String, String> urlsMap = new HashMap<String, String>();
        for (Role role : roles) {
            if (role.getUrl() != null && !role.getUrl().isEmpty()) {
                for (String url : role.getUrl().split(",")) {
                    String urlFormated = null;
                    int indexOfQuery = url.indexOf("?");
                    if (indexOfQuery > -1) {
                        urlFormated = url.substring(0, indexOfQuery).trim();
                    } else {
                        urlFormated = url.trim();
                    }
                    urlsMap.put(urlFormated, urlFormated);
                }
            }
        }
        ((HttpServletRequest) request).getSession().setAttribute(Constants.USER_ROLES, roles);
        ((HttpServletRequest) request).getSession().setAttribute(Constants.USER_ROLES_KEY_MAP, rolesMap);
        ((HttpServletRequest) request).getSession().setAttribute(Constants.USER_ROLES_URL_MAP, urlsMap);

    }

    /**
     * same as hasURL(String url, ServletRequest request) but uses FacesContext
     *
     * @param key
     * @return
     */
    public static boolean hasURL(String url) {
        return hasURL(url, (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
    }

    /**
     * Verify if user can acess url.
     *
     * @param key
     * @return
     */
    public static boolean hasURL(String url, ServletRequest request) {
        Map<String, String> urlsMap = (Map<String, String>) ((HttpServletRequest) request).getSession().getAttribute(Constants.USER_ROLES_URL_MAP);
        if (urlsMap != null && urlsMap.get(url.trim()) != null) {
            return true;
        }
        return false;
    }

    /**
     * same as hasRole(String key, ServletRequest request) but uses FacesContext
     * to get the request
     *
     * @param key
     * @return
     */
    public static boolean hasRole(String key) {
        return hasRole(key, (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
    }

    /**
     * Verify if user has the role os a permission. It can be multiple keys
     * (separeted with commas)
     *
     * @param key
     * @param request
     * @return
     */
    public static boolean hasRole(String key, ServletRequest request) {
        if (getRole(key, request) != null) {
            return true;
        }
        return false;
    }

    /**
     * Same as getRole(String key, ServletRequest request) but uses FacesContext
     *
     * @param key
     * @param request
     * @return
     */
    public static Role getRole(String key) {
        return getRole(key, (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
    }

    /**
     * Get role from user. The key can be separeted with commas, so the first
     * result is retrieved
     *
     * @param key
     * @param request
     * @return
     */
    public static Role getRole(String key, ServletRequest request) {
        Map<String, Role> rolesMap = (Map<String, Role>) ((HttpServletRequest) request).getSession().getAttribute(Constants.USER_ROLES_KEY_MAP);
        if (key != null && !key.trim().isEmpty() && rolesMap != null) {
            String[] keys = key.split(",");
            for (String c : keys) {
                Role role = rolesMap.get(c.trim());
                if (role != null) {
                    return role;
                }
            }
        }
        return null;
    }

    public static List<Role> getRoles(ServletRequest request) {
        return (List<Role>) ((HttpServletRequest) request).getSession().getAttribute(Constants.USER_ROLES);
    }

    public static List<Role> getRoles(FacesContext context) {
        return (List<Role>) context.getExternalContext().getSessionMap().get(Constants.USER_ROLES);
    }
}
