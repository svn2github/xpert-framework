package com.xpert.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;

public class CollectionsUtils {

    @SuppressWarnings("unchecked")
    public static void orderAsc(List list, String ordenacao) {
        ArrayList colecao = new ArrayList();
        colecao.add(new BeanComparator(ordenacao));
        ComparatorChain multiSort = new ComparatorChain(colecao);
        Collections.sort(list, multiSort);
    }

    @SuppressWarnings("unchecked")
    public static void orderDesc(List list, String ordenacao) {
        ArrayList colecao = new ArrayList();
        colecao.add(new BeanComparator(ordenacao));
        ComparatorChain multiSort = new ComparatorChain(colecao);
        multiSort.setReverseSort(0);
        Collections.sort(list, multiSort);
    }


}
