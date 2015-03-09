package com.xpert.security.bean;

import com.xpert.core.exception.BusinessException;
import com.xpert.faces.utils.FacesMessageUtils;
import com.xpert.faces.utils.FacesUtils;
import com.xpert.security.EncryptionType;
import com.xpert.security.model.User;
import com.xpert.security.session.AbstractUserSession;
import com.xpert.utils.Encryption;
import java.security.NoSuchAlgorithmException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 *
 * @author Ayslan
 */
public abstract class SecurityLoginBean {

    private String userLogin;
    private String userPassword;

    public EncryptionType getEncryptionType() {
        return EncryptionType.SHA256;
    }

    public boolean isLoginIgnoreCase() {
        return false;
    }

    public boolean isLoginUpperCase() {
        return true;
    }

    public boolean isLoginLowerCase() {
        return false;
    }

    public boolean isValidateWhenNoRolesFound() {
        return true;
    }

    public Class getUserClass() {
        return null;
    }

    public abstract AbstractUserSession getUserSession();

    public abstract EntityManager getEntityManager();

    /**
     * Define something when login is successful like add a message "Welcome
     * user"
     *
     * @return
     */
    public void onSucess(User user) {
    }

    /**
     * Define something when login is unsuccessful
     *
     * @return
     */
    public void onError() {
    }

    /**
     * Redirect to this page when login is successful
     *
     * @return
     */
    public abstract String getRedirectPageWhenSucess();

    /**
     * Redirect to this page when user logout
     *
     * @return
     */
    public abstract String getRedirectPageWhenLogout();

    /**
     * Message when user was not found.
     *
     * @return
     */
    public String getUserNotFoundMessage() {
        return "User not found";
    }

    public String getUserWithoutPassword() {
        return "User with no password in database";
    }

    /**
     * Message when user is not active not found.
     *
     * @return
     */
    public String getInactiveUserMessage() {
        return "Inactive User";
    }

    /**
     * Message when there is no role.
     *
     * @return
     */
    public String getNoRolesFoundMessage() {
        return "No roles found for this user";
    }

    /**
     * Executed before user query in database
     *
     * @return
     */
    public boolean validate() {
        boolean valid = true;
        if (userLogin == null || userLogin.trim().isEmpty()) {
            addErrorMessage("User is required");
            valid = false;
        }
        if (userPassword == null || userPassword.trim().isEmpty()) {
            addErrorMessage("Password is required");
            valid = false;
        }
        return valid;
    }

    /**
     * Executed after user query in database
     *
     * @return
     */
    public boolean validate(User user) throws BusinessException {
        return true;
    }

    public void logout() {
        FacesUtils.invalidateSession();
        FacesUtils.redirect(getRedirectPageWhenLogout());
    }

    /**
     *
     * @return Query String to find User
     */
    public String getUserLoginQueryString() {
        String queryString = " FROM " + getUserClass().getName();
        if (isLoginIgnoreCase()) {
            queryString = queryString + " WHERE UPPER(userLogin) = UPPER(?1) ";
        } else {
            queryString = queryString + " WHERE userLogin = ?1 ";
        }
        return queryString;
    }

    /**
     *
     * @param entityManager
     * @param queryString String to find User
     * @param login User Login
     * @return
     */
    public Query getUserLoginQuery(EntityManager entityManager, String queryString, String login) {
        return entityManager.createQuery(queryString).setParameter(1, login);
    }

    public User getUser(String login, String password) {

        User user = null;
        EntityManager entityManager = getEntityManager();
        if (entityManager == null || getUserClass() == null) {
            throw new IllegalArgumentException("To get the user you must override methods getEntityManager() and getUserClass() or override getUser() and do your own logic");
        }

        String queryString = getUserLoginQueryString();

        if (isLoginUpperCase()) {
            login = login.toUpperCase();
        } else if (isLoginLowerCase()) {
            login = login.toLowerCase();
        }
        try {
            user = (User) getUserLoginQuery(entityManager, queryString, login).getSingleResult();
        } catch (NoResultException ex) {
            //
        }

        //verify user password
        if (user != null) {
            user = authenticateUserPassword(user, password);
        }
        
        return user;
    }

    public User authenticateUserPassword(User user, String password) {
        //compare password encryptedPassword
        if (user != null && user.getUserPassword() != null && !user.getUserPassword().isEmpty()) {
            try {
                String encryptedPassword = null;
                if (getEncryptionType() != null) {
                    if (getEncryptionType().equals(EncryptionType.SHA256)) {
                        encryptedPassword = Encryption.getSHA256(password);
                    }
                    if (getEncryptionType().equals(EncryptionType.MD5)) {
                        encryptedPassword = Encryption.getMD5(password);
                    }
                } else {
                    encryptedPassword = password;
                }

                if (!user.getUserPassword().equals(encryptedPassword)) {
                    return null;
                }
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }

        }
        return user;
    }

    public void login() {

        //clear user session
        if (getUserSession() != null) {
            getUserSession().setUser(null);
        }

        if (validate()) {
            User user = getUser(userLogin, userPassword);

            if (user == null) {
                addErrorMessage(getUserNotFoundMessage());
                onError();
                return;
            }
            if (!user.isActive()) {
                addErrorMessage(getInactiveUserMessage());
                onError();
                return;
            }
            if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) {
                addErrorMessage(getUserWithoutPassword());
                onError();
                return;
            }
            try {
                //more validation
                validate(user);
            } catch (BusinessException ex) {
                FacesMessageUtils.error(ex);
                return;
            }

            //set user in session
            if (getUserSession() != null) {
                getUserSession().setUser(user);
                getUserSession().createSession();
                //when no roles found, login is not sucessful
                if (isValidateWhenNoRolesFound() && (getUserSession().getRoles() == null || getUserSession().getRoles().isEmpty())) {
                    addErrorMessage(getNoRolesFoundMessage());
                    getUserSession().setUser(null);
                    return;
                }
            }
            onSucess(user);
            FacesUtils.redirect(getRedirectPageWhenSucess());
        }

    }

    public void addErrorMessage(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
