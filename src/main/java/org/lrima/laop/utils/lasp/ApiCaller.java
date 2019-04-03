package org.lrima.laop.utils.lasp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Give some usefull methods to classes that need to call APIs
 * @author Clement Bisaillon
 */
public class ApiCaller {
	
	/**
	 * Get the string content of the response of an API endpoint
	 * @param url - the url to request
	 * @param method - the method to request
	 * @return the content of the response from the API endpoint
	 */
	protected static String getContentFromURL(String url, String method) throws IOException {
		//setup url
		URL endPoint = new URL(url);
		HttpURLConnection con = (HttpURLConnection) endPoint.openConnection();
		con.setRequestMethod(method);
			
		//Get the data
		String data = getData(con);
		con.disconnect();
			
		return data;
	}
	
	/**
	 * Get the data from HttpURLConnection in String format
	 * @param connection the connection object
	 * @return a String containing the response data of the connection
	 */
	private static String getData(HttpURLConnection connection) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			
			return content.toString();
		}catch(Exception e) {
			System.err.println("Error while getting the data from LASP !");
			return "";
		}
		
		
	}
}
