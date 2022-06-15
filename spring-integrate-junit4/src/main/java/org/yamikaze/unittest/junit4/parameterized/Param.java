package org.yamikaze.unittest.junit4.parameterized;

import java.util.Objects;

/**
 * Display a single param in source file
 *
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-17 11:12
 */
public class Param {

    /**
     * Param's title, such as userId
     */
    private String columnName;

    /**
     * Param's actual string value.
     */
    private String value;

    /**
     * Param's line number in source file.
     */
    private int line;

    /**
     * Param's column number in source file.
     */
    private int column;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public boolean isComment() {
        return Objects.equals("comment", columnName);
    }
}
