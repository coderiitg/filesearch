package com.fileSearch.filesearchEngineUI.model;

import javax.swing.table.DefaultTableModel;

public class ResultTableModel extends DefaultTableModel {

    public ResultTableModel(Object[] header, int rows){
        super(header, rows);    
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (super.getRowCount() == 0) {
            return Object.class;
        }
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
