package com.xpert.faces.component.pdfprinter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.swing.NaiveUserAgent;

/**
 *
 * @author ayslan
 */
public class CustomUserAgentCallback extends NaiveUserAgent {

    private static final Logger logger = Logger.getLogger(CustomUserAgentCallback.class.getName());

    private static final Map<String, byte[]> CACHE = new HashMap<String, byte[]>();

    @Override
    public CSSResource getCSSResource(String uri) {
        byte[] resource = CACHE.get(uri);
        try {
            if (resource == null || resource.length == 0) {
                if(uri.startsWith("https://")){
                    uri = uri.replace("https://", "http://");
                }
                InputStream inputStream = resolveAndOpenStream(uri);
                if (inputStream != null) {
                    resource = IOUtils.toByteArray(inputStream);
                    CACHE.put(uri, resource);
                }
            }
            if (resource != null && resource.length > 0) {
                return new CSSResource(new ByteArrayInputStream(resource));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
