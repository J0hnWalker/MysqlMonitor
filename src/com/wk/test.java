package com.wk;

import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static monitor m;
    public static Connection conn;
    public static Thread t1;
    public static Connection getConn(){
        return conn;
    }
    public static Thread getT1(){
        return t1;
    }
    public static int cacheNum = 50;
    public static Vector vdata = new Vector();
    public static Vector vtitle = new Vector();
    public static DefaultTableModel defaultModel = null;

    public test() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                test.t1 = new Thread() {
                    public void run() {
                        test.m = new monitor();
                        String username = usernameField.getText();
                        char[] pw = passwordField.getPassword();
                        String password = new String(pw);
                        int port = Integer.valueOf(portField.getText());
                        String host = hostField.getText();
                        String dbname = dbField.getText();
                        test.conn = test.m.connectMysql(username, password, dbname, host, port, JP);
                        test.m.execSql(test.conn, "set global general_log=on;");
                        String[] logStatus = test.m.execSql(test.conn, "show variables like 'general_log';").trim().split("\t");
                        System.out.println(logStatus[1]);
                        //while (logStatus[1].equals("ON")) {
                        while(!Thread.currentThread().isInterrupted()){
                            System.out.println("ISINTERRUPTED: "+test.t1.isInterrupted());
                            try {
                                test.m.logMonitor(test.m.logSwitch(test.conn, bottomlabel), TextArea1, test.cacheNum, vdata, vtitle, table1, defaultModel);
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e){
                                System.out.println("ISINTERRUPTED: "+test.t1.isInterrupted());
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                };
                test.t1.start();
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showMessageDialog(null, "Hello Walker.");
                Connection cn = getConn();
                Thread t2 = getT1();
                if (cn != null) {
                    try {
                        test.m.execSql(test.conn, "set global general_log=off;");
                        cn.close();
                        t2.interrupt();
                        JOptionPane.showMessageDialog(null, "关闭连接.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        vtitle.add("id");
        vtitle.add("requestText");
        vtitle.add("requestType");
        vtitle.add("time");
        defaultModel = new DefaultTableModel(vdata, vtitle);
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table1.setModel(defaultModel);
        JScrollPane2.setViewportView(table1);
        comboBox1.addItem(50);
        comboBox1.addItem(100);
        comboBox1.addItem(200);
        JScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane1.setViewportView(TextArea1);
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
        JFrame frame = new JFrame("test");
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
    private JTextArea TextArea1;
    private JScrollPane JScrollPane1;
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