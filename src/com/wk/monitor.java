package com.wk;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.regex.*;
import java.util.stream.StreamSupport;

public class monitor {

    public static Connection connectMysql(String user, String pass, String dbname, String host, int port, JOptionPane JP){
        Connection conn = null;
        Date d = new Date();
        SimpleDateFormat time = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s", host, port, dbname), user, pass);
            //System.out.println(time.format(d) + ": 数据库连接成功");
            return conn;

        } catch (ClassNotFoundException | SQLException e) {
            //e.printStackTrace();
            //System.out.println(time.format(d) + ": 数据库连接失败..." + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), new String("连接失败"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static String execSql(Connection conn, String sql){
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            StringBuilder results = new StringBuilder();
            //检索此 ResultSet对象的列的数量，类型和属性
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            //返回列数
            int column = resultSetMetaData.getColumnCount();
            while(rs.next()){
                for (int i = 1; i <= column; i++){
                    //System.out.print(rs.getObject(i) + "\t");
                    results.append(rs.getObject(i)).append("\t");
                }
                //System.out.print("\n");
                results.append("\n");
            }
            String text = results.toString();
            rs.close();
            ps.close();
            //conn.close();
            return text;
        } catch (SQLException | NullPointerException e) {
            //e.printStackTrace();
            return null;
        }
    }


    public static String logSwitch(Connection conn, JLabel label){
        Date d = new Date();
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat time1 = new SimpleDateFormat("yyyy-MM-dd");
        String path = execSql(conn, "select @@datadir;").trim().replace("\\", "/") + String.format("%s.txt", time1.format(d));
        //Connection conn = connectMysql("root", "toor", "sec_sql", "127.0.0.1", 8889);
        String[] logfile = null;
        if(conn != null) {
            //String ver = execSql(conn, "select VERSION();");
            //System.out.print(time.format(d) + ": 数据库版本: " + ver);
            String[] logStatus = execSql(conn, "show variables like 'general_log';").trim().split("\t");


            if (logStatus[1].equals("OFF")) {
                //System.out.println(time.format(d) + ": 正在尝试开启日志模式");
                label.setText(time.format(d) + ": 正在尝试开启日志模式");
                execSql(conn, "set global general_log=on;");
                logStatus = execSql(conn, "show variables like 'general_log';").trim().split("\t");
                if (logStatus[1].equals("ON")) {
                    //开启日志模式后设置log文件存放路径
                    //System.out.println(time.format(d) + ": 日志模式已开启");
                    label.setText(time.format(d) + ": 日志模式已开启");
                    //System.out.println(path);
                    execSql(conn, "set global general_log_file='" + path + "';");
                    logfile = execSql(conn, "show variables like 'general_log_file';").trim().split("\t");
                    //System.out.println(time.format(d) + ": 日志文件: " + logfile[1]);
                    //System.out.println(time.format(d) + ": 日志监听中...");
                    label.setText(time.format(d) + ": 日志监听中...");
                    try {
                      //  logMonitor(logfile[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //System.out.println(time.format(d) + ": 日志模式已开启");
                //System.out.println(time.format(d) + ": 日志监听中...");
                label.setText(time.format(d) + ": 日志模式已开启");
                label.setText(time.format(d) + ": 日志监听中...");
                execSql(conn, "set global general_log_file='" + path + "';");
                logfile = execSql(conn, "show variables like 'general_log_file';").trim().split("\t");
                try {
                   // logMonitor(logfile[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return logfile[1];
    }

    public static Process logMonitor(String logfile, int cacheNum, JTable table, DefaultTableModel defaultModel){
        Process process = null;
        String[] command = null;
        Date d = new Date();
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String OS = System.getProperty("os.name").toLowerCase();
            if (OS.contains("win")) {
                command = new String[]{"cmd.exe", "/c", "tail.exe -f \"" + logfile + "\""};
            }
            else{
                command = new String[]{"/bin/sh", "-c", "tail -f \"" + logfile + "\""};
            }
            process = Runtime.getRuntime().exec(command);
            final InputStream in = process.getInputStream();
//            Thread t = new Thread() {
//                public void run() {
//
//                }
//            };
//            t.start();
            BufferedReader read = null;
            try {
                read = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Pattern r = Pattern.compile("Query\\s*(.*)");
            try {
                MySqlStParser mtp = new MySqlStParser();
                String line = null;
                int count = 1;
                //String sql = "";
                String resultText = null;
//                TableColumn tableColumn = table.getColumn("requestText");
//                DefaultTableCellRenderer backGroundColor = new DefaultTableCellRenderer();
//                backGroundColor.setBackground(Color.red);
//                tableColumn.setCellRenderer(backGroundColor);
                while ((line = read.readLine()) != null) {
                    try{
                        Matcher m = r.matcher(line);
                        if(m.find()){
                            //System.out.println(time.format(d) + " : " + m.group(0));
                            if(mtp.MySqlParser(m.group(1))){
                                //sql = "[error] " + m.group(1);
                                resultText = "Syntax Error";
                                table.getColumnModel().getColumn(1).setCellRenderer(new StatusColumnCellRenderer());
                            }
                            else{
                                resultText = "OK";
                            }
                            Vector vRow = new Vector();
                            vRow.add(count);
                            vRow.add(m.group(1));
                            vRow.add(resultText);
                            vRow.add("Query");
                            vRow.add(time.format(d));

                            defaultModel.addRow(vRow);
                            if(table.getRowCount()>cacheNum){
                                defaultModel.setRowCount(0);
                                count = 0;
                            }
                            count += 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally{
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            process.waitFor();
            process.destroy();
        }
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
        return process;
    }

    public static void main(String[] args){
//        Connection conn = null;
//        String logfile = null;
//        //conn = connectMysql("root", "toor", "sys", "127.0.0.1", 3306);
//        //conn = connectMysql("root", "toor", "test", "127.0.0.1", 3306);
//        //logfile = logSwitch(conn);
//        try {
//            //logMonitor(logfile, textArea1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try{
//            execSql(conn, "set global general_log=off;");
//            if (conn != null) {
//                conn.close();
//            }
//        }
//        catch (SQLException ex){
//            ex.printStackTrace();
//        }


    }
}