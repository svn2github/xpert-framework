package com.xpert.persistence.query;

/**
 *
 * @author Ayslan
 */
public class JoinBuilder {

    private StringBuilder builder;

    public JoinBuilder() {
        builder = new StringBuilder();
    }

    public JoinBuilder leftJoin(String join) {
        this.builder.append("LEFT JOIN ").append(join).append(" ");
        return this;
    }

    public JoinBuilder innerJoin(String join) {
        this.builder.append("INNER JOIN ").append(join).append(" ");
        return this;
    }

    public JoinBuilder join(String join) {
        this.builder.append("JOIN ").append(join).append(" ");
        return this;
    }

    public JoinBuilder leftJoinFetch(String join) {
        this.builder.append("LEFT JOIN FETCH ").append(join).append(" ");
        return this;
    }

    public JoinBuilder innerJoinFetch(String join) {
        this.builder.append("INNER JOIN FETCH ").append(join).append(" ");
        return this;
    }

    public JoinBuilder joinFetch(String join) {
        this.builder.append("JOIN FETCH ").append(join).append(" ");
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
