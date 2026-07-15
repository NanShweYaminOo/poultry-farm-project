package com.poultry.broiler_farming_system.service.report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Renders a Thymeleaf template to PDF, writing directly to the caller's
 * {@link OutputStream} (see {@code ReportController}'s
 * {@code StreamingResponseBody} usage) instead of building a {@code byte[]}
 * in memory first -- for a report with many rows, that's the difference
 * between holding roughly one page's worth of PDF bytes in the JVM heap at a
 * time versus the whole finished file. This only avoids buffering the
 * *output*, though: like every PDF layout engine (iText, PDFBox,
 * wkhtmltopdf, a browser's own print-to-PDF), openhtmltopdf still has to
 * hold the full HTML DOM and render tree in memory to paginate correctly --
 * no library avoids that for an arbitrarily large document. For this app's
 * report sizes (one farmer's history, one admin period) that DOM is trivial;
 * it would only become a real constraint again at a genuinely huge row count
 * (tens of thousands+), which would call for pagination at the query level
 * (date-range chunking) rather than a single mega-PDF regardless of library.
 *
 * <p><b>Why openhtmltopdf over iText / OpenPDF / Apache PDFBox directly:</b>
 * <ul>
 *   <li><b>iText 7</b> has the best-engineered layout/typography pipeline of
 *       the bunch, but its free tier is AGPLv3 -- every consumer of this app
 *       would need to accept AGPL's copyleft (or the project buys a
 *       commercial license). Full complex-script shaping (the Myanmar
 *       requirement below) additionally needs iText's separate paid
 *       pdfCalligraph add-on even under a commercial license.</li>
 *   <li><b>OpenPDF</b> (LGPL/MPL, the pre-AGPL iText 4/5 fork) is free and
 *       embeds TTF/OTF fonts, but its API is low-level, imperative
 *       "draw this string at this x/y" -- authoring and maintaining a
 *       multi-section financial report with tables that way is far more
 *       error-prone than HTML/CSS, and it has no complex-script shaping at
 *       all (same Myanmar problem as everything below).</li>
 *   <li><b>Apache PDFBox</b> directly is even lower-level than OpenPDF --
 *       you position every glyph yourself. Not a realistic authoring layer
 *       for a report with this much structure.</li>
 *   <li><b>openhtmltopdf</b> (the actively-maintained Flying Saucer fork,
 *       used here) lets the report be authored as ordinary Thymeleaf
 *       HTML/CSS -- tables, headers, page-break rules, all declarative and
 *       easy to iterate on -- under an LGPL-family license with no AGPL/
 *       commercial-license tradeoff. It uses Apache PDFBox as its actual PDF
 *       *output* writer (hence the {@code openhtmltopdf-pdfbox} artifact),
 *       just with a proper CSS box/layout model on top instead of raw
 *       drawing calls.</li>
 * </ul>
 *
 * <p><b>Myanmar script caveat -- read before shipping Myanmar body text for
 * real:</b> openhtmltopdf's text layout does not perform OpenType
 * complex-script shaping (glyph reordering/substitution). Myanmar script
 * requires that for essentially any real sentence, not just rare edge
 * cases -- e.g. the vowel sign for "e" is stored in Unicode *after* its
 * consonant but must be *rendered before* it; medial consonants stack
 * vertically. Embedding a Myanmar font via {@code @font-face} here (see
 * {@link #registerMyanmarFont}) gives correct *glyphs* but in the wrong
 * *visual order/position* without shaping -- this is not specific to
 * openhtmltopdf, no pure-Java PDF library ships full Myanmar shaping
 * out of the box (iText 7's free tier has the identical gap; only its paid
 * pdfCalligraph add-on solves it). The two credible fixes when this becomes
 * a real requirement: (a) pre-shape Myanmar text runs through a proper
 * shaper (HarfBuzz, via a small native/WASM binding) before they ever reach
 * this service's HTML, or (b) swap this service's PDF step for a
 * headless-Chromium print-to-PDF call (e.g. Playwright for Java) against the
 * exact same Thymeleaf HTML -- Chromium ships HarfBuzz and shapes Myanmar
 * correctly today. Neither is implemented here; report labels/numbers in
 * this codebase's report templates are kept in Latin/ASCII for that reason,
 * with Myanmar reserved for values that are short enough (names, single
 * words) to often read acceptably even unshaped -- that's a stopgap, not a
 * fix, and should be called out to end users if this ships as-is.
 */
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    public static final String MYANMAR_FONT_FAMILY = "Noto Sans Myanmar";

    private final TemplateEngine templateEngine;

    // Not bundled with this repo -- binary font files aren't something a
    // code change can supply. Download Noto Sans Myanmar (OFL license,
    // fonts.google.com/noto/specimen/Noto+Sans+Myanmar) and place the
    // Regular .ttf at src/main/resources/fonts/NotoSansMyanmar-Regular.ttf,
    // or point this at a different classpath location.
    @Value("${app.reports.myanmar-font-path:fonts/NotoSansMyanmar-Regular.ttf}")
    private String myanmarFontPath;

    public void render(String templateName, Map<String, Object> model, OutputStream outputStream) {
        Context context = new Context();
        context.setVariables(model);
        String html = templateEngine.process(templateName, context);

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        registerMyanmarFont(builder);
        builder.toStream(outputStream);

        try {
            builder.run();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to render PDF report '" + templateName + "'.", ex);
        }
    }

    private void registerMyanmarFont(PdfRendererBuilder builder) {
        ClassPathResource fontResource = new ClassPathResource(myanmarFontPath);
        if (!fontResource.exists()) {
            throw new IllegalStateException(
                    "Myanmar font not found on the classpath at '" + myanmarFontPath + "'. Download Noto Sans "
                            + "Myanmar (OFL license, fonts.google.com) and place it at "
                            + "src/main/resources/" + myanmarFontPath + ", or override app.reports.myanmar-font-path.");
        }
        builder.useFont(() -> openFontStream(fontResource), MYANMAR_FONT_FAMILY);
    }

    private InputStream openFontStream(ClassPathResource fontResource) {
        try {
            return fontResource.getInputStream();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
