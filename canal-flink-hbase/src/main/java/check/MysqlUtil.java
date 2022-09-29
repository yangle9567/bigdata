package check;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MysqlUtil {

    private static Map<String,Map<String,String>> mysqlmap = new HashMap<String, Map<String, String>>();
    private static Map<String, String> clumkvmap = new HashMap<String, String>();
    public static void mainf(String[] args) {

        // mysql -h10.9.28.135 -P3320 -uwdDBUser -p'2Gb(tv+-n'
        //声明Connection对象
        Connection con;
        ResultSetMetaData resultMetaData = null;

        //驱动程序名
        String driver = "com.mysql.jdbc.Driver";
        //URL指向要访问的数据库名mydata
        String url = "jdbc:mysql://10.9.28.135:3308/dbzdm_youhui?noDatetimeStringSync=true";
        //MySQL配置时的用户名
        String user = "bi_canal_user";
        //MySQL配置时的密码
        String password = "CTI2xt1dbf4vur4z";
        //遍历查询结果集
        try {
            //加载驱动程序
            Class.forName(driver);
            //1.getConnection()方法，连接MySQL数据库！！
            con = DriverManager.getConnection(url,user,password);
            Statement statement = null;
            if(!con.isClosed())
                //System.out.println("Succeeded connecting to the Database!");
            //2.创建statement类对象，用来执行SQL语句！！
             statement = con.createStatement();
            //要执行的SQL语句
            String sql = "select * from dbzdm_youhui.youhui where id > 21648112";

            //select id from dbzdm_youhui.youhui where id > 21648112


            //3.ResultSet类，用来存放获取的结果集！！
            ResultSet resultSet = statement.executeQuery(sql);


            /*String id = null;
            String yh_status = null;
            String clean_link = null;
            while(rs.next()){
                id = rs.getString("id");
                yh_status = rs.getString("yh_status");
                clean_link = rs.getString("clean_link");
                //输出结果
                //System.out.println(id + " " + yh_status+ " "+clean_link);
            }*/

            // 输出查询的列名到控制台
            resultMetaData = resultSet.getMetaData();
            int columnCount = resultMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                //System.out.print(resultMetaData.getColumnLabel(i) + '\t');
            }

            //System.out.println();

            // 输出查询结果到控制台
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    ////System.out.print(resultSet.getString(i) + '\t');

                    clumkvmap.put(resultMetaData.getColumnLabel(i),resultSet.getString(i));
                }
                if(clumkvmap.get("id") != null){
                    mysqlmap.put(clumkvmap.get("id"),clumkvmap);
                }
                ////System.out.println();
            }


            resultSet.close();
            con.close();
        } catch(ClassNotFoundException e) {
            //数据库驱动类异常处理
            //System.out.println("Sorry,can`t find the Driver!");
            e.printStackTrace();
        } catch(SQLException e) {
            //数据库连接失败异常处理
            e.printStackTrace();
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }finally{
            //System.out.println("数据库数据成功获取！！");
        }
    }

    public List<Map<String, String>> getListMap() {
        return null;
    }

    public Map<String, Map<String, String>> getMap() {
        mainf(null);
        return mysqlmap;
    }

}