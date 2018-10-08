package com.wire.bots.recording;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.annotation.Nullable;
import java.io.IOException;

class UrlUtil {
    @Nullable
    static String extractPagePreview(String url) throws IOException {
        Connection con = Jsoup.connect(url);
        Document doc = con.get();

        Elements metaOgImage = doc.select("meta[property=og:image]");
        if (metaOgImage != null) {
            return metaOgImage.attr("content");
        }
        return null;
    }

    static String extractPageTitle(String url) throws IOException {
        Connection con = Jsoup.connect(url);
        Document doc = con.get();

        Elements title = doc.select("meta[property=og:title]");
        if (title != null) {
            return title.attr("content");
        }
        return doc.title();
    }
}
