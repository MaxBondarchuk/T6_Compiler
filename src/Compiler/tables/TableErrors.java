package Compiler.tables;

import java.util.Vector;

public class TableErrors {
    public Vector<Integer> errors_rows;
    public Vector<Integer> errors_columns;
    public Vector<String> errors_comments;

    public TableErrors() {
        errors_rows = new Vector<Integer>();
        errors_columns = new Vector<Integer>();
        errors_comments = new Vector<String>();
    }

    public int getErrorRow(int index) {
        return errors_rows.get(index);
    }

    public int getErrorColumn(int index) {
        return errors_columns.get(index);
    }

    public String getErrorComment(int index) {
        return errors_comments.get(index);
    }

    public void addError(int row, int column, String comment) {
        errors_rows.add(row);
        errors_columns.add(column);
        errors_comments.add(comment);
    }

    public int size() {
        return errors_rows.size();
    }
}
