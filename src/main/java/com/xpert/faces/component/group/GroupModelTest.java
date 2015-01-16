/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.faces.component.group;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ayslan
 */
public class GroupModelTest {

    public static void main(String[] args) {

        PersonTest personTest1 = new PersonTest("PESSOA 1", "111", "TERESINA");
        PersonTest personTest2 = new PersonTest("PESSOA 2", "222", "TERESINA");
        PersonTest personTest3 = new PersonTest("PESSOA 3", "333", "TERESINA");
        PersonTest personTest4 = new PersonTest("PESSOA 4", "444", "ALTOS");
        PersonTest personTest5 = new PersonTest("PESSOA 5", "555", "ALTOS");
        PersonTest personTest6 = new PersonTest("PESSOA 6", "666", "BARRAS");
        PersonTest personTest7 = new PersonTest("PESSOA 7", "777", null);
        PersonTest personTest8 = new PersonTest("PESSOA 8", "888", null);

        List<PersonTest> list = new ArrayList<PersonTest>();
        list.add(personTest3);
        list.add(personTest6);
        list.add(personTest4);
        list.add(personTest5);
        list.add(personTest8);
        list.add(personTest7);
        list.add(personTest1);
        list.add(personTest2);

        GroupModel<String, PersonTest> groupModel = new GroupModel("city", list);
        groupModel.setSortOrder(GroupSortOrder.ASC);
        groupModel.setItemSortField("name");
        groupModel.groupItens();
        printItens(groupModel);

    }

    public static void printItens(GroupModel<String, PersonTest> groupModel) {

        System.out.println("total agrupamentos: " + groupModel.getItensSize());
        System.out.println("total itens: " + groupModel.getSize());

        for (GroupModelItem<String, PersonTest> item : groupModel.getItens()) {
            System.out.println(item.getIndex() + " - " + item.getKey() + ", first: " + item.isFirst() + ", last: " + item.isLast());
            for (PersonTest personTest : item.getValue()) {
                System.out.println(personTest.getName() + " - " + personTest.getCpf());
            }
        }
    }

}
