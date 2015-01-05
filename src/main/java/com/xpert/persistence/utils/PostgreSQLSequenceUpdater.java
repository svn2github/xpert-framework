package com.xpert.persistence.utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Sequence update to PostgreSQL
 *
 * @author ayslan, arnaldo
 */
public class PostgreSQLSequenceUpdater extends SequenceUpdater {

    private final EntityManager entityManager;

    public PostgreSQLSequenceUpdater(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void changeCurrentValue(String sequenceName, Long maxId) {
        //select setval('sequencename' ,1);
        String setValQueryString = null;
        if (maxId == null) {
            //false indicate that sequence is not called (maxId == null the table is empty)
            setValQueryString = "SELECT SETVAL('" + sequenceName + "', 1, false)";
        } else {
            setValQueryString = "SELECT SETVAL('" + sequenceName + "', " + maxId + ")";
        }
        Query querySetVal = entityManager.createNativeQuery(setValQueryString);
        querySetVal.getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

}
