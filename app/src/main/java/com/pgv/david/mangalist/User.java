package com.pgv.david.mangalist;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

public class User implements Serializable{
    private String nick;
    private String email;
    private String gender;
    private String age;
    private ArrayList<String> planToRead = new ArrayList<>();
    private ArrayList<String> reading = new ArrayList<>();
    private ArrayList<String> completed = new ArrayList<>();

    public User() {
    }

    public User(String nick, String email, String gender, String age) {
        this.nick = nick;
        this.email = email;
        this.gender = gender;
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNick() {
        return nick;
    }

    public String getEmail() {
        return email;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public ArrayList<String> getPlanToRead() {
        return planToRead;
    }

    public void setPlanToRead(ArrayList<String> planToRead) {
        this.planToRead = planToRead;
    }

    public ArrayList<String> getReading() {
        return reading;
    }

    public void setReading(ArrayList<String> reading) {
        this.reading = reading;
    }

    public ArrayList<String> getCompleted() {
        return completed;
    }

    public void setCompleted(ArrayList<String> completed) {
        this.completed = completed;
    }
}
