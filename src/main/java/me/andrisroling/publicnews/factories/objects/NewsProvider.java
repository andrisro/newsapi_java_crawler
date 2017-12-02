package me.andrisroling.publicnews.factories.objects;

/**
 * @author: Andris Roling - andrisroling.me
 * @description: -
 **/


public class NewsProvider {
    private int publisherID;
    private String publisherName;
    private String apiPath;

    public NewsProvider(int publisherID, String publisherName, String apiPath) {
        this.setPublisherID(publisherID);
        this.setPublisherName(publisherName);
        this.setApiPath(apiPath);
    }

    public int getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(int publisherID) {
        this.publisherID = publisherID;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }
}