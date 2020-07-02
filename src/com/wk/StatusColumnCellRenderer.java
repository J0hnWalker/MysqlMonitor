package com.wk;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusColumnCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        //Cells are by default rendered as a JLabel.
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        //Get the status for the current row.
        if (table.getValueAt(row, 2).equals("Syntax Error")) {
            l.setBackground(new Color(255,51,51));
        }
        else {
            if (row%2 == 0){
                l.setBackground(new Color(255,255,255));
            }
            else{
                l.setBackground(new Color(242,242,242));
            }
            //System.out.println(l.getBackground());
        }
        if (isSelected){
            table.setSelectionBackground(new Color(242,242,242));
            table.setSelectionForeground(new Color(132,172,160));
        }
        //Return the JLabel which renders the cell.
        return l;

    }
}
