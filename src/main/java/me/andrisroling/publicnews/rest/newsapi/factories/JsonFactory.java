package me.andrisroling.publicnews.rest.newsapi.factories;

import me.andrisroling.publicnews.rest.newsapi.json.RootJsonResource;
import com.google.gson.Gson;

/**
 * @author: Andris Roling - andrisroling.me
 * @description: -
 **/
public class JsonFactory {
	/**
	 * Formats the JSON of the NewsAPI to Java Objects
	 * @param content
	 * @return
	 */
	public static RootJsonResource getJsonObjectsFromString(String content) {
		Gson gson = new Gson();
		RootJsonResource jsonResource = gson.fromJson(content, RootJsonResource.class);
		
		return jsonResource;
	}
}
