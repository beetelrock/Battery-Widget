package com.BeetelRockBattery;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class Logger {
    private static Logger LOGGER = null;
    private static FileWriter sLogWriter = null;
    public static String LOG_FILE_NAME =
        Environment.getExternalStorageDirectory() + "/batterylog.txt";

    public synchronized static Logger getLogger (Context c) {
    	//if(LOGGER == null)
    	//	LOGGER = new Logger();
        return LOGGER;
    }

    private Logger() {
    }

    static public synchronized void close() {
        if (sLogWriter != null) {
            try {
                sLogWriter.close();
            } catch (IOException e) {
                // Doesn't matter
            }
            sLogWriter = null;
        }
    }

    static public synchronized void log(Exception e) {
        if (sLogWriter != null) {
            log("Exception", "Stack trace follows...");
            Log.e("BATTERY", "Stack trace follows...");
            PrintWriter pw = new PrintWriter(sLogWriter);
            e.printStackTrace(pw);
            pw.flush();
        }
    }

    @SuppressWarnings("deprecation")
    static public synchronized void log(String prefix, String str) {
        if (LOGGER == null) {
            LOGGER = new Logger();
            log("Logger", "\r\n\r\n --- New Log ---" + LOG_FILE_NAME);
            
            if(LOGGER == null) {
            	return;
            }
            
        }
        Date d = new Date();
        int hr = d.getHours();
        int min = d.getMinutes();
        int sec = d.getSeconds();
        int day = d.getDate();
        int mon = d.getMonth() + 1;
        int year = d.getYear() + 1900;

        // I don't use DateFormat here because (in my experience), it's much slower
        StringBuffer sb = new StringBuffer(256);
        sb.append('[');
        sb.append(hr);
        sb.append(':');
        if (min < 10)
            sb.append('0');
        sb.append(min);
        sb.append(':');
        if (sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        sb.append("-");
        if(day < 10) {
        	sb.append("0");
        }
        sb.append(day);
        sb.append(':');
        if(mon < 10) {
        	sb.append("0");
        }
        sb.append(mon);
        sb.append(':');
        sb.append(year);
        sb.append("] ");
        if (prefix != null) {
            sb.append(prefix);
            sb.append("| ");
        }
        sb.append(str);
        sb.append("\r\n");
        String s = sb.toString();

        //Log.e("BATTERY", s);
        if (sLogWriter != null) {
            try {
                sLogWriter.write(s);
                sLogWriter.flush();
            } catch (IOException e) {
            	
                // Something might have happened to the sdcard
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    // If the card is mounted and we can create the writer, retry
                    LOGGER = new Logger();
                    if (sLogWriter != null) {
                        try {
                            log("Logger", "Exception writing log; recreating...");
                            log(prefix, str);
                            Log.e("BATTERY", "Exception writing log; recreating...");
                            Log.e("BATTERY", str);
                        } catch (Exception e1) {
                            // 
                        	Log.e("BATTERY", "Nothing to do at this point...");
                        }
                    } else {
                    	Log.e("BATTERY", "log writter null...");
                    }
                } else {
                	Log.e("BATTERY", "Media state not mounted...");
                }
            }
        }
    }
}
