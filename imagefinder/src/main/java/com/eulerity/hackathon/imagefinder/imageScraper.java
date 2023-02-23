package com.eulerity.hackathon.imagefinder;

import java.io.IOException;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * The class ImageScraper is used to instantiate and object that uses JSoup to scrape the images from a webpage
 * and store it to be displayed via javascript. The class is threaded and thus uses a threadsafe array list for
 * storing references to images. Images are defined as having URIs that contain .ico, .png, .jpeg, .jpg, or .gif.
 */
public class ImageScraper implements Runnable {
    private String url;
    private CopyOnWriteArrayList imageList;
    private Thread thread;
    private final Logger LOGGER;
    private static Date timeStamp = new Date();

    /**
	 * Constructor for a threaded image scraper object.
	 * 
     * @param 	url     	: A String value representing the desired domain's full URL
     * @param 	imageList	: A threadsafe ArrayList meant to store references to all unique images scraped
     * @param   id          : An integer values representing the unique identifier for the object
     * @param   LOGGER      : A logger object that facilitates the persistent storage of logging information
     * @throws 	IOException	: Signals a failed or interrupted input/output operation
	 */
    public ImageScraper(String url, CopyOnWriteArrayList imageList, int id, Logger LOGGER){
        this.url = url;
        this.imageList = imageList;
        this.LOGGER = LOGGER;
        LOGGER.log(Level.INFO, timeStamp + ": Servlet response printed containing the images scraped from " + url + ".");
        deployThread();
    }

    /**
	 * Initializes and starts a thread.
	 */
	private void deployThread() {
		thread = new Thread(this);
		thread.start();
	}

    /**
	 * Commits a thread to the extractImages actions defined in the run method.
	 */
    @Override
    public void run() {
        try {
            extractImages();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, timeStamp + ": An IO exception was thrown when extracting images from " + url + ". Msg: ", e);
        }
    }

    /**
	 * Connects to a url, scrapes all it's unique images, and persistently stores the images.
	 * 
     * @throws 	IOException	: Signals a failed or interrupted input/output operation
	 */
    public void extractImages() throws IOException {
        Connection connection = Jsoup.connect(url).header("Accept-Language", "en");
        Document document = connection.ignoreContentType(true).get();

        Elements images = document.select("img[src~=(?i)\\.(ico|png|jpe?g|gif)]");

        for (Element img : images) {
            if (!imageList.contains(img)) {
                imageList.add(img.absUrl("src"));
            }
        }
    }

    /**
	 * Retrieves a thread.
	 * 
	 * @return 	thread	: The active thread
	 */
    public Thread getThread(){
        return thread;
    }
}