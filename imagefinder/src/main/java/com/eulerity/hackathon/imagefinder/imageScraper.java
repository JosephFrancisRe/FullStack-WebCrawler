package com.eulerity.hackathon.imagefinder;

import java.io.IOException;

import java.util.concurrent.CopyOnWriteArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Initializes and starts a thread.
 */
public class imageScraper implements Runnable {
    private String url;
    private CopyOnWriteArrayList imageList;
    private Thread thread;

    /**
	 * Constructor for a threaded image scraper object.
	 * 
     * @param 	url     	: A String value representing the desired domain's full URL
     * @param 	imageList	: A threadsafe ArrayList meant to store references to all unique images scraped
     * @param   id          : An integer values representing the unique identifier for the object 
     * @throws 	IOException	: Signals a failed or interrupted input/output operation
	 */
    public imageScraper(String url, CopyOnWriteArrayList imageList, int id){
        this.url = url;
        this.imageList = imageList;
        System.out.println("ImageScraper with ID of " + id + " and source of " + url + " created.");

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
            e.printStackTrace();
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