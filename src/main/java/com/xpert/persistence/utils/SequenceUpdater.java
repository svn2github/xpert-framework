package com.xpert.persistence.utils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.sql.DataSource;

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

    private static final Logger logger = Logger.getLogger(SequenceUpdater.class.getName());

    public abstract DataSource getDataSource();

    public abstract EntityManager getEntityManager();

    public void createSequences() {
        if (getEntityManager() == null) {
            throw new IllegalArgumentException("Provide a EntityManager to create sequences");
        }

        final List<Class> classes = EntityUtils.getMappedEntities(getEntityManager());
        Connection connection = null;
        try {
            if (getDataSource() != null) {
                connection = getDataSource().getConnection();
            }
            SequenceGenerator sequenceGenerator = null;
            for (Class clazz : classes) {
                String schema = null;
                sequenceGenerator = getSequenceGenerator(clazz);
                if (sequenceGenerator != null) {
                    logger.log(Level.INFO, "Mapping sequence {0}", sequenceGenerator.sequenceName());
                    try {
                        createSequence(connection, schema, sequenceGenerator.sequenceName(), sequenceGenerator.initialValue(), sequenceGenerator.allocationSize());
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Erro in sequence " + sequenceGenerator.sequenceName(), ex);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
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
        Connection connection = null;
        try {
            if (getDataSource() != null) {
                connection = getDataSource().getConnection();
            }
            for (Class clazz : classes) {
                String schema = null;
                sequenceGenerator = getSequenceGenerator(clazz);
                if (sequenceGenerator != null) {
                    try {
                        logger.log(Level.INFO, "Mapping sequence {0}", sequenceGenerator.sequenceName());
                        Long maxId = getMaxId(sequenceGenerator.sequenceName(), clazz);
                        if (schema != null && !schema.isEmpty()) {
                            changeCurrentValue(connection, schema + "." + sequenceGenerator.sequenceName(), maxId);
                        } else {
                            changeCurrentValue(connection, sequenceGenerator.sequenceName(), maxId);
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Erro in sequence " + sequenceGenerator.sequenceName(), ex);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro updating sequence " + sequenceGenerator.sequenceName(), ex);
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
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

    public abstract void createSequence(Connection connection, String schema, String sequenceName, int initialValue, int allocationSize);

    public abstract void changeCurrentValue(Connection connection, String sequenceName, Long maxId);

}
