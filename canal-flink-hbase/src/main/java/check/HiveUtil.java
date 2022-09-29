package check;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiveUtil {

    private static Map<String,Map<String,String>> hivemap = new HashMap<String, Map<String, String>>();
    private static Map<String, String> clumkvmap = new HashMap<String, String>();


    Connection connection = null;
    public static Statement stmt = null;
    ResultSet resultSet = null;
    public static ResultSetMetaData resultMetaData = null;
    HiveUtil(){
        try{
            //System.out.println("---------建连接--------");
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            //jdbc:hive2://10.9.182.144:10000/bi_test
            connection = DriverManager.getConnection("jdbc:hive2://10.9.182.144:10000/bi_test", "hdfs", "");
            stmt = connection.createStatement();

            //System.out.println("---------建完连接--------");
        }catch (Exception e){
            //throw new MyException("hive  连接失败");
            //System.out.println(e);
        }

    }
    public static void mainf(String args[]) throws Exception {
        //jdbc:hive2://10.9.182.144:10000/bi_test
        final HiveUtil hiveUtil = new HiveUtil();
        String sqlStr="select * from bi_test.youhui where id > 21648112";
        stmt.execute(sqlStr);
        ResultSet resultSet = stmt.executeQuery(sqlStr);
        // 输出查询的列名到控制台
        resultMetaData = resultSet.getMetaData();
        int columnCount = resultMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            ////System.out.print(resultMetaData.getColumnLabel(i) + '\t');
        }
        
        //System.out.println();

        // 输出查询结果到控制台
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                ////System.out.print(resultSet.getString(i) + '\t');
                clumkvmap.put(resultMetaData.getColumnLabel(i).replace("youhui.",""),resultSet.getString(i));
            }
            clumkvmap.remove("rowkey");
            if(clumkvmap.get("id") != null){
                hivemap.put(clumkvmap.get("id"),clumkvmap);
            }
            ////System.out.println();
        }

    }

    public List<Map<String, String>> getListMap() {
        return null;
    }


    public Map<String, Map<String, String>> getMap() throws Exception {
        mainf(null);
        return hivemap;
    }
}
