package com.xpert.persistence.query;

import com.xpert.core.Person;

/**
 *
 * @author Ayslan
 */
public class TestQueryBuilder {

    public static void printQueryString(Restrictions restrictions) {
        QueryBuilder builder = new QueryBuilder(null);
        builder.from(Person.class);
        builder.add(restrictions);
        System.out.println(builder.getQueryString());
        System.out.println(builder.getQueryParameters());
    }

    public static void main(String[] args) {


        //Caso 1
        //FROM class WHERE  nome = 'MARIA' OR nome = 'JOSE' OR status = true
        Restrictions restrictions = new Restrictions();

        //solução 1
        restrictions.equals("nome", "MARIA")
                .or()
                .equals("nome", "JOSE")
                .or()
                .equals("status", true);


        printQueryString(restrictions);


        //Caso 2
        //FROM class WHERE  (nome = 'MARIA' AND status = true) OR (code = '123') 
        restrictions = new Restrictions();

        //em cadeia
        restrictions.startGroup()
                    .equals("nome", "MARIA").equals("status", true)
                    .endGroup()
                    .or()
                    .equals("code", "123");

        printQueryString(restrictions);


        //Caso 3
        //FROM class WHERE  (nome = 'MARIA' OR nome = 'JOSE') AND (code = '123' OR code = '321')

        //em cadeia
        restrictions = new Restrictions();
        restrictions
                .startGroup()
                    .equals("nome", "MARIA").or().equals("nome", "JOSE")
                .endGroup()
                .startGroup()
                    .equals("code", "123").or().equals("code", "321")
                .endGroup()
                .isNotNull("teste");

        printQueryString(restrictions);
        

        //Caso 4
        //FROM class WHERE  ((nome = 'MARIA' OR nome = 'JOSE') AND (cidade = 'TERESINA' OR cidade = 'BRASILIA')) AND (code = '123' OR code = '321')
        //em cadeia

        restrictions = new Restrictions();

        restrictions.startGroup()
                        .startGroup()
                            .equals("nome", "MARIA").or().equals("nome", "JOSE")
                        .endGroup()
                        .startGroup()
                            .equals("cidade", "TERESINA").or().equals("cidade", "BRASILIA")
                        .endGroup()
                    .endGroup()
                    .startGroup()
                        .equals("code", "123").or().equals("code", "321")
                    .endGroup();
        
         printQueryString(restrictions);
         
          //Caso 5
        //FROM class WHERE  nome = 'MARIA' AND ativo = true AND status IN(?)
        restrictions = new Restrictions();

        //em cadeia
        restrictions
                .equals("nome", "MARIA")
                .equals("nome", "MARIA")
                .in("status", true);

        printQueryString(restrictions);


    }
}
