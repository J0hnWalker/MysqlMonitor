package com.wk;

import com.alibaba.druid.sql.parser.ParserException;
import java.util.List;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;

public class MySqlStParser {

    public static Boolean MySqlParser(String sql) {
        Boolean isValid = false;
        try {
            MySqlStatementParser parser = new MySqlStatementParser(sql);
            List<SQLStatement> statementList = parser.parseStatementList();
        }
        catch (ParserException e){
            isValid = true;
            //System.out.println(e.getMessage());
        }
        return isValid;
    }

    public static void main(String[] args){
        System.out.println(MySqlParser("select user"));
    }
}