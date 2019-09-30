package com.example.audioscript;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class LectureItem implements Comparable<LectureItem> {

    private String name;
    private String content;
    private GregorianCalendar cal;

    public LectureItem(String name, String content, int day, int month, int year) {
        this.name = name;
        this.content = content;
        cal = new GregorianCalendar(year, month, day);
    }


    public String getName() {
        return name;
    }

    public String getFormattedDate() {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,
                Locale.GERMANY);

        return df.format(cal.getTime());
    }

    private Date getDueDate() {
        return cal.getTime();
    }

    @Override
    public int compareTo(LectureItem lectureItem) {
        return getDueDate().compareTo(lectureItem.getDueDate());
    }

    public String getContent() {return content;
    }
}
