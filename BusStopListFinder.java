/*
BusStopListFinder.java
Date: 26 Jan 2019
Student Name: Timothy Michael Biggar
Quarter: Winter 2019
Class: CS 320
Instructor: Sara Farag

Note: This is my first quarter taking a CS class here at BC, so I am unsure how
to style my comments, and am guessing as to what information is required at the
top of my code: what is present is based on the information that was commonly
required for submission at Foothill College.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Class BusStopListFinder:
 * 
 * This class is a program that allows a user to find destination communities
 * starting with a character that is input by the user, as well as listing the 
 * Route IDs for routes linked to that community. After that, the user is
 * prompted for a Route ID.
 * 
 * public functions:
 * - createURLConnection(String url): makes a new URLConnection from url
 * - createMatcher(String regEx, String text): makes a new Matcher object that
 *     applies the regular expression "regEx" to the target text field "text".
 */
public class BusStopListFinder {
	// ------ URL and SSL connection info ------
	private static final String[] REQUEST_PROPERTY = {"user-Agent",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, " + 
					"like Gecko) Chrome/23.0.1271.95 Safari/537.11"};
	
	private static final String MAIN_URL = 
			"https://www.communitytransit.org/busservice/schedules/";
	
	
	// ------ Regular Expressions ------
	private static final String communityRegEx =
			"(?s:<h3>(CHAR[^<]+)(.*?)(<hr|<p>))";
	// CHAR will be replaced with a single character input by the user. 
	// Group 1 is the Community Destination matching with CHAR, group 2 is the
	// search field for routIDRegEx.
	// (?s: ... ) activates DOTALL flag for the section, also note that the
	// "? ... :" after an opening bracket makes the group non-capturing, so it
	// does not contribute to the number of (capturing) groups.
	
	private static final String routeIdRegEx = ">(\\d{3}(/\\d{3})?|Swift)";
	// Group 1 is a Route ID, group two is an optional chunk for double routes,
	// (e.g. routes 532 and 535 are grouped together as 532/535.
	// The "Swift" route has the only non-numeric Route ID.
	
	private static final String routeDestRegEx =
			"(?s:<h2>Weekday<small>([^<]+)(.*?)</thead>)";
	// Group 1 is the "Destination", group 2 is the search field for
	// routeStopRegEx.
	// DOTALL flag is activated, non-capturing group like communityRegEx.
	
	private static final String routeStopRegEx = 
	//		">(\\d+)</strong>\\s</span>\\s<p>([^<]*)</p>";
	">(\\d+)</strong>\\s*</span>\\s*<p>([^<]*)";
	// Group 1 is the stop number, group 2 is the stop name.
	
	
	// ------ User Input ------
	private static final Scanner inputStream = new Scanner(System.in);
	
	
	/*
	 * main(String[] args): Main program, see class description.
	 */
	public static void main(String[] args) throws Exception {
		URLConnection busSchedules = createURLConnection(MAIN_URL);
		
		BufferedReader in1 = new BufferedReader(new 
				InputStreamReader(busSchedules.getInputStream()));
		
		String inputLine = "";
		String text = "";
		
		while ((inputLine = in1.readLine()) != null) {
				text += inputLine + "\n";
		}
		
		in1.close();
		
		String input = promptInput(
				"Please enter a letter that your destinations start with: ");
		
		// Find all destination communities and routes associated with them
		Matcher matcherCom = createMatcher(
				communityRegEx.replace("CHAR", input.toUpperCase()), text);
		
		if (matcherCom.find()) {
			matcherCom.reset();
			while (matcherCom.find()) {
				// print the destination community
				System.out.println("Destination: " + matcherCom.group(1));
				
				Matcher matcherRoute = createMatcher(routeIdRegEx, 
						matcherCom.group(2));
				
				while (matcherRoute.find()) {
					// print all Route IDs for that community
					System.out.println("Route ID: " + matcherRoute.group(1));
				}
			 
				System.out.println("+++++++++++++++++++++++++++++++++++");
			}
			
			// prompt user for a Route ID
			input = promptInput("Please enter a route ID as a string...\n" +
					"(Routes like 532/535 should be entered exactly): ");
			input = input.replace("swift", "Swift");
			System.out.println("");
			
			// if there is a match, for the user's Route ID, then a URL exists.
			if (createMatcher("href[^>]+>" + input + "( \\*)?<", text).find()) {
				String routeLink = MAIN_URL + "route/" + input.replace("/", "-");
				// URLs for double routes has - instead of /
				// (e.g. 532/535 URL ender is 532-535)
				
				System.out.println("The link for your route is: \n" + routeLink +
						"\n");
			
				URLConnection routeURL = createURLConnection(routeLink);
			
				BufferedReader in2 = new BufferedReader(new 
						InputStreamReader(routeURL.getInputStream()));
				
				String inputLine2 = "";
				String text2 = "";
				
				while ((inputLine2 = in2.readLine()) != null) {
					text2 += inputLine2 + "\n";
				}
			
				in2.close();
				
				Matcher matchDest = createMatcher(routeDestRegEx, text2);
				
				// get destination and stops
				while (matchDest.find()) {
					System.out.println("" + matchDest.group(1));
					
					Matcher matchStop = createMatcher(routeStopRegEx,
						matchDest.group(2));
				
					while (matchStop.find()) {
						System.out.println("Stop number: " + matchStop.group(1) +
								" is " + matchStop.group(2));
					}
					
					System.out.println("+++++++++++++++++++++++++++++++++++");
				}
			}
			else {
				// Invalid Route ID, so there is also no URL, and nothing to do.
				System.out.println("Route \"" + input + "\" does not exist");
			}
		}
		else {
			// No community starts with input (e.g. "c" will not match).
			System.out.print("No destination found starting with \"" + input + 
					"\"");
		}
		
		inputStream.close();
	}
	
	/*
	 * createURLConnection(String url): Helper function for streamlining the
	 * creation of new URLConnection objects:
	 * 
	 * Opens a URLConnection using the String url and returns it to the caller.
	 */
	public static URLConnection createURLConnection(String url) throws Exception {
		URLConnection myURL = new URL(url).openConnection();
		myURL.setRequestProperty(REQUEST_PROPERTY[0], REQUEST_PROPERTY[1]);
		
		return myURL;
	}
	
	/*
	 * createMatcher(String regEx, String text): Helper function for
	 * streamlining the creation of new Matcher objects:
	 * 
	 * Creates a Matcher object that uses the regular expression String regEX
	 * on the target text String text and returns it to the caller.
	 */
	public static Matcher createMatcher(String regEx, String text) {
		return Pattern.compile(regEx).matcher(text);
		// Pattern object is only being used to create a Matcher object
	}
	
	/*
	 * promptInput(String prompt): prints prompt to the console, returns the
	 * next line of Scanner inputStream.
	 */
	public static String promptInput(String prompt){
		System.out.print(prompt);
		return inputStream.nextLine();
	}
}

/*
------ Sample Output 1 ------
Please enter a letter that your destinations start with: B
Destination: Bellevue
Route ID: 532/535
+++++++++++++++++++++++++++++++++++
Destination: Bothell
Route ID: 105
Route ID: 106
Route ID: 120
Route ID: 435
Route ID: 532/535
+++++++++++++++++++++++++++++++++++
Destination: Brier
Route ID: 111
+++++++++++++++++++++++++++++++++++
Please enter a route ID as a string...
(Routes like 532/535 should be entered exactly): 111

The link for your route is: 
https://www.communitytransit.org/busservice/schedules/route/111

To Mountlake Terrace
Stop number: 1 is Brier Rd &amp; 228th Pl SW
Stop number: 2 is 228th St SW &amp; 48th Ave W
Stop number: 3 is Mountlake Terrace Transit Center
+++++++++++++++++++++++++++++++++++
To Brier
Stop number: 3 is Mountlake Terrace Transit Center Bay 2
Stop number: 2 is 228th St SW &amp; 48th Ave W
Stop number: 4 is 228th St SW &amp; 29th Ave W
+++++++++++++++++++++++++++++++++++

------ Sample Output 2 ------
Please enter a letter that your destinations start with: c
No destination found starting with "c"

------ Sample Output 3 ------
Please enter a letter that your destinations start with: d
Destination: Darrington
Route ID: 230
+++++++++++++++++++++++++++++++++++
Please enter a route ID as a string...
(Routes like 532/535 should be entered exactly): 1

Route "1" does not exist
 */
