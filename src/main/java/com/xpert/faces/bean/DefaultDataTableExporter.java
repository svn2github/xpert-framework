package com.xpert.faces.bean;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.xpert.faces.utils.FacesUtils;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Ayslan
 */
public class DefaultDataTableExporter {

    public static final String LOGO_PATH = "/images/logo.png";

    public void preProcessorPDF(Object document) throws IOException,
            BadElementException, DocumentException {
        if (document != null) {
            String logoPath = FacesUtils.getRealPath(LOGO_PATH);
            if(!new File(logoPath).exists()){
                return;
            }
            Document pdf = (Document) document;
            pdf.open();
            Image image = Image.getInstance(logoPath);
            image.scalePercent(50F);
            pdf.add(image);
        }
    }

    public void postProcessorPDF(Object document) throws IOException, BadElementException, DocumentException {
    }

    public void preProcessorExcel(Object document) throws IOException, BadElementException, DocumentException {
    }

    public void postProcessorExcel(Object document) throws IOException, BadElementException, DocumentException {
    }

}
