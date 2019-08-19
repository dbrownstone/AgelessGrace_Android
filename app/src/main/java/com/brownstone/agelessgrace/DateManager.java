package com.brownstone.agelessgrace;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DateManager {

    public static DateManager mDateMgr;
    public static Set<String> startToEndDates;
    public static String todaysDateStr;

    private DateManager()
    {

    }

    public static void init(Context context)
    {
        if(mDateMgr == null)
            mDateMgr = new DateManager();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        todaysDateStr = formatter.format(new Date());
    }

    public static void setStartToEndDates() {

        startToEndDates = SharedPref.readSet("StartToEndDates",new HashSet<String>());
        if (startToEndDates.size() > 0) {
            return;
        }
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyyMMdd");
        String initDateString = SharedPref.read("StartingDate", todaysDateStr);//simpleFormatter.format(cal.getTime());
        Date thisDate;
        try {
            thisDate = simpleFormatter.parse(initDateString);
            cal.setTime(thisDate);
        } catch(ParseException e) {
            e.printStackTrace();
        }


        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");

        startToEndDates = new HashSet<String>(7);

        for(int l=0; l<=6; l++) {
            if (l > 0) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            startToEndDates.add(dateFormat.format(cal.getTime()));
        }
        SharedPref.writeSet("StartToEndDates", startToEndDates);
    }

    public static Set<String> getStartToEndDates() {
        if (startToEndDates == null || startToEndDates.size() == 0) {
            startToEndDates = SharedPref.readSet("StartToEndDates", new HashSet<String>());
        }
        return startToEndDates;
    }

    public static String getStartingDate() {
        if (todaysDateStr == null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            todaysDateStr = formatter.format(new Date());
        }
        if (SharedPref.keyExists("StartingDate")) {
            return SharedPref.read("StartingDate", todaysDateStr);
        }
        return todaysDateStr;
    }

    public static String getTheDateAccordingToTheTitle(String currentTitle) {
        String resultantDate = "";
        List<String> list = new ArrayList<String>(startToEndDates);
        Collections.sort(list);
        switch (currentTitle) {
            case "First Day":
                resultantDate = list.get(0);
                break;
            case "Second Day":
                resultantDate = list.get(1);
                break;
            case "Third Day":
                resultantDate = list.get(2);
                break;
            case "Fourth Day":
                resultantDate = list.get(3);
                break;
            case "Fifth Day":
                resultantDate = list.get(4);
                break;
            case "Sixth Day":
                resultantDate = list.get(5);
                break;
            case "Seventh Day":
                resultantDate = list.get(6);
                break;
        }
        return resultantDate;
    }
}
