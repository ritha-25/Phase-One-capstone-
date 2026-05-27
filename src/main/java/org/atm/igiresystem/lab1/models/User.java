package org.atm.igiresystem.lab1.models;

public class User {

    private int    id;
    private String phone;    // used as login identifier (like MoMo)
    private String password; // internal system password (admin use)
    private String role;     // ADMIN or USER

    public User(int id, String phone, String password, String role) {
        this.id       = id;
        this.phone    = phone;
        this.password = password;
        this.role     = role;
    }

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getPhone()                 { return phone; }
    public void setPhone(String phone)       { this.phone = phone; }

    // kept for backward compat — phone is the username
    public String getUsername()              { return phone; }
    public void setUsername(String phone)    { this.phone = phone; }

    public String getPassword()              { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole()                  { return role; }
    public void setRole(String role)         { this.role = role; }

    public boolean isAdmin()                 { return "ADMIN".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return "User{id=" + id + ", phone=" + phone + ", role=" + role + "}";
    }
}
