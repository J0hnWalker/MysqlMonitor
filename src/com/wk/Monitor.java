package com.wk;

import javax.swing.*;
import javax.swing.table.*;
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
import java.io.File;
import java.nio.charset.Charset;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

public class Monitor {

    public static Connection connectMysql(String user, String pass, String dbname, String host, int port, JOptionPane JP){
        Connection conn = null;
        Date d = new Date();
        SimpleDateFormat time = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s?serverTimezone=Asia/Shanghai&characterEncoding=UTF-8", host, port, dbname), user, pass);
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

    public void logMonitor(String logfile, int cacheNum, JTable table, DefaultTableModel defaultModel){
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss:SSSS");

        Pattern r = Pattern.compile("Query\\s*(.*)");
        MySqlStParser mtp = new MySqlStParser();
        table.setAutoCreateRowSorter(true);
        File file = new File(logfile);
        Charset charset = Charset.forName("UTF-8");
        Tailer tailer = new Tailer(file, charset, new TailerListenerAdapter(){
            int count = 1;
            @Override
            public void handle(String line) {
                //增加的文件的内容
                String resultText = null;
                //System.out.println(line);
                Matcher m = r.matcher(line);
                if(m.find()){
                    String querycontent = "";
                    if(!m.group(1).equals("")){
                        if(m.group(1).equals("shutdown")){
                            querycontent = "shutdown";
                            resultText = "Mysql ShutDown";
                        }
                        else{
                            querycontent = m.group(1);
                            if(mtp.MySqlParser(querycontent)){
                                resultText = "Syntax Error";
                                table.getColumnModel().getColumn(1).setCellRenderer(new StatusColumnCellRenderer());
                            }
                            else{
                                resultText = "OK";
                            }
                        }
                        Vector vRow = new Vector();
                        vRow.add(count);
                        vRow.add(querycontent);
                        vRow.add(resultText);
                        vRow.add("Query");
                        vRow.add(time.format(new Date()));
                        defaultModel.addRow(vRow);
                        table.setRowSorter(null);
                        if(defaultModel.getRowCount() > 0){
                            RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(defaultModel);
                            table.setRowSorter(sorter);
                            table.setAutoCreateRowSorter(true);
                            defaultModel.fireTableDataChanged();
                        }
                        if(defaultModel.getRowCount()>cacheNum){
                            defaultModel.setRowCount(0);
                            count = 0;
                        }
                        count += 1;
                    }
                }
            }
        },500,true, false, 8192);
        tailer.run();
    }

    public static void main(String[] args){

    }
}