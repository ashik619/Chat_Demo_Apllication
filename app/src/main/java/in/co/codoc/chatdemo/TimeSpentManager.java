package in.co.codoc.chatdemo;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ashik619 on 03-02-2017.
 */
public class TimeSpentManager {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static Context context;

    public static String setTimeAgo(Context ctx, long time) {

        context = ctx;
        if (time < 1000000000000L) {

            time *= 1000;
        }

        long now = getCurrentTime(ctx);

        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;

        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            //return diff / DAY_MILLIS + " days ago";
            return   getTimeStamp(time);
        }
    }


    private static long getCurrentTime(Context ctx) {

        return new Date().getTime();

    }
    public static String getTimeStamp(long ms){

        String timeStamp;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        month+=1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String formattedDate = df.format(c.getTime());
        timeStamp = ""+day+"/"+month+"/"+year + " at "+formattedDate ;
        return timeStamp;
    }


}
