package com.eulerity.hackathon.imagefinder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Crawler implements Runnable {
    private String domain,link;
    private CopyOnWriteArrayList imagelist;
    private Thread thread;


    public Crawler(String domain, String link, CopyOnWriteArrayList imageList,int id){
        System.out.println("crawler number "+id+" created");
         this.domain=domain;
         this.link=link;
         this.imagelist=imageList;

         thread=new Thread(this);
         thread.start();

    }

    @Override
    public void run() {
        try {
            getImagesFromUrl(domain,link,imagelist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getImagesFromUrl(String domain, String link,CopyOnWriteArrayList imagesList) throws IOException {
        Elements images;

            //Connection connection = Jsoup.connect(link).header("Accept-Language", "en").userAgent("Mozilla");
        	Connection connection = Jsoup.connect(link).header("Accept-Language", "en");
        
            Document document = connection.ignoreContentType(true).get();

            images = document.head().select("link[href~=.*\\.(ico|png)]");
            images.addAll(document.head().select("meta[itemprop=image]"));
            images.addAll(document.select("img[src]"));

            for (Element e : images) {
                imagesList.add(e.absUrl("src"));
            }
    }

       public Thread getThread(){
        return thread;
       }

}