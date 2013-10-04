package com.xpert.faces.primefaces;

/**
 *
 * @author ayslan
 */
public enum LazyCountType {

      /**
     * Always count data (Execute count even in paging event, but only when filters change).It's the default value on LazyDataModelImpl
     */
    ALWAYS,
    /**
     * Don't count data. Useful when it1s a data table for many records.
     */
    NONE,
    /**
     * Only count data once. Next paginations will get the first count.
     */
    ONLY_ONCE;
}
