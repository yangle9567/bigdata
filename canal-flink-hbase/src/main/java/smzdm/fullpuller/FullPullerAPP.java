package smzdm.fullpuller;


import smzdm.config.GlobalConfig;
import smzdm.utils.JdbcUtil;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.hadoop.mapreduce.HadoopOutputFormat;
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat;
import org.apache.flink.api.java.io.jdbc.split.NumericBetweenParametersProvider;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.types.Row;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 全量拉取
 *
 * @author dajiangtai
 * @create 2019-07-30-16:51
 */
public class FullPullerAPP {
    public static final boolean isparallelism = true;


    public static final String SPLIT_FIELD = "goodsId";

    public  static final RowTypeInfo ROW_TYPE_INFO = new RowTypeInfo(
            BasicTypeInfo.INT_TYPE_INFO,
            BasicTypeInfo.STRING_TYPE_INFO,
            BasicTypeInfo.BIG_DEC_TYPE_INFO,
            BasicTypeInfo.INT_TYPE_INFO,
            BasicTypeInfo.INT_TYPE_INFO
    );
    public static void main(String[] args) throws Exception {
        //获取实现环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //读取商品表
        JDBCInputFormat.JDBCInputFormatBuilder jdbcInputFormatBuilder = JDBCInputFormat.buildJDBCInputFormat()
                .setDrivername(GlobalConfig.DRIVER_CLASS)
                .setDBUrl(GlobalConfig.DB_URL)
                .setUsername(GlobalConfig.USER_MAME)
                .setPassword(GlobalConfig.PASSWORD)
                .setQuery("select * from dajiangtai_goods")
                .setRowTypeInfo(ROW_TYPE_INFO);

        if(isparallelism){
            int fetchSize = 2;

            Boundary boundary = boundaryQuery(SPLIT_FIELD);

            jdbcInputFormatBuilder.setQuery("select * from dajiangtai_goods where "+SPLIT_FIELD+" between ? and ?")
                                    .setParametersProvider(new NumericBetweenParametersProvider(fetchSize,boundary.min,boundary.max));
        }

        //读取MySQL数据
        DataSource<Row> source = env.createInput(jdbcInputFormatBuilder.finish());

        //生成HBase输出数据
        DataSet<Tuple2<Text, Mutation>> hbaseResult =convertMysqlToHBase(source);

        //数据输出到HBase
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum","master,slave1,slave2");
        conf.set("hbase.zookeeper.property.clientPort","2181");
        conf.set("zookeeper.znode.parent","/hbase");
        conf.set(TableOutputFormat.OUTPUT_TABLE,"learing_flink:dajiangtai_goods");
        conf.set("mapreduce.output.fileoutputformat.outputdir","/tmp");

        //new 一个job实例
        Job job = Job.getInstance(conf);


        hbaseResult.output(new HadoopOutputFormat<Text, Mutation>(new TableOutputFormat<>(),job));

        env.execute("FullPullerAPP");

    }

    public static DataSet<Tuple2<Text,Mutation>> convertMysqlToHBase(DataSet<Row> ds){
        return ds.map(new RichMapFunction<Row, Tuple2<Text, Mutation>>() {
            private transient Tuple2<Text,Mutation> resultTp ;

            private  byte[] cf = "F".getBytes(ConfigConstants.DEFAULT_CHARSET);

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                resultTp = new Tuple2<>();
            }

            @Override
            public Tuple2<Text, Mutation> map(Row row) throws Exception {
                resultTp.f0 = new Text(row.getField(0).toString());

                Put put = new Put(row.getField(0).toString().getBytes(ConfigConstants.DEFAULT_CHARSET));

                if(null != row.getField(1)){
                    put.addColumn(cf, Bytes.toBytes("goodsName"),Bytes.toBytes(row.getField(1).toString())) ;
                }

                put.addColumn(cf, Bytes.toBytes("sellingPrice"),Bytes.toBytes(row.getField(2).toString())) ;

                put.addColumn(cf, Bytes.toBytes("goodsStock"),Bytes.toBytes(row.getField(3).toString())) ;

                put.addColumn(cf, Bytes.toBytes("appraiseNum"),Bytes.toBytes(row.getField(4).toString())) ;

                resultTp.f1 = put;
                return resultTp;
            }
        });

    }

    public static Boundary boundaryQuery(String splitField) throws Exception{
        String sql = "select min("+splitField+") , max("+splitField+") from dajiangtai_goods";
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        int min = 0;
        int max = 0;
        try {

            connection = JdbcUtil.getConnection();

            statement = connection.createStatement();

            resultSet = statement.executeQuery(sql);

            while (resultSet.next()){
                min = resultSet.getInt(1);
                max = resultSet.getInt(2);

                //System.out.println(min+"------------------"+max);
            }

        }finally {
            JdbcUtil.close(resultSet,statement,connection);
        }

        return Boundary.of(min,max);

    }

    public static class Boundary{
        private int min;
        private int max;

        public Boundary(int min ,int max){
            this.min = min;
            this.max = max;

        }

        public static Boundary of(int min ,int max){
            return new Boundary(min,max);
        }
    }
}
