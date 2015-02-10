package com.xpert.persistence.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

/**
 *
 * Sequence updater to Oracle database
 *
 * @author ayslan, arnaldo
 */
public class OracleSequenceUpdater extends SequenceUpdater {

    private final EntityManager entityManager;
    private DataSource dataSource;

    public OracleSequenceUpdater(DataSource dataSource, EntityManager entityManager) {
        this.dataSource = dataSource;
        this.entityManager = entityManager;
    }

    public OracleSequenceUpdater(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void changeCurrentValue(Connection connection, String sequenceName, Long maxId) {
        if (connection != null) {
            changeCurrentValueJdbc(connection, sequenceName, maxId);
        } else {
            changeCurrentValueJpa(sequenceName, maxId);
        }
    }

    public void changeCurrentValueJpa(String sequenceName, Long maxId) {

        if (maxId == null) {
            maxId = 1L;
        }

        String nextVal = getSelectNextVal(sequenceName);

        Query queryNextVal = entityManager.createNativeQuery(nextVal);
        Number nextValResult = (Number) queryNextVal.getSingleResult();

        String alterDefault = getAlterSequenceIncrementBy(sequenceName, 1);
        String alterMax = getAlterSequenceIncrementBy(sequenceName, (maxId - nextValResult.longValue()));

        if ((maxId - nextValResult.longValue()) != 0) {
            Query queryAlterMax = entityManager.createNativeQuery(alterMax);
            queryAlterMax.executeUpdate();
        }

        Query queryNext = entityManager.createNativeQuery(nextVal);
        queryNext.executeUpdate();

        Query queryAlterDefault = entityManager.createNativeQuery(alterDefault);
        queryAlterDefault.executeUpdate();
    }

    public void changeCurrentValueJdbc(Connection connection, String sequenceName, Long maxId) {

        if (maxId == null) {
            maxId = 1L;
        }

        String nextVal = getSelectNextVal(sequenceName);
        try {

            PreparedStatement statementNextVal = connection.prepareStatement(nextVal);
            statementNextVal.execute();
            statementNextVal.getResultSet().next();
            long nextValResult = statementNextVal.getResultSet().getLong(1);

            String alterDefault = getAlterSequenceIncrementBy(sequenceName, 1);
            String alterMax = getAlterSequenceIncrementBy(sequenceName, (maxId - nextValResult));

            if ((maxId - nextValResult) != 0) {
                PreparedStatement statementAlterMax = connection.prepareStatement(alterMax);
                statementAlterMax.execute();
            }

            PreparedStatement statementNext = connection.prepareStatement(nextVal);
            statementNext.execute();

            PreparedStatement statementAlterDefault = connection.prepareStatement(alterDefault);
            statementAlterDefault.execute();

        } catch (SQLException ex) {
            throw new RuntimeException("Error updating sequence " + sequenceName, ex);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void createSequence(final Connection connection, final String schema, final String sequenceName, final int initialValue, final int allocationSize) {
        if (connection != null) {
            createSequenceJdbc(connection, schema, sequenceName, initialValue, allocationSize);
        } else {
            createSequenceJpa(schema, sequenceName, initialValue, allocationSize);
        }
    }

    public void createSequenceJdbc(final Connection connection, final String schema, final String sequenceName, final int initialValue, final int allocationSize) {

        try {
            PreparedStatement statement = connection.prepareStatement(getQuerySelectSequence());

            statement.setString(1, sequenceName.trim().toUpperCase());
            statement.execute();
            statement.getResultSet().next();
            Long result = statement.getResultSet().getLong(1);

            if (result == null || result.intValue() <= 0) {
                String nameWithSchema = sequenceName;
                if (schema != null && !schema.isEmpty()) {
                    nameWithSchema = nameWithSchema + "." + sequenceName;
                }
                String queryCreate = getQueryCreateSequence(nameWithSchema, initialValue, allocationSize);
                PreparedStatement statementCreate = connection.prepareStatement(queryCreate);
                statementCreate.execute();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error creating sequence " + sequenceName, ex);
        }

    }

    public void createSequenceJpa(final String schema, final String sequenceName, final int initialValue, final int allocationSize) {
        Query querySelect = entityManager.createNativeQuery(getQuerySelectSequence());
        querySelect.setParameter(1, sequenceName.trim().toUpperCase());
        Number result = (Number) querySelect.getSingleResult();

        if (result == null || result.intValue() <= 0) {
            String nameWithSchema = sequenceName;
            if (schema != null && !schema.isEmpty()) {
                nameWithSchema = nameWithSchema + "." + sequenceName;
            }
            String queryCreate = getQueryCreateSequence(nameWithSchema, initialValue, allocationSize);
            Query queryNext = entityManager.createNativeQuery(queryCreate);
            queryNext.executeUpdate();
        }
    }

    public String getAlterSequenceIncrementBy(String sequenceName, long incrementBy) {
        return "ALTER SEQUENCE " + sequenceName + " INCREMENT by " + incrementBy;
    }

    public String getSelectNextVal(String sequenceName) {
        return "SELECT " + sequenceName + ".NEXTVAL FROM dual";
    }

    public String getQuerySelectSequence() {
        return "SELECT COUNT(*) FROM user_sequences WHERE upper(sequence_name) = ? ";
    }

    public String getQueryCreateSequence(String sequenceName, int initialValue, int allocationSize) {
        return "CREATE SEQUENCE " + sequenceName + " INCREMENT BY " + allocationSize + " START WITH " + initialValue;
    }

}
