package com.wire.backups.exports.utils;

import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.util.XRLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PdfGenerator {
    private static final String[] fonts = new String[]{
            "NotoEmoji-Regular.ttf",
            "Arial-Unicode.ttf",
            "Arial-Bold.ttf"
    };
    private static final String FONT_FAMILY = "Arial";
    private static final PdfRendererBuilder builder;

    static {
        XRLog.setLoggingEnabled(false);
        builder = new PdfRendererBuilder().useSVGDrawer(new BatikSVGDrawer());
        for (String font : fonts) {
            builder.useFont(new FSSupplier<InputStream>() {
                @Override
                public InputStream supply() {
                    return getClass().getClassLoader().getResourceAsStream(String.format("fonts/%s", font));
                }
            }, FONT_FAMILY);
        }
    }

    public static File save(String filename, String html, String baseUrl) throws Exception {
        File file = new File(filename);
        try (OutputStream out = new FileOutputStream(filename)) {
            build(html, baseUrl, out);
        }
        return file;
    }

    private static void build(String html, String baseUrl, OutputStream out) throws Exception {
        builder.useUriResolver((bu, uri) -> {
                    if (uri.contains(":"))
                        return uri.contains(".") ? uri : null;
                    return bu + uri;
                })
                .withHtmlContent(html, baseUrl)
                .useFastMode()
                .toStream(out)
                .run();
    }
}
