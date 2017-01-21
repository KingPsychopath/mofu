package com.edasaki.misakachan.scanlator;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.edasaki.misakachan.utils.MStringUtils;
import com.edasaki.misakachan.utils.logging.M;

public class BakaUpdateSearcher {
    //    private static final String PREFIX = "http://www.google.com/search?q=";
    //    private static final String SUFFIX = "+site:mangaupdates.com";
    private static final String PREFIX = "https://www.mangaupdates.com/series.html?stype=title&orderby=rating&perpage=100&page=";
    private static final String SEARCH = "&search=";

    //    private static final String SELECTOR_REGEX = ".*\\Qurl?q=\\E.*\\Qmangaupdates.com/series.html\\E.*";
    //    private static final String SELECTOR = "a[href~=" + SELECTOR_REGEX + "]";
    private static final String SELECTOR_REGEX = ".*\\Qmangaupdates.com/series.html?id=\\E.*";
    private static final String SELECTOR = "a[href~=" + SELECTOR_REGEX + "]";

    private static final String LINK_REGEX = "\\Qhttp\\Es?\\Q://\\E(www)?.?\\Qmangaupdates.com\\E.*";

    private static final String CHARSET = "UTF-8";

    private static final String USER_AGENT = "misakachan (+http://misakachan.net)";
    private static final String REFERRER = "https://www.mangaupdates.com/";

    private static final String GROUP_SELECTOR_REGEX = ".*\\Qmangaupdates.com/groups.html?id=\\E.*";
    private static final String GROUP_SELECTOR = "a[href~=" + GROUP_SELECTOR_REGEX + "]";

    protected ScanGroup search(String title) {
        title = title.trim();
        Document doc;
        long start = System.currentTimeMillis();
        try {
            double bestSimilarity = 0;
            String bestTitle = null;
            String bestURL = null;
            int page = 1;
            HashMap<String, String> mapContainedTitleToURL = new HashMap<String, String>();
            do {
                final String connectionURL = PREFIX + page + SEARCH + URLEncoder.encode(title, CHARSET);
                doc = Jsoup.connect(connectionURL).userAgent(USER_AGENT).referrer(REFERRER).get();
                if (doc.html().contains("There are no") || doc.html().contains("make your query more restrictive.")) // search complete
                    break;
                Elements links = doc.select(SELECTOR);
                for (Element link : links) {
                    String linkName = link.text().trim();
                    String linkHref = link.absUrl("href");
                    if (!linkHref.matches(LINK_REGEX))
                        continue;
                    double sim = MStringUtils.similarityMaxContains(linkName, title);
                    if (sim >= bestSimilarity) {
                        bestSimilarity = sim;
                        bestURL = linkHref;
                        bestTitle = linkName;
                        if (sim == 1.0) {
                            mapContainedTitleToURL.put(bestTitle, bestURL);
                        }
                    }
                    //check for alphanumeric chars only (mostly to avoid weird dash issues like tomo-chan vs tomochan)
                    sim = MStringUtils.similarityMaxContainsAlphanumeric(linkName, title);
                    if (sim >= bestSimilarity) {
                        bestSimilarity = sim;
                        bestURL = linkHref;
                        bestTitle = linkName;
                        if (sim == 1.0) {
                            mapContainedTitleToURL.put(bestTitle, bestURL);
                        }
                    }
                }
                page++;
            } while (true);
            if (mapContainedTitleToURL.size() > 1) {
                bestSimilarity = 0;
                for (Entry<String, String> e : mapContainedTitleToURL.entrySet()) {
                    double sim = MStringUtils.similarity(e.getKey(), title);
                    if (sim > bestSimilarity) {
                        bestSimilarity = sim;
                        bestURL = e.getValue();
                        bestTitle = e.getKey();
                    }
                }
            }
            M.debug("Best result for \"" + title + "\": " + bestTitle + " [" + bestURL + "] with a similarity of " + bestSimilarity);
            if (bestURL != null) {
                Document detailPage = Jsoup.connect(bestURL).userAgent(USER_AGENT).referrer(REFERRER).get();
                Elements sCats = detailPage.select(GROUP_SELECTOR);
                HashMap<Integer, ScanGroup> groups = new HashMap<Integer, ScanGroup>();
                for (Element e : sCats) {
                    try {
                        String href = e.absUrl("href");
                        int id = Integer.parseInt(href.substring(href.indexOf("?id=") + "?id=".length()).trim());
                        if (groups.containsKey(id))
                            continue;
                        String name = e.text();
                        ScanGroup sg = new ScanGroup(id, name);
                        groups.put(id, sg);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                System.out.println(groups);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished search() in " + (System.currentTimeMillis() - start) + "ms");
        return null;
    }

}