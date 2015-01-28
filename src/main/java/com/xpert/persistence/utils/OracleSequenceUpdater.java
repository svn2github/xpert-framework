package com.xpert.persistence.utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * Sequence updater to Oracle database
 *
 * @author ayslan, arnaldo
 */
public class OracleSequenceUpdater extends SequenceUpdater {

    private final EntityManager entityManager;

    public OracleSequenceUpdater(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void changeCurrentValue(String sequenceName, Long maxId) {

        if (maxId == null) {
            maxId = 1L;
        }

        String nextVal = "SELECT " + sequenceName + ".NEXTVAL FROM dual";

        Query queryNextVal = entityManager.createNativeQuery(nextVal);
        Number nextValResult = (Number) queryNextVal.getSingleResult();

        String alterDefault = "ALTER SEQUENCE " + sequenceName + " INCREMENT by 1";
        String alterMax = "ALTER SEQUENCE " + sequenceName + " INCREMENT by " + (maxId - nextValResult.longValue());

        if ((maxId - nextValResult.longValue()) != 0) {
            Query queryAlterMax = entityManager.createNativeQuery(alterMax);
            queryAlterMax.executeUpdate();
        }

        Query queryNext = entityManager.createNativeQuery(nextVal);
        queryNext.executeUpdate();

        Query queryAlterDefault = entityManager.createNativeQuery(alterDefault);
        queryAlterDefault.executeUpdate();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public void createSequence(String schema, String sequenceName, int initialValue, int allocationSize) {

        //verify if sequence exists
        String selectSequence = "SELECT COUNT(*) FROM user_sequences WHERE upper(sequence_name) = :name ";
        Query querySelect = entityManager.createNativeQuery(selectSequence);
        querySelect.setParameter("name", sequenceName.toUpperCase());
        Number result = (Number) querySelect.getSingleResult();

        if (result == null || result.intValue() <= 0) {
            String nameWithSchema = sequenceName;
            if (schema != null && !schema.isEmpty()) {
                nameWithSchema = nameWithSchema + "." + sequenceName;
            }
            String nextVal = "CREATE SEQUENCE " + nameWithSchema + " INCREMENT BY " + allocationSize + " START WITH " + initialValue;
            Query queryNext = entityManager.createNativeQuery(nextVal);
            queryNext.executeUpdate();
        }

    }

}
