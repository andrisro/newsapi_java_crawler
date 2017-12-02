package me.andrisroling.publicnews.factories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import me.andrisroling.publicnews.factories.objects.NewsProvider;
import me.andrisroling.publicnews.rest.newsapi.factories.JsonFactory;
import me.andrisroling.publicnews.rest.newsapi.json.Article;
import me.andrisroling.publicnews.rest.newsapi.json.RootJsonResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author: Andris Roling - andrisroling.me
 * @description: -
 **/
public class UpdateSQLDataFactory {
	private static final Logger logger = LogManager.getLogger(UpdateSQLDataFactory.class);

	/**
	 * Executes an Update for the Database - Reads current Articles from the API, downloads and insert it in DB
	 * @param connection SQL Connection
	 * @param convertRows ConvertDates?
	 * @return Number of Inserted Articles
	 * @throws SQLException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static int doUpdate(Connection connection, boolean convertRows) throws SQLException, MalformedURLException, IOException {
		List<NewsProvider> newsProviderList = SQLFactory.getNewsProviders(connection);
		
		logger.trace("Found "+newsProviderList.size()+" News Providers in DB");
		int articlesInsertedSum = 0;
		
		for(NewsProvider newsProvider: newsProviderList) {
			logger.trace("Execute an Update for the News of "+newsProvider.getPublisherName()+" - "+newsProvider.getPublisherID());
			
			int articlesInserted = updatePublisherSQLTable(connection, newsProvider, false);
			articlesInsertedSum += articlesInserted;
		}

		logger.debug("Finished All Updates: Count "+articlesInsertedSum);
		
		if(convertRows) {
			SQLFactory.formatDates(connection);
		}
		
		return articlesInsertedSum;
	}

	/**
	 * Reads Content of Publisher from the API, inserts it into DB
	 * @param connection
	 * @param newsProvider
	 * @param convertRows
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static int updatePublisherSQLTable(Connection connection, NewsProvider newsProvider, boolean convertRows) throws MalformedURLException, IOException, SQLException {
		System.out.println("Execute an Update for Publisher "+newsProvider.getPublisherName()+" - "+newsProvider.getPublisherID());
		
		
		/*
		 * GET Content of REST Ressource
		 */
		logger.trace("Read API Path "+newsProvider.getApiPath());
		
		logger.trace("URL Length Before "+newsProvider.getApiPath().length() + " for "+newsProvider.getApiPath());
		
		URL url = new URL(newsProvider.getApiPath());
		
		logger.trace("URL Length After "+url.toString().length()+" - "+url.toString());
		
		String content = HttpRequestFactory.getContentOfHttpsPage(url);
		
		/*
		 * Convert to JSON
		 */
		
		RootJsonResource rootJsonResourceApi = JsonFactory.getJsonObjectsFromString(content);
		
		List<Article> articleList = rootJsonResourceApi.getArticles();
		
		logger.trace("Found "+articleList.size()+" Articles for Publisher "+newsProvider.getPublisherName()+" - "+newsProvider.getPublisherID());
		logger.trace("Execute an Update of the DB");
		
		int articlesInserted = SQLFactory.insertMultipleArticles(connection, newsProvider.getPublisherID(), articleList);
		
		logger.trace("Update for Publisher "+newsProvider.getPublisherName()+" - "+newsProvider.getPublisherID()+" finished");
		logger.trace("Inserted "+articlesInserted+" for the Provider "+newsProvider.getPublisherID());
		
		SQLFactory.setLastUpdated(connection);

		if(convertRows) {
			SQLFactory.formatDates(connection);
		}
		
		
		return articlesInserted;
	}
}
