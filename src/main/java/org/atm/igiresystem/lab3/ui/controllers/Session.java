package org.atm.igiresystem.lab3.ui.controllers;

import org.atm.igiresystem.lab1.models.Customer;
import org.atm.igiresystem.lab1.models.User;

public class Session {

    private static User     currentUser;
    private static Customer currentCustomer;

    private Session() {}

    public static User getUser()                        { return currentUser; }
    public static void setUser(User user)               { currentUser = user; }

    public static Customer getCustomer()                { return currentCustomer; }
    public static void setCustomer(Customer customer)   { currentCustomer = customer; }

    public static void clear()                          { currentUser = null; currentCustomer = null; }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
