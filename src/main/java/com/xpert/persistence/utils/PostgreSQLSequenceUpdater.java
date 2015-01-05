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
        String setValQueryString = "SELECT SETVAL('" + sequenceName + "', " + maxId + ")";
        Query querySetVal = entityManager.createNativeQuery(setValQueryString);
        querySetVal.executeUpdate();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

}
