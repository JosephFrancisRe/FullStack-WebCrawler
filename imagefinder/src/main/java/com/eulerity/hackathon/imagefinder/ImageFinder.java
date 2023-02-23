package com.eulerity.hackathon.imagefinder;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Objects;
import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The class ImageFinder is a multi-purpose utility class: Firstly, it facilitates the communication between the
 * client and server by creating Servlet operations to handle HTTP requests. Secondly, it interprets a URL's content
 * in order to crawl from an origin URL through all of the possible hyperlinks of the same domain. Thirdly, for
 * every unique webpage identified, the class instantiates a threaded object to scrape images that will ultimately
 * be classified as either an image or a logo by the updatelist.js file and displayed on index.html.
 */
@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;
	protected static final Gson GSON = new GsonBuilder().create();
	private static final Logger LOGGER = WebCrawlerLogger.init();
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
	 * ImageScraper object is created to scrape all images from the webpage's programming interface
	 * known as a Document Object Model (DOM). Once those images are scraped for all unique
	 * hyperlinks with the same domain name as the original URL, a Json file is written to
	 * inform the client's web browser of each image's URI so the ImageScraper's main page can be
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
		LOGGER.log(Level.INFO, timeStamp + ": Got request of: " + path + " with query param: " + url);
		ArrayList<ImageScraper> imageScrapers = new ArrayList<>();

		if (handleTestImages(url, resp)) { return; }

		try {
			domainName = extractDomain(url);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE, timeStamp + ": An exception was thrown as the URI was not parsable for " + url +". Msg: ", e.getMessage());
		}

		visitedWebpages = new HashSet<>();
		imageList = new CopyOnWriteArrayList<>();
		crawl(url);
		deployImageScrapers(visitedWebpages, imageScrapers, imageList);
		resp.getWriter().print(GSON.toJson(imageList));
		LOGGER.log(Level.INFO, timeStamp + ": Servlet response printed containing the images scraped from " + url + ".");
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
		LOGGER.log(Level.INFO, timeStamp + ": Processing a delete operation.");
		visitedWebpages = new HashSet<>();
		imageList = new CopyOnWriteArrayList<>();
		LOGGER.log(Level.INFO, timeStamp + ": Completed a delete operation.");
	}

	/**
	 * Crawls all hyperlinks in a URL's document while their domains match. This method is
	 * semi-friendly for the target website in that the crawling actions are delayed by a
	 * random duration of time between 1 and 2.5 seconds. This random delay both reduces
	 * the strain that the ImageFinder's crawling process places on the target webserver,
	 * while also reducing the chances that the bot is detected and therefore decreases the
	 * likelihood of the local network being banned from communicating with the target
	 * webserver in the future. Furthermore, the delay on webserver requests is also
	 * accompanied by random webpage scrolling in order to make bot detection more challenging.
	 * 
	 * @param	url						: A String value representing the desired domain's full URL
	 * @throws	IOException				: Signals a failed or interrupted input/output operation
	 * @throws	InterruptedException	: Signals that the thread was interrupted while sleeping
	 */
	public void crawl(String url) {
		if (!visitedWebpages.contains(url)) {
			try {
                visitedWebpages.add(url);
				Document document = Jsoup.connect(url).get();
				Elements adjacentWebpages = document.select("a[href*=https://" + domainName + "]");
				
				for (Element webpage : adjacentWebpages) {
					simulateHumanScrolling(url);
					crawl(webpage.attr("abs:href"));
				}
			} catch (IOException|InterruptedException e) {
				LOGGER.log(Level.SEVERE, timeStamp + ": An IO exception was thrown when scraping " + url + ". Msg: ", e.getMessage());
			}
		}
	}

	/**
	 * This method randomizes between 3 and 5 time intervals for random scrolling actions to occur on the target URL.
	 * Doing so mitigates the risk of the bot being IP banned for behaving in ways that are identifiably non-human
	 * (i.e., actions such as instantaneous web surfing which would be likely to get classified as a bot program via
	 * the target URL's machine learning classification models).
	 * 
	 * @param	url						: A String value representing the desired domain's full URL
	 * @throws	InterruptedException	: Signals that the thread was interrupted while sleeping
	 */
	public static void simulateHumanScrolling(String url) throws InterruptedException {
		WebDriverManager.chromedriver().setup();
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get(url);

		Random rand = new Random();
		int numOfActions = rand.nextInt(3) + 3;
		boolean scrollDown = true;

		TimeUnit.MILLISECONDS.sleep(getRandomBoundedLong(100L, 200L));
		for (int i = 0; i < numOfActions; i++) {
			JavascriptExecutor jse = (JavascriptExecutor) driver;
			int scrollDistance = rand.nextInt(400);
			if (!scrollDown) {
				scrollDistance *= -1;
			}
			jse.executeScript("window.scrollBy(0," + scrollDistance + ")");
			TimeUnit.MILLISECONDS.sleep(getRandomBoundedLong(200L, 500L));
			scrollDown = !scrollDown;
		}
	}

	/**
	 * Returns a random number as a long between two numbers.
	 * 
	 * @param 	leftLimit	: A long value representing the lower of two bounds for a random value
	 * @param	rightLimit	: A long value representing the higher of two bounds for a random value
	 * @return 	randomLong	: A long value that random falls between the two limit parameters
	 */
	public static long getRandomBoundedLong(long leftLimit, long rightLimit) {
		return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
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
			LOGGER.log(Level.INFO, timeStamp + ": Parsed a non-null hostname. Attempting to extract the domain name.");
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
			LOGGER.log(Level.INFO, timeStamp + ": Attempting to write a servlet response containing the test images.");
			resp.getWriter().print(GSON.toJson(testImages));
			LOGGER.log(Level.INFO, timeStamp + ": Servlet response printed containing the test images.");
			return true;
		}
		return false;
	}

	/**
	 * Creates and deploys objects on threads to scrape images for each webpage that was crawled to from the original URL.
	 * 
	 * @param 	visitedWebpages	: A HashSet containing every unique webpage accessible from the source URL that shares the domain name
	 * @param 	imageScrapers	: An arraylist to hold the ImageScraper objects that will eventually scrape images
	 * @param	imageList		: A threadsafe arraylist to be populated with images scraped by the ImageScraper objects
	 */
	public static void deployImageScrapers(HashSet<String> visitedWebpages, ArrayList<ImageScraper> imageScrapers, CopyOnWriteArrayList<Element> imageList) {
		int id = 1;

		for (String webpage : visitedWebpages) {
			imageScrapers.add(new ImageScraper(webpage, imageList, id++, LOGGER));
		}

		for (ImageScraper imageScraper : imageScrapers) {
			try {
				imageScraper.getThread().join();
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, timeStamp + ": An exception was thrown as an issue arose during the threading process. Msg: ", e.getMessage());
			}
		}
	}
}