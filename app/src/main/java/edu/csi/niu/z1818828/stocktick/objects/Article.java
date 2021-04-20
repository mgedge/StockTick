/************************************************************************
 * 	File Name: Article.java     					    				*
 * 																		*
 *  Developer: Matthew Gedge											*
 *   																	*
 *    Purpose: This java class creates the news objects required to     *
 *    display news articles.
 *																		*
 * *********************************************************************/
package edu.csi.niu.z1818828.stocktick.objects;

public class Article {
    String title;
    String source;
    String url;
    String imageUrl;

    /**
     * Create an article object which has a title, news source, and two article related links
     *
     * @param title    - string for the title of the article
     * @param source   - string for the source of the article
     * @param url      - string for the url to the article
     * @param imageUrl - string for the url of the image in article
     */
    public Article(String title, String source, String url, String imageUrl) {
        this.title = title;
        this.source = source;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
