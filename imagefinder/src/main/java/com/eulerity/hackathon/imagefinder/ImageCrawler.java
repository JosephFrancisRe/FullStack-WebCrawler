package com.eulerity.hackathon.imagefinder;

import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ImageCrawler implements Runnable {
	private int ID;
	private Thread thread;
	private Document source;
	private String title;
	private CopyOnWriteArrayList<String> crawled_images;

	public ImageCrawler(Document doc, int id, CopyOnWriteArrayList<String> aList) {
		source = doc;
		title = source.title();
		ID = id;
		crawled_images = aList;
		System.out.println("ImageCrawler with ID of " + ID + " and source of " + title + " created.");
		deployThread();
	}
	
	private void deployThread() {
		thread = new Thread(this);
		thread.start();
	}
	
	private void crawl(Document doc) {
		if (doc != null) {
			Elements images = doc.getElementsByTag("img");
			
			for (Element src: images) {
				if (crawled_images.contains(src) == false) {
					System.out.println("Image src = " + src.attr("abs:src"));
					crawled_images.add(src.attr("abs:src"));
				}
			}
		}
	}
	
	public Thread getThread() {
		return thread;
	}

	@Override
	public void run() {
		crawl(source);
	}
	
}
