package com.eulerity.hackathon.imagefinder;

import java.util.ArrayList;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler implements Runnable {
	private int ID;
	private int image_crawler_count = 0;
	private Thread thread;
	private String origin;
	private ArrayList<String> accessed_nodes = new ArrayList<String>();
	private ArrayList<String> crawled_images = new ArrayList<String>();
	private static final int MAX_DEPTH = 5;

	public WebCrawler(String url, int id) {
		origin = url;
		ID = id;
		System.out.print("WebCrawler with ID of " + ID + " and an origin of " + origin + " created.");
		deployThread();
	}
	
	private void deployThread() {
		thread = new Thread(this);
		thread.start();
	}
	
	private void crawl(int currDepth, String url) {
		if (currDepth < MAX_DEPTH) {
			Document doc = request(url);
			
			for (Element link : doc.select("a[href]")) {
				String next_link = link.absUrl("href");
			
				if (accessed_nodes.contains(next_link) == false) {
					ImageCrawler iC = new ImageCrawler(doc, image_crawler_count++);
					crawl(currDepth++, next_link);
				}
			}
		}
	}
	
	private Document request(String url) {
		try {
			Connection con = Jsoup.connect(url);
			Document doc = con.get();
			
			if (con.response().statusCode() == 200) {
				String title = doc.title();
				System.out.println("\n**WebCrawler ID: " + ID + " received a webpage entitled " + title + " that can be found at " + url);
				accessed_nodes.add(url);
				
				return doc;
			}
			return null;
		}
		catch (IOException e) {
			return null;
		}
	}
	
	public Thread getThread() {
		return thread;
	}

	@Override
	public void run() {
		crawl(0, origin);
	}
	
}
