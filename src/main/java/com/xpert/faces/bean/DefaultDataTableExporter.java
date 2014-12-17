package com.xpert.faces.bean;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.xpert.faces.utils.FacesUtils;
import java.io.File;
import java.io.IOException;

/**
 * Default Managed Bean to process export.
 * This bean add manipulates the Object in pre/post events in export.
 * 
 * @author Ayslan
 */
public class DefaultDataTableExporter {

    public static final String LOGO_PATH_PNG = "/images/logo.png";
    public static final String LOGO_PATH_JPG = "/images/logo.jpg";

    public void preProcessorPDF(Object document) throws IOException,
            BadElementException, DocumentException {
        if (document != null) {
            //try to get PNG
            String logoPath = FacesUtils.getRealPath(LOGO_PATH_PNG);
            if(!new File(logoPath).exists()){
                //try to get JPG
                logoPath = FacesUtils.getRealPath(LOGO_PATH_JPG);
                if(!new File(logoPath).exists()){
                    return;
                }
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
