package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;
	protected static final Gson GSON = new GsonBuilder().create();
	private CopyOnWriteArrayList imageList = new CopyOnWriteArrayList();

	private HashSet<String> visitedLinks=new HashSet<>();
	private String domain="";



	//This is just a test array
	public static final String[] testImages = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
  };


	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ArrayList<Crawler> webCrawlers=new ArrayList<>();
		String[] array;

		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");
		System.out.println("Got request of:" + path + " with query param:" + url);
		int id=0;

		//check if url is null or empty string
		if(Objects.nonNull(url) && !url.isEmpty()) {
			try {
				domain=getDomainName(url);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
            //clear link and image list from previous submission
			visitedLinks.clear();imageList.clear();
			//get all available link of the website
			getPageLinks(url);


			for(String link:visitedLinks){
				webCrawlers.add(new Crawler(domain,link,imageList,id));
				id++;
			}

			for(Crawler wc:webCrawlers){
				try {
					wc.getThread().join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			array=new String[imageList.size()];
			imageList.toArray(array);

		resp.getWriter().print(GSON.toJson(array));
		}else {
			resp.getWriter().print(GSON.toJson(testImages));
		}
	}


	public void getPageLinks(String URL) {
		//Check if you have already crawled the URLs
		if (!visitedLinks.contains(URL)) {
			try {
				// If not add it to the index
                visitedLinks.add(URL);
				// Fetch the HTML code
				Document document = Jsoup.connect(URL).get();
				// Parse the HTML to extract links to other URLs
				Elements linksOnPage = document.select("a[href*=https://"+domain+"]");
				// get all links For each extracted URL
				for (Element link : linksOnPage) {
					getPageLinks(link.attr("abs:href"));
				}
			} catch (IOException e) {
				System.err.println("For '" + URL + "': " + e.getMessage());
			}
		}
	}

	public static String getDomainName(String url) throws URISyntaxException {
		URI uri = new URI(url);
			String domain = uri.getHost();
			return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("clear");
		visitedLinks.clear();
		imageList.clear();
	}
}