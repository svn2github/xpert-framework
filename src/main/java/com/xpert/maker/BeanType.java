package com.xpert.maker;

/**
 *
 * @author Ayslan
 */
public enum BeanType {

    MANAGED_BEAN("managed-bean.ftl", "java", false),
    BUSINESS_OBJECT("business-object.ftl", "java", false),
    DAO("dao.ftl", "java", false),
    DAO_IMPL("dao-impl.ftl", "java", false),
    //xhtml
    VIEW_LIST("view-list.ftl", "xhtml", true, true),
    VIEW_DETAIL("view-detail.ftl", "xhtml", true, true),
    VIEW_FORM_CREATE("view-form-create.ftl", "xhtml", true, true),
    VIEW_MENU("view-menu.ftl", "xhtml", true),
    VIEW_CREATE("view-create.ftl", "xhtml", true);
    private String template;
    private String extension;
    private boolean view;
    private boolean primefacesVersionDependend;

    private BeanType(String template, String extension, boolean view) {
        this.template = template;
        this.extension = extension;
        this.view = view;
    }

    private BeanType(String template, String extension, boolean view, boolean primefacesVersionDependend) {
        this.template = template;
        this.extension = extension;
        this.view = view;
        this.primefacesVersionDependend = primefacesVersionDependend;
    }
    
    

    public String getTemplate() {
        return template;
    }

    public String getExtension() {
        return extension;
    }

    public boolean isView() {
        return view;
    }

    public boolean isPrimefacesVersionDependend() {
        return primefacesVersionDependend;
    }
    
    
}
