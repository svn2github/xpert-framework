/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.faces.component.group;

/**
 *
 * @author Ayslan
 */
public class PersonTest {

    private String name;
    private String cpf;
    private String city;

    public PersonTest() {
    }

    
    
    public PersonTest(String name, String cpf, String city) {
        this.name = name;
        this.cpf = cpf;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}
