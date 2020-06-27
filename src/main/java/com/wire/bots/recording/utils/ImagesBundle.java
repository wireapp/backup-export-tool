package com.wire.bots.recording.utils;

import com.wire.bots.sdk.tools.Logger;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.servlets.assets.AssetServlet;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ImagesBundle extends AssetsBundle {
    public ImagesBundle(String resourcePath, String uriPath, String name) {
        super(resourcePath, uriPath, "index.htm", name);
    }

    @Override
    protected AssetServlet createServlet() {
        return new _AssetServlet(getResourcePath(), getUriPath(), getIndexFile(), StandardCharsets.UTF_8);
    }

    static class _AssetServlet extends AssetServlet {
        _AssetServlet(String resourcePath, String uriPath, @Nullable String indexFile, @Nullable Charset defaultCharset) {
            super(resourcePath, uriPath, indexFile, defaultCharset);
        }

        @Override
        protected URL getResourceUrl(String path) {
            Logger.debug("ImagesBundle: loading: %s", path);
            try {
                return new URL(String.format("file:/%s", path));
            } catch (MalformedURLException e) {
                //Logger.error(e.toString());
                return null;
            }
        }
    }
}
