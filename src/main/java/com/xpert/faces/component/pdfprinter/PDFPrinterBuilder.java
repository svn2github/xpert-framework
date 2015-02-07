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

    public static boolean DEBUG = true;
    private static final String EMPTY_HTML = "<html><head></head><body></body></html>";
    private static final Logger logger = Logger.getLogger(PDFPrinterBuilder.class.getName());

    /**
     * Get base URI of application, the pattern is : scheme + server name+ port,
     * example: http://180.1.1.10:8080
     *
     * @param context
     * @return
     */
    public static String getBaseURI(FacesContext context) {
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    /**
     * Return a byte representation of a PDF file, based on a HTML String. The
     * conversion is made using framework flying-saucer. Default page
     * orientation is "portrait"
     *
     * @param context
     * @param html
     * @return
     * @throws DocumentException
     * @throws IOException
     */
    public static byte[] createPDF(FacesContext context, String html) throws DocumentException, IOException {
        return createPDF(context, html, PageOrientation.PORTRAIT);
    }

    /**
     * Return a byte representation of a PDF file, based on a HTML String. The
     * conversion is made using framework flying-saucer.
     *
     * @param context
     * @param html
     * @param pageOrientation
     * @return
     * @throws DocumentException
     * @throws IOException
     */
    public static byte[] createPDF(FacesContext context, String html, PageOrientation pageOrientation) throws DocumentException, IOException {

        if (html == null || html.trim().isEmpty()) {
            html = EMPTY_HTML;
        }

        long inicio = System.currentTimeMillis();

        String content = HtmlNormalizer.normalize(html, getBaseURI(context), pageOrientation);

        long fim = System.currentTimeMillis();

        if (DEBUG) {
            logger.log(Level.INFO, "HTML normalized {0}ms", (fim - inicio));
        }

        inicio = System.currentTimeMillis();
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

        fim = System.currentTimeMillis();

        if (DEBUG) {
            logger.log(Level.INFO, "PDF created in {0}ms", (fim - inicio));
        }

        return baos.toByteArray();
    }

}
