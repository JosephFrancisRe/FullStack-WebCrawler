# FullStack WebCrawler
This repository contains all of the code I wrote for a hackathon event. The objective was to create a webapplication that instantiates a multithreaded webcrawler to scan websites and extract their images.

------------------------------------------------

<b>Back-End Features / Considerations</b>
- Multithreaded deployment of image scraping objects that process images on separate webpages at the same time.
- Threads are randomly put to sleep to make the WebCrawler more friendly to the destination web-server.
- While viewing the DOM for a URL, hyperlink elements are extracted so long as the link shares the same domain name as the source URL.
- When visiting a new webpage between 3 and 5 scrolling actions (of random pixel lengths) are taken to "simulate human behavior." Doing so is a basic attempt reducing the effectiveness of the destination webserver's anti-botting detection systems.
- Logging actions document key backend processing moments and error-handling messages both on the command-line and in a persistent text file called "WebCralerLogger.Log". All logging messages include a time-stamp at the beginning.
- All newly created methods include proper JavaDoc commenting to generate API documentation from the Java Source Code. This documentation generator allows for IDEs to extract those method descriptions for easy code-review and method reuse.
- The images were stored in a threadsafe arraylist of type "CopyOnWriteArrayList<Element>." This was done to prevent concurrency issues when various threads were attempting to access that shared memory space.
- The URLs of previously visited webpages were stored in a HashSet as sets are used when order does not matter and duplicates are irrelevant. Sets have constant time additions, deletions, and access to/of elements. Futhermore, any time this data structure needed to be emptied I instantiated a new object and assigned it to the old one rather than calling the .clear() method. The reason for this is that the .clear() method as it is interpreted by the Java compiler, iteratively accesses all elements of the set by calling on a HashMap delete operation. If the set has a large number of elements, the .clear() operation is substantially slower than simply assigning the value to a newly instantiated HashSet object.
- Updated pom.xml for dependencies required for org.seleniumhq.selenium and io.github.bonigarcia as this was how I simulated human behaviors for the bot. This invoked transitive dependencies which ultimately created dependency conflicts for jetty, so I had to view the "mvn depency:tree" to identify the conflict and manually resolve the issue.

<b>Front-End Features / Considerations</b>
- index.html has been redesigned and references /css/styles.css to mirror the look of a basic website.
- Bootstrap is used as well as percentage-based css attributes to create a dynamic webpage that resizes with changes in resolution and viewport.
- The navigation bar uses a scrolling animator javascript function which has 2 major features:
	1) navigating to sections of the HTML via the top-right corner hyperlinks does not append "#SECTION-NAME" to the end of the URL, and
	2) the webpage does not instantly jump to the anchor put rather gradually animates moving there over a 50 millisecond time period.
- In addition to submit and reset buttons, the form for beginning the web-crawling process has an "I'm feeling lucky" button which randomizes the selection of one of the websites that I regularly used to validate the bot was working.
- Scraped images are displayed as either an image or a logo on index.html. Logos are assumed to be an image that satisfies any of the following criteria: 1) contains the word "logo" in its URI, 2) contains the term "favicon" in its URI, or 3) has the extension .svg.

------------------------------------------------

The webcrawler was written in Java, Javascript, HTML, and CSS using Maven.

<b>How to run this repository:</b>

1) Install Apache Maven onto your machine
2) Download the repository
3) Unzip the package
4) Using a terminal, navigate to the download location on your harddrive
5) Type the following into your terminal:
		> mvn package
		> mvn clean
		> mvn clean test package jetty:run