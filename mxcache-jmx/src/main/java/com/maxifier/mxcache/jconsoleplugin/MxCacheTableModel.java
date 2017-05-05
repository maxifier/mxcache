/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.jconsoleplugin;

import javax.swing.table.AbstractTableModel;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class MxCacheTableModel extends AbstractTableModel {
    private static final Attribute[] ATTRIBUTES = Attribute.values();

    private Object[][] rows = {};

    @Override
    public String getColumnName(int column) {
        return ATTRIBUTES[column].toString();
    }

    @Override
    public int getRowCount() {
        return rows == null ? 0 : rows.length;
    }

    @Override
    public int getColumnCount() {
        return ATTRIBUTES.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return rows[row][col];
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        throw new UnsupportedOperationException();
    }

    public void setRows(Object[][] rows) {
        this.rows = rows;
        fireTableDataChanged();
    }
}
