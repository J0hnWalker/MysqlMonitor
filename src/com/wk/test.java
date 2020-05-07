package com.wk;

import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

public class test {
    public static monitor m;
    public static Connection conn;
    public static Thread t1;
    public static Connection getConn(){
        return conn;
    }
    public static int cacheNum = 50;
    public static DefaultTableModel defaultModel = null;

    public test() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                t1 = new Thread() {
                    public void run() {
                        test.m = new monitor();
                        String username = usernameField.getText();
                        char[] pw = passwordField.getPassword();
                        String password = new String(pw);
                        int port = Integer.valueOf(portField.getText());
                        String host = hostField.getText();
                        String dbname = dbField.getText();
                        test.conn = test.m.connectMysql(username, password, dbname, host, port, JP);
                        if (test.conn != null) {
                            button1.setEnabled(false);
                            test.m.execSql(test.conn, "set global general_log=on;");
                            String[] logStatus = test.m.execSql(test.conn, "show variables like 'general_log';").trim().split("\t");
                            //System.out.println(logStatus[1]);
                            //while (logStatus[1].equals("ON")) {
                            while (!Thread.currentThread().isInterrupted()) {
                                //System.out.println("threadid t1: "+currentThread().getId());
                                try {
                                    m.logMonitor(test.m.logSwitch(test.conn, bottomlabel), test.cacheNum, table1, defaultModel);
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    //System.out.println("ISINTERRUPTED: "+test.t1.isInterrupted());
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
                //JOptionPane.showMessageDialog(null, "Hello Walker.");
                Connection cn = getConn();
                if (cn != null) {
                    try {
                        //System.out.println("threadid t1: "+t1.getId());
                        test.m.execSql(test.conn, "set global general_log=off;");
                        cn.close();
                        //System.out.println("isAlive: " + test.process.isAlive());
                        t1.interrupt();
                        defaultModel.setRowCount(0);
                        String OS = System.getProperty("os.name").toLowerCase();
                        String[] command = null;
                        Process process = null;
                        if (OS.contains("win")) {
                            command = new String[]{"cmd.exe", "/c", "taskkill /f /im tail.exe"};
                        }
                        else{
                            command = new String[]{"/bin/sh", "-c", "pkill tail"};
                        }
                        process = Runtime.getRuntime().exec(command);
                        //process.destroy();
                        //JOptionPane.showMessageDialog(null, "连接关闭.");
                        bottomlabel.setText("连接关闭");
                    } catch (SQLException | IOException ex) {
                        ex.printStackTrace();
                    }
                }
                if (button2 == e.getSource()){
                    button1.setEnabled(true);
                }
            }
        });
        String header[] = new String[] { "id", "requestText", "requestType",
                "time"};
        defaultModel = new DefaultTableModel(0, 0);
        defaultModel.setColumnIdentifiers(header);
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table1.setModel(defaultModel);
        JScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane2.setViewportView(table1);
        comboBox1.addItem(50);
        comboBox1.addItem(100);
        comboBox1.addItem(200);
        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println(comboBox1.getSelectedItem());
                test.cacheNum = (Integer) comboBox1.getSelectedItem();
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (UnsupportedLookAndFeelException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        JFrame frame = new JFrame("MysqlLogMonitor");
        //frame.setSize(50, 50);
        frame.setContentPane(new test().panelMain);
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