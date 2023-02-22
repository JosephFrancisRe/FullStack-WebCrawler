package com.eulerity.hackathon.imagefinder;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Date;


public class WebCrawlerLogger {
    private final static Logger LOGGER = Logger.getLogger("WebCrawlerLogger");
    private static Date timeStamp = new Date();
    
    public static Logger init() {
        FileHandler fh;
        try {
            fh = new FileHandler("WebCrawlerLogger.log");

            LOGGER.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            LOGGER.log(Level.INFO, timeStamp + ": Logger Initialized");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, timeStamp + ": Exception thrown. Msg: ", e.getMessage());
        }
        return LOGGER;
    }
}
