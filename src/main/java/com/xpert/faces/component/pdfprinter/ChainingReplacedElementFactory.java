/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.faces.component.pdfprinter;

import org.xhtmlrenderer.extend.ReplacedElementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

/**
 *
 * @author Ayslan
 */
public class ChainingReplacedElementFactory implements ReplacedElementFactory {
    private List<ReplacedElementFactory> replacedElementFactories 
            = new ArrayList<ReplacedElementFactory>();

    public void addReplacedElementFactory(ReplacedElementFactory replacedElementFactory) {
        replacedElementFactories.add(0, replacedElementFactory);
    }

    @Override
    public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, 
            UserAgentCallback uac, int cssWidth, int cssHeight) {
        for(ReplacedElementFactory replacedElementFactory : replacedElementFactories) {
            ReplacedElement element = replacedElementFactory
                    .createReplacedElement(c, box, uac, cssWidth, cssHeight);
            if(element != null) {
                return element;
            }
        }
        return null;
    }

    @Override
    public void reset() {
        for(ReplacedElementFactory replacedElementFactory : replacedElementFactories) {
            replacedElementFactory.reset();
        }
    }

    @Override
    public void remove(Element e) {
        for(ReplacedElementFactory replacedElementFactory : replacedElementFactories) {
            replacedElementFactory.remove(e);
        }
    }

    @Override
    public void setFormSubmissionListener(FormSubmissionListener listener) {
        for(ReplacedElementFactory replacedElementFactory : replacedElementFactories) {
            replacedElementFactory.setFormSubmissionListener(listener);
        }
    }
}