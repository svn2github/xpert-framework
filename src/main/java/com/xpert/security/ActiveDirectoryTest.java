package com.xpert.security;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.ldap.LdapContext;

/**
 *
 * @author ayslan
 */
public class ActiveDirectoryTest {

    public static void testConnection(String username, String password) {
        try {
            LdapContext context = ActiveDirectory.getConnection("ayslan", "123456");
            context.close();
            System.out.println("Success!");
        } catch (CommunicationException ex) {
            System.out.println(ex.getMessage());
        } catch (AuthenticationException ex) {
            System.out.println(ex.getMessage());
        } catch (NamingException ex) {
            Logger.getLogger(ActiveDirectoryTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void testChangePassword(String username, String oldPass, String newPass) {
        try {
            LdapContext conn = ActiveDirectory.getConnection(username, oldPass);
            ActiveDirectory.getUser(username, conn).changePassword(oldPass, newPass, conn);
            conn.close();
            System.out.println("Success!");
        } catch (CommunicationException ex) {
            System.out.println(ex.getMessage());
        } catch (AuthenticationException ex) {
            System.out.println(ex.getMessage());
        } catch (InvalidAttributeValueException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(ActiveDirectoryTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        testChangePassword("ayslan", "paranoid", "123456");

    }

}
