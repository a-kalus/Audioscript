package com.example.audioscript;

import java.io.Serializable;

public class Course  implements Serializable {

    private int id;
    private String courseName;


    public Course(int id, String name) {
        this.id = id;
        this.courseName = name;
    }
    public Course(String name) {
        this.courseName = name;
    }

    public void setName(String name) {
        this.courseName = name;
    }

    public int getId() {
        return this.id;
    }

    public String getCourseName() {
        return this.courseName;
    }
}