package com.mythcon.savr.ngelih.Model;

/**
 * Created by SAVR on 20/02/2018.
 */

public class User {
    private String name;
    private String pass;
    private String phone;

    public User() {
    }

    public User(String name, String pass) {
        this.name = name;
        this.pass = pass;

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
