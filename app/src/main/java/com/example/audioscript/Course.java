package com.example.audioscript;

import java.io.Serializable;

public class Course  implements Serializable {

    int id;
    String course_name;


    public Course(int id, String name) {
        this.id = id;
        this.course_name = name;
    }
    public Course(String name) {
        this.course_name = name;
    }

    public void setName(String name) {
        this.course_name = name;
    }

    // getter
    public int getId() {
        return this.id;
    }

    public String getCourseName() {
        return this.course_name;
    }
}