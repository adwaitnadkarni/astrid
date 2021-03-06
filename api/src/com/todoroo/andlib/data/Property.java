/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.andlib.data;

import static com.todoroo.andlib.sql.SqlConstants.COMMA;
import static com.todoroo.andlib.sql.SqlConstants.LEFT_PARENTHESIS;
import static com.todoroo.andlib.sql.SqlConstants.RIGHT_PARENTHESIS;
import static com.todoroo.andlib.sql.SqlConstants.SPACE;

import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.Operator;

/**
 * Property represents a typed column in a database.
 *
 * Within a given database row, the parameter may not exist, in which case the
 * value is null, it may be of an incorrect type, in which case an exception is
 * thrown, or the correct type, in which case the value is returned.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 * @param <TYPE>
 *            a database supported type, such as String or Integer
 */
@SuppressWarnings("nls")
public abstract class Property<TYPE> extends Field implements Cloneable {

    // --- implementation

    /** The database table name this property */
    public final Table table;

    /** The database column name for this property */
    public final String name;

    /** Can this field be null? */
    public boolean nullable = false;

    /**
     * Create a property by table and column name. Uses the default property
     * expression which is derived from default table name
     */
    protected Property(Table table, String columnName) {
        this(table, columnName, (table == null) ? (columnName) : (table.name() + "." + columnName));
    }

    /**
     * Create a property by table and column name. Uses the default property
     * expression which is derived from default table name
     */
    protected Property(Table table, String columnName, boolean nullable) {
        this(table, columnName, (table == null) ? (columnName) : (table.name() + "." + columnName));
        this.nullable = nullable;
    }

    /**
     * Create a property by table and column name, manually specifying an
     * expression to use in SQL
     */
    protected Property(Table table, String columnName, String expression) {
        super(expression);
        this.table = table;
        this.name = columnName;
    }

    /**
     * Accept a visitor
     */
    abstract public <RETURN, PARAMETER> RETURN accept(
            PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data);

    /**
     * Return a clone of this property
     */
    @Override
    public Property<TYPE> clone() {
        try {
            return (Property<TYPE>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    // --- helper classes and interfaces

    /**
     * Visitor interface for property classes
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public interface PropertyVisitor<RETURN, PARAMETER> {
        public RETURN visitInteger(Property<Integer> property, PARAMETER data);

        public RETURN visitLong(Property<Long> property, PARAMETER data);

        public RETURN visitDouble(Property<Double> property, PARAMETER data);

        public RETURN visitString(Property<String> property, PARAMETER data);
    }

    // --- children

    /**
     * Integer property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class IntegerProperty extends Property<Integer> {

        public IntegerProperty(Table table, String name) {
            super(table, name);
        }

        public IntegerProperty(Table table, String name, boolean nullable) {
            super(table, name, nullable);
        }

        protected IntegerProperty(Table table, String name, String expression) {
            super(table, name, expression);
        }

        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitInteger(this, data);
        }
    }

    /**
     * String property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class StringProperty extends Property<String> {

        public StringProperty(Table table, String name) {
            super(table, name);
        }

        public StringProperty(Table table, String name, boolean nullable) {
            super(table, name, nullable);
        }

        protected StringProperty(Table table, String name, String expression) {
            super(table, name, expression);
        }

        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitString(this, data);
        }

        @Override
        public StringProperty as(String newAlias) {
            return (StringProperty) super.as(newAlias);
        }

        public Criterion in(final String[] value) {
            final Field field = this;
            return new Criterion(Operator.in) {

                @Override
                protected void populate(StringBuilder sb) {
                    sb.append(field).append(SPACE).append(Operator.in).append(SPACE).append(LEFT_PARENTHESIS).append(SPACE);
                    for (String s : value) {
                        sb.append("\"").append(s.toString()).append("\"").append(COMMA);
                    }
                    sb.deleteCharAt(sb.length() - 1).append(RIGHT_PARENTHESIS);
                }
            };
        }
    }

    /**
     * Double property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class DoubleProperty extends Property<Double> {

        public DoubleProperty(Table table, String name) {
            super(table, name);
        }

        public DoubleProperty(Table table, String name, boolean nullable) {
            super(table, name, nullable);
        }

        protected DoubleProperty(Table table, String name, String expression) {
            super(table, name, expression);
        }


        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitDouble(this, data);
        }
    }

    /**
     * Long property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class LongProperty extends Property<Long> {

        public LongProperty(Table table, String name) {
            super(table, name);
        }

        public LongProperty(Table table, String name, boolean nullable) {
            super(table, name, nullable);
        }

        protected LongProperty(Table table, String name, String expression) {
            super(table, name, expression);
        }

        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitLong(this, data);
        }

        @Override
        public LongProperty as(String newAlias) {
            return (LongProperty) super.as(newAlias);
        }
    }

    public String getColumnName() {
        if (hasAlias())
            return alias;
        return name;
    }

    // --- pseudo-properties

    /** Runs a SQL function and returns the result as a string */
    public static class StringFunctionProperty extends StringProperty {
        public StringFunctionProperty(String function, String columnName) {
            super(null, columnName, function);
            alias = columnName;
        }
    }

    /** Runs a SQL function and returns the result as a string */
    public static class IntegerFunctionProperty extends IntegerProperty {
        public IntegerFunctionProperty(String function, String columnName) {
            super(null, columnName, function);
            alias = columnName;
        }
    }

    /** Counting in aggregated tables. Returns the result of COUNT(1) */
    public static final class CountProperty extends IntegerFunctionProperty {
        public CountProperty() {
            super("COUNT(1)", "count");
        }
    }

}
