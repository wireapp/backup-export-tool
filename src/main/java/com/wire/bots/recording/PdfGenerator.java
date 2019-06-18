package com.wire.bots.recording;

import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;

import java.io.*;

public class PdfGenerator {
    private static final String[] fonts = new String[]{
            "NotoEmoji-Regular.ttf",
            "Arial-Unicode.ttf",
            "Arial-Bold.ttf"
    };
    private static final String FONT_FAMILY = "Arial";
    private static final PdfRendererBuilder builder;

    static {
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

    public static byte[] convert(String html) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            builder.withHtmlContent(html, "")
                    .toStream(out)
                    .run();
            return out.toByteArray();
        }
    }

    static File save(String filename, String html) throws Exception {
        File file = new File(filename);
        try (OutputStream out = new FileOutputStream(filename)) {
            builder.withHtmlContent(html, "")
                    .toStream(out)
                    .run();
        }
        return file;
    }
}
