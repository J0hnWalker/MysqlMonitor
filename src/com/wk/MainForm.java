package com.wk;

import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

public class MainForm {
    public static Monitor m;
    public static Connection conn;
    public static Thread t1;
    public static Connection getConn(){
        return conn;
    }
    public static int cacheNum = 100;
    public static DefaultTableModel defaultModel = null;

    public MainForm() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                t1 = new Thread() {
                    public void run() {
                        MainForm.m = new Monitor();
                        String username = usernameField.getText();
                        char[] pw = passwordField.getPassword();
                        String password = new String(pw);
                        int port = Integer.valueOf(portField.getText());
                        String host = hostField.getText();
                        String dbname = dbField.getText();
                        MainForm.conn = MainForm.m.connectMysql(username, password, dbname, host, port, JP);
                        if (MainForm.conn != null) {
                            button1.setEnabled(false);
                            MainForm.m.execSql(MainForm.conn, "set global general_log=on;");
                            String[] logStatus = MainForm.m.execSql(MainForm.conn, "show variables like 'general_log';").trim().split("\t");
                            while (!Thread.currentThread().isInterrupted()) {
                                try {
                                    m.logMonitor(MainForm.m.logSwitch(MainForm.conn, bottomlabel), MainForm.cacheNum, table1, defaultModel);
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                    }
                };
                t1.start();
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Connection cn = getConn();
                if (cn != null) {
                    try {
                        MainForm.m.execSql(MainForm.conn, "set global general_log=off;");
                        cn.close();
                        t1.interrupt();
                        defaultModel.setRowCount(0);
                        bottomlabel.setText("连接关闭");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                if (button2 == e.getSource()){
                    button1.setEnabled(true);
                }
            }
        });
        String header[] = new String[] { "id", "requestText", "return", "requestType", "time"};
        defaultModel = new DefaultTableModel(0, 0){
            @Override
            public Class<?> getColumnClass(int column){
                Class returnValue;
                if (column == 0)
                {
                    //returnValue = getValueAt(0, column).getClass();
                    returnValue = Integer.class;
                }
                else{
                    returnValue = String.class;
                }
                return returnValue;
            }
        };
        defaultModel.setColumnIdentifiers(header);
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table1.setModel(defaultModel);
        table1.getColumnModel().getColumn(0).setPreferredWidth(20);
        table1.getColumnModel().getColumn(1).setPreferredWidth(500);
        table1.getColumnModel().getColumn(2).setPreferredWidth(100);
        table1.getColumnModel().getColumn(3).setPreferredWidth(100);
        table1.getColumnModel().getColumn(4).setPreferredWidth(100);
        JScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane2.setViewportView(table1);
        comboBox1.addItem(100);
        comboBox1.addItem(200);
        comboBox1.addItem(500);
        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println(comboBox1.getSelectedItem());
                MainForm.cacheNum = (Integer) comboBox1.getSelectedItem();
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        JFrame frame = new JFrame("MysqlLogMonitor");
        frame.setSize(1000, 500); //窗体初始大小
        frame.setLocationRelativeTo(null); //居中显示
        frame.setContentPane(new MainForm().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }


    private JButton button1;
    private JPanel panelMain;
    private JTextField usernameField;
    private JTextField portField;
    private JButton button2;
    private JPasswordField passwordField;
    private JPanel panel2;
    private JComboBox comboBox1;
    private JLabel bottomlabel;
    private JLabel hostLabel;
    private JTextField hostField;
    private JLabel dblabel;
    private JTextField dbField;
    private JTable table1;
    private JScrollPane JScrollPane2;
    private JButton 断开连接Button;
    private JOptionPane JP;
}