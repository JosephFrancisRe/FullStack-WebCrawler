package com.eulerity.hackathon.imagefinder;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CrawlerLogger {
    private static Logger logger = Logger.getLogger("CrawlerLog");
    
    public static void init() {
        FileHandler fh;
        try {
            fh = new FileHandler("CrawlerLogFile.log");

            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info("Logger Initialized");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception ::", e);
        }
    }
}
