package com.yplay.search;

import com.yplay.search.exceptions.NoResultsException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchTask {

    private static final String BASE_URL = "https://www.youtube.com/results?sp=EgIQAQ%253D%253D&q=";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    public static List<MediaObject> fetchSearchResults(String keyword) throws NoResultsException,
            IOException {
        List<MediaObject> searchResults = new ArrayList<>();

        String url = BASE_URL + URLEncoder.encode(keyword, "UTF-8") + "&gl=US";

        Document source = Jsoup.connect(url).userAgent(USER_AGENT).get();

        if (source.select("div.search-message").size() != 0) {
            throw new NoResultsException("The query hasn't produced any results.");
        }

        Elements videos = source.select("div.yt-lockup-video");
        for (Element video : videos) {
            String id = video.attr("data-context-item-id");
            String title = video.select("a").attr("title");
            String duration = video.select("span").first().text();
            String views;
            try {
                views = video.select("ul.yt-lockup-meta-info").select("li").get(1).text()
                        .split(" ")[0];
            } catch (IndexOutOfBoundsException e) { // sometimes an ad might appear and fuck everything
                continue;
            }

            searchResults.add(new MediaObject(id, title, convertDuration(duration), views));
        }

        return searchResults;
    }

    private static int convertDuration(String durationString) {
        int duration = 0;

        String[] splitString = durationString.split(":");

        int magnitude = 1;
        for (int i = splitString.length - 1; i >= 0; i--) {
            duration += magnitude * Integer.parseInt(splitString[i]);
            magnitude *= 60;
        }

        return duration;
    }

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        try {
            List<MediaObject> objects = fetchSearchResults("Michael+Jackson");

            for (MediaObject object : objects) {
                System.out.println(object.getTitle() + " | " + object.getId() + " | " +
                        object.getDuration() + " | " + object.getViews());
            }
        } catch (IOException | NoResultsException e) {
            e.printStackTrace();
        }
    }

}
