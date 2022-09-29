package cn.northpark.flink.project.syncIO.function;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/***
 * 高效低延时
 * 异步执行关联数据库【线程池】
 */
public class AsyncMysqlToActivityBeanFunciton extends RichAsyncFunction<String, String> {

    private transient HikariDataSource dataSource;
    private transient ExecutorService executorService;

//   private transient DruidDataSource dataSource;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        executorService = Executors.newFixedThreadPool(20);
        HikariConfig hikariConfig = new HikariConfig();
        //oracle-jdbc:oracle:thin:@localhost:1521:orcl
        //mysql
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/flink?characterEncoding=UTF-8");
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("123456");
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setMaximumPoolSize(20);
        dataSource = new HikariDataSource(hikariConfig);

//        dataSource = new DruidDataSource();
//        dataSource.setDriverClassName (" com.mysql.jdbc.Driver");
//        dataSource.setUsername (" root");
//        dataSource.setPassword ("123456");
//        dataSource.setUrl("jdbç:mysql: //localhost 3306/bigdata? characterEncoding=UTF-8");
//        dataSource.setInitialSize(5);
//        dataSource.setMinIdle(10);
//        dataSource.setMaxActive(20);
    }


    @Override
    public void asyncInvoke(String id, ResultFuture<String> resultFuture) throws Exception {

        Future<String> future = executorService.submit(() -> {
            return queryFromMySql(id);
        });

        CompletableFuture.supplyAsync(new Supplier<String>() {

            @Override
            public String get() {
                try {
                    return future.get();
                } catch (Exception e) {
                    return null;
                }
            }
        }).thenAccept((String dbResult) -> {
            resultFuture.complete(Collections.singleton(dbResult));
        });
    }

    @Override
    public void close() throws Exception {
        super.close();
        dataSource.close();
        executorService.shutdown();
    }


    private String queryFromMySql(String param) throws SQLException {
        String sql = "SELECT * FROM t_activity WHERE id = ?";
        String result = null;
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, param);
            rs = stmt.executeQuery();
            while (rs.next()) {
                result = rs.getString("name");
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        if (result != null) {
            //放到缓存中
        }
        return result;


    }

}
