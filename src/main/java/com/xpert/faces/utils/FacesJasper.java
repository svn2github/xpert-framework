package com.xpert.faces.utils;

import com.xpert.jasper.JRBeanCollectionDataSource;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 *
 * @author Ayslan
 */
public class FacesJasper {

    private static final Logger logger = Logger.getLogger(FacesJasper.class.getName());

    public static void createJasperReport(List dataSource, Map parameters, String path, String fileName) {
        createJasperReport(dataSource, parameters, path, fileName, null);
    }

    public static void createJasperReport(List dataSource, Map parameters, String path, String fileName, EntityManager entityManager) {

        try {
            String layout = FacesContext.getCurrentInstance().getExternalContext().getRealPath(path);
            JasperPrint jasperPrint;
            JRBeanCollectionDataSource jRBeanCollectionDataSource = new JRBeanCollectionDataSource(dataSource, entityManager);
            if (jRBeanCollectionDataSource.getData() == null || jRBeanCollectionDataSource.getData().isEmpty()) {
                JREmptyDataSource jREmptyDataSource = new JREmptyDataSource();
                jasperPrint = JasperFillManager.fillReport(layout, parameters, jREmptyDataSource);
            } else {
                jasperPrint = JasperFillManager.fillReport(layout, parameters, jRBeanCollectionDataSource);
            }
            FacesUtils.download(JasperExportManager.exportReportToPdf(jasperPrint), "application/pdf", fileName.endsWith(".pdf") ? fileName : fileName + ".pdf");
        } catch (JRException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
}
