package me.andrisroling.publicnews.factories;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author: Andris Roling - andrisroling.me
 * @description: -
 **/
public class HttpRequestFactory {
	/**
	 * Reads the Content of an HTTPS Page
	 * @param url
	 * @return Content as String
	 * @throws IOException
	 */
	public static String getContentOfHttpsPage(URL url) throws IOException {
		
		//Create Content String Builder
		StringBuilder contentStringBuilder = new StringBuilder();
		
		//Create Connection
		HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
		
		
		//Initialize Readers
		BufferedReader inputStreamBufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
		
		String lineOfInputStream; 
		
		/*
		 * Read Content of Page
		 */
		while((lineOfInputStream=inputStreamBufferedReader.readLine())!=null) {
			contentStringBuilder.append(lineOfInputStream);
		}
		
		return contentStringBuilder.toString();		
	}
}
