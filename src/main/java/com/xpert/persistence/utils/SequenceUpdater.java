package com.xpert.persistence.utils;

import java.lang.reflect.Field;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * Abstract class to update sequences based on max value in current Entity, the
 * sequence name is get with reflections searching for annotation
 *
 * @SequenceGenerator
 *
 * @author ayslan, arnaldo
 */
public abstract class SequenceUpdater {

    public abstract EntityManager getEntityManager();

    public void createSequences() {
        if (getEntityManager() == null) {
            throw new IllegalArgumentException("EntityManager must not null");
        }

        List<Class> classes = EntityUtils.getMappedEntities(getEntityManager());
        SequenceGenerator sequenceGenerator = null;

        for (Class clazz : classes) {
            String schema = null;
            sequenceGenerator = getSequenceGenerator(clazz);
            if (sequenceGenerator != null) {
                createSequence(schema, sequenceGenerator.sequenceName(), sequenceGenerator.initialValue(), sequenceGenerator.allocationSize());
            }
        }

    }

    private SequenceGenerator getSequenceGenerator(Class clazz) {
        SequenceGenerator sequenceGenerator = null;
        sequenceGenerator = (SequenceGenerator) clazz.getAnnotation(SequenceGenerator.class);
        //try to get in fields
        if (sequenceGenerator == null) {
            Field[] fields = clazz.getDeclaredFields();
            Field.setAccessible(fields, true);
            for (Field field : fields) {
                sequenceGenerator = (SequenceGenerator) field.getAnnotation(SequenceGenerator.class);
                if (sequenceGenerator != null) {
                    break;
                }
            }
        }
        return sequenceGenerator;
    }

    /**
     * Update all entity sequences
     */
    public void updateSequences() {

        if (getEntityManager() == null) {
            throw new IllegalArgumentException("EntityManager must not null");
        }

        List<Class> classes = EntityUtils.getMappedEntities(getEntityManager());
        SequenceGenerator sequenceGenerator = null;

        for (Class clazz : classes) {
            String schema = null;
            sequenceGenerator = getSequenceGenerator(clazz);
            if (sequenceGenerator != null) {
                Long maxId = getMaxId(sequenceGenerator.sequenceName(), clazz);
                if (schema != null && !schema.isEmpty()) {
                    changeCurrentValue(schema + "." + sequenceGenerator.sequenceName(), maxId);
                } else {
                    changeCurrentValue(sequenceGenerator.sequenceName(), maxId);
                }
            }
        }
    }

    public Long getMaxId(String sequenceName, Class clazz) {
        StringBuilder sqlMax = new StringBuilder();
        sqlMax.append("SELECT MAX(").append(EntityUtils.getIdFieldName(clazz)).append(") FROM ").append(clazz.getName());
        Query query = getEntityManager().createQuery(sqlMax.toString());
        Long maxId = null;
        try {
            maxId = (Long) query.getSingleResult();
        } catch (NoResultException ex) {
        }
        return maxId;
    }

    public abstract void createSequence(String schema, String sequenceName, int initialValue, int allocationSize);

    public abstract void changeCurrentValue(String sequenceName, Long maxId);

}
