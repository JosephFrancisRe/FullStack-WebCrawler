package com.eulerity.hackathon.imagefinder;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Objects;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;
	protected static final Gson GSON = new GsonBuilder().create();
	private static final Logger logger = Logger.getLogger(ImageFinder.class.getName());
	private static Date timeStamp = new Date();
	private static String domainName = "";

	private CopyOnWriteArrayList<Element> imageList = new CopyOnWriteArrayList<>();
	private HashSet<String> visitedWebpages = new HashSet<>();

	// This is just a test array
	public static final String[] testImages = {
		"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
		"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
		"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
		"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
	};
	
	/**
	 * The Servlet post operation for HTTP POST requests.
	 * 
	 * This method crawls a URL and scrapes unique hyperlinks. For every unique hyperlink found, an
	 * imageScraper object is created to scrape all images from the webpage's programming interface
	 * known as a Document Object Model (DOM). Once those images are scraped for all unique
	 * hyperlinks with the same domain name as the original URL, a Json file is written to
	 * inform the client's web browser of each image's URI so the imageScraper's main page can be
	 * populated.
	 * 
	 * @param	req					: The object containing the client's servlet request
	 * @param	resp				: The object containing the servlet's response to the client
	 * @throws	ServletException	: Signals a generalized servlet error per the web.xml config
	 * @throws	IOException 		: Signals a failed or interrupted input/output operation
	 */
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");
		logger.info(timeStamp + ": Got request of: " + path + " with query param: " + url);
		ArrayList<imageScraper> imageScrapers = new ArrayList<>();

		if (handleTestImages(url, resp)) { return; }

		try {
			domainName = extractDomain(url);
		} catch (URISyntaxException e) {
			logger.log(Level.SEVERE, timeStamp + ": An exception was thrown as the URI was not parsable for " + url +". Msg: ", e);
		}

		visitedWebpages = new HashSet<>();
		imageList = new CopyOnWriteArrayList<>();
		crawl(url);
		deployimageScrapers(visitedWebpages, imageScrapers, imageList);
		resp.getWriter().print(GSON.toJson(imageList));
		logger.info(timeStamp + ": Servlet response printed containing the images scraped from " + url + ".");
	}

	/**
	 * The Servlet delete operation for HTTP DELETE requests.
	 * 
	 * This method simply constructs new empty objects to replace the previous values contained in
	 * the variables visitedWebpages and imageList. Reassigning the variables to newly constructed
	 * empty objects rather than calling the delete operations for HashSets and ArrayLists is a
	 * faster operation in the average case scenario as there is no need to iterate through the
	 * data structures removing each element.
	 * 
	 * @param	req					: The object containing the client's servlet request
	 * @param	resp				: The object containing the servlet's response to the client
	 * @throws	ServletException	: Signals a generalized servlet error per the web.xml config
	 * @throws	IOException 		: Signals a failed or interrupted input/output operation
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info(timeStamp + ": Processing a delete operation.");
		visitedWebpages = new HashSet<>();
		imageList = new CopyOnWriteArrayList<>();
		logger.info(timeStamp + ": Completed a delete operation.");
	}

	/**
	 * Crawls all hyperlinks in a URL's document while their domains match.
	 * 
	 * @param	url			: A String value representing the desired domain's full URL
	 * @throws	IOException	: Signals a failed or interrupted input/output operation
	 */
	public void crawl(String url) {
		if (!visitedWebpages.contains(url)) {
			try {
                visitedWebpages.add(url);
				Document document = Jsoup.connect(url).get();
				Elements adjacentWebpages = document.select("a[href*=https://" + domainName + "]");
				
				for (Element webpage : adjacentWebpages) {
					crawl(webpage.attr("abs:href"));
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, timeStamp + ": An exception was thrown when scraping " + url + ". Msg: ", e);
			}
		}
	}

	/**
	 * Returns the domain name from a provided URL's hostname.
	 * 
	 * @param 	url					: A String value representing the desired domain's full URL
	 * @return 	domainName			: A String value representing the URL's domain name
	 * @throws 	URISyntaxException	: Indicates that the URL parameter was not parsable as a URI reference
	 */
	public static String extractDomain(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String hostName = uri.getHost();
		domainName = hostName;

		if (Objects.nonNull(hostName)) {	
			logger.info(timeStamp + ": Parsed a non-null hostname. Attempting to extract the domain name.");
			domainName = hostName.startsWith("www.") ? hostName.substring(4) : hostName;
			return domainName;
		}
		return domainName;
	}

	/**
	 * Process the test images if necessary, and returns whether the processing occurred.
	 * 
	 * @param 	url			: A String value representing the desired domain's full URL
	 * @param	resp		: The object containing the servlet's response to the client
	 * @return 	boolean		: Returns true if the requested URL is empty or null; otherwise, false
	 * @throws 	IOException	: Signals a failed or interrupted input/output operation
	 */
	public static boolean handleTestImages(String url, HttpServletResponse resp) throws IOException {
		if (url == null || url.isEmpty()) {
			logger.info(timeStamp + ": Attempting to write a servlet response containing the test images.");
			resp.getWriter().print(GSON.toJson(testImages));
			logger.info(timeStamp + ": Servlet response printed containing the test images.");
			return true;
		}
		return false;
	}

	/**
	 * Creates and deploys objects on threads to scrape images for each webpage that was crawled to from the original URL.
	 * 
	 * @param 	visitedWebpages	: A HashSet containing every unique webpage accessible from the source URL that shares the domain name
	 * @param 	imageScrapers	: An arraylist to hold the imageScraper objects that will eventually scrape images
	 * @param	imageList		: A threadsafe arraylist to be populated with images scraped by the imageScraper objects
	 */
	public static void deployimageScrapers(HashSet<String> visitedWebpages, ArrayList<imageScraper> imageScrapers, CopyOnWriteArrayList<Element> imageList) {
		int id = 1;

		for (String webpage : visitedWebpages) {
			imageScrapers.add(new imageScraper(webpage, imageList, id++));
		}

		for (imageScraper imageScraper : imageScrapers) {
			try {
				imageScraper.getThread().join();
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, timeStamp + ": An exception was thrown as an issue arose during the threading process. Msg: ", e);
			}
		}
	}
}