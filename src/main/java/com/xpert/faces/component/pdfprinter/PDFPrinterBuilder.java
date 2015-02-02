package com.xpert.faces.component.pdfprinter;

import com.itextpdf.text.DocumentException;
import com.xpert.faces.utils.FacesUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 *
 * @author Ayslan
 */
public class PDFPrinterBuilder {

    public static boolean DEBUG = false;
    private static final String EMPTY_HTML = "<html><head></head><body></body></html>";
    private static final Logger logger = Logger.getLogger(PDFPrinterBuilder.class.getName());

    /**
     * Normalize the document, setting absolute link on images, css, etc. Set
     * display block on SVG.
     *
     * @param document
     * @param pageOrientation
     */
    public static void normalize(Document document, PageOrientation pageOrientation) {
        document.select("script").remove();
        Elements elements = document.select("link,a");
        for (Element element : elements) {
            element.attr("href", element.absUrl("href"));
        }
        elements = document.select("script,img");
        for (Element element : elements) {
            element.attr("src", element.absUrl("src"));
        }
        //normalize svg, add display block
        elements = document.select("svg");
        for (Element element : elements) {
            String style = element.attr("style");
            String width = element.attr("width");
            String height = element.attr("height");
            if (style == null) {
                style = "";
            }
            if (style.endsWith(";")) {
                style = style + ";";
            }

            style = style + " display: block;";
            if (width != null && !width.isEmpty()) {
                style = style + " width: " + width + "px;";
            }
            if (height != null && !height.isEmpty()) {
                style = style + " height: " + height + "px;";
            }
            element.attr("style", style);
        }

        //put all styles in <head>
        Elements headSeletor = document.select("head");
        if (headSeletor != null && !headSeletor.isEmpty()) {
            //page orientation
            if (pageOrientation != null && pageOrientation.equals(PageOrientation.LANDSCAPE)) {
                headSeletor.append("<style>").append("@page {size: landscape}").append("</style>");
            }
            elements = document.select("style");
            elements.remove();
            headSeletor.append(elements.outerHtml());
        }

    }

    public static String getBaseURI(FacesContext context) {
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    public static byte[] createPDF(FacesContext context, String html) throws DocumentException, IOException {
        return createPDF(context, html, PageOrientation.PORTRAIT);
    }

    public static byte[] createPDF(FacesContext context, String html, PageOrientation pageOrientation) throws DocumentException, IOException {

        if (html == null || html.trim().isEmpty()) {
            html = EMPTY_HTML;
        }

        long inicio = System.currentTimeMillis();

        //TODO get baseuri
        Document document = Jsoup.parse(html, getBaseURI(context), Parser.xmlParser());

        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        normalize(document, pageOrientation);
        String content = document.html();

//        System.out.println(content);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //create renderer
        ITextRenderer iTextRenderer = new ITextRenderer();
        iTextRenderer.setDocumentFromString(content);

        //to convert svg
        ChainingReplacedElementFactory chainingReplacedElementFactory = new ChainingReplacedElementFactory();
        chainingReplacedElementFactory.addReplacedElementFactory(new SVGReplacedElementFactory(iTextRenderer.getOutputDevice()));
        SharedContext sharedContext = iTextRenderer.getSharedContext();

        sharedContext.setReplacedElementFactory(chainingReplacedElementFactory);
        iTextRenderer.layout();

        //write
        iTextRenderer.createPDF(baos);
        baos.flush();
        baos.close();

        long fim = System.currentTimeMillis();

        if (DEBUG) {
            logger.log(Level.INFO, "PDF created in {0}ms", (fim - inicio));
        }

        return baos.toByteArray();
    }

}
