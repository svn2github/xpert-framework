/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.faces.primefaces;

/**
 *
 * @author ayslan
 */
public enum PrimeFacesVersion {
    
    VERSION_3("Version 3", "primefaces3"), 
    VERSION_4("Version 4", "primefaces4"),
    /*
     version 5 is the same as version 4 in CRUD generation
    */
    VERSION_5("Version 5", "primefaces4");
    
    private final String description;
    private final String packageName;

    private PrimeFacesVersion(String description, String packageName) {
        this.description = description;
        this.packageName = packageName;
    }

    public String getDescription() {
        return description;
    }

 
    public String getPackageName() {
        return packageName;
    }

    
    
}
