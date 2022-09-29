package smzdm.sink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hdfs.server.namenode.FSImageUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Slf4j
public class HbaseTemplate implements Serializable {

    private static final String SEPARATE = ",";
    private Configuration hbaseConfig;                                      // hbase配置对象
    private Connection    conn;                                             // hbase连接

    public HbaseTemplate(Configuration hbaseConfig){
        this.hbaseConfig = hbaseConfig;
        initConn();
    }

    private void initConn() {
        try {
            this.conn = ConnectionFactory.createConnection(hbaseConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        if (conn == null || conn.isAborted() || conn.isClosed()) {
            initConn();
        }
        return conn;
    }

    public boolean tableExists(String tableName) {
        try (HBaseAdmin admin = (HBaseAdmin) getConnection().getAdmin()) {
            return admin.tableExists(TableName.valueOf(tableName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createTable(String tableName, String... familyNames) {
        try (HBaseAdmin admin = (HBaseAdmin) getConnection().getAdmin()) {

            HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableName));
            // 添加列簇
            if (familyNames != null) {
                for (String familyName : familyNames) {
                    HColumnDescriptor hcd = new HColumnDescriptor(familyName);
                    desc.addFamily(hcd);
                }
            }
            admin.createTable(desc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disableTable(String tableName) {
        try (HBaseAdmin admin = (HBaseAdmin) getConnection().getAdmin()) {
            admin.disableTable(tableName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTable(String tableName) {
        try (HBaseAdmin admin = (HBaseAdmin) getConnection().getAdmin()) {
            if (admin.isTableEnabled(tableName)) {
                disableTable(tableName);
            }
            admin.deleteTable(tableName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 插入一行数据
     *
     * @param tableName 表名
     * @param hRow 行数据对象
     * @return 是否成功
     */
    public Boolean put(String tableName, HRow hRow) {
        boolean flag = false;
        try {
            HTable table = (HTable) getConnection().getTable(TableName.valueOf(tableName));
            Put put = new Put(hRow.getRowKey());
            for (HRow.HCell hCell : hRow.getCells()) {
                put.addColumn(Bytes.toBytes(hCell.getFamily()), Bytes.toBytes(hCell.getQualifier()), hCell.getValue());
            }
            table.put(put);
            flag = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;

    }

    /**
     * 批量插入
     *
     * @param tableName 表名
     * @param rows 行数据对象集合
     * @return 是否成功
     */
    public Boolean puts(String tableName, List<HRow> rows) {

        boolean flag = false;
        try {
            HTable table = (HTable) getConnection().getTable(TableName.valueOf(tableName));
            List<Put> puts = new ArrayList<>();
            for (HRow hRow : rows) {
                Put put = new Put(hRow.getRowKey());
                for (HRow.HCell hCell : hRow.getCells()) {
                    put.addColumn(Bytes.toBytes(hCell.getFamily()),
                        Bytes.toBytes(hCell.getQualifier()),
                        hCell.getValue());
                }
                puts.add(put);
            }
            if (!puts.isEmpty()) {
                table.put(puts);
            }
            flag = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    public String gets(String tableName,List<HRow> rows) {
        String tagids_str="";

        try {
            HTable table = (HTable) getConnection().getTable(TableName.valueOf(tableName));
            List<Get> gets = new ArrayList<>();
            for (HRow hRow : rows) {
                Get get = new Get(hRow.getRowKey());
                for (HRow.HCell hCell : hRow.getCells()) {
                    get.addColumn(Bytes.toBytes(hCell.getFamily()),Bytes.toBytes(hCell.getQualifier()));
                    //byte[] attribute = get.getAttribute("tag_id");
                }
                gets.add(get);
            }
            if (!gets.isEmpty()) {
                Result[] results = table.get(gets);
                for (Result result : results) {
                    byte[] tagids = result.getValue("cf1".getBytes(),"tag_id".getBytes());
                    byte[] article_id = result.getValue("cf1".getBytes(),"id".getBytes());
                    //System.out.println("-------hbaseresult---------"+Bytes.toString(tagids));
                    //System.out.println("-------hbaseresult---------"+Bytes.toString(article_id));
                    tagids_str=Bytes.toString(tagids);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //System.out.println("gets() tagids_str="+tagids_str);
        return tagids_str;

    }

    /**
     * 批量删除数据
     *
     * @param tableName 表名
     * @param rowKeys rowKey集合
     * @return 是否成功
     */
    public Boolean deletes(String tableName, Set<byte[]> rowKeys) {
        boolean flag = false;
        try {
            HTable table = (HTable) getConnection().getTable(TableName.valueOf(tableName));
            List<Delete> deletes = new ArrayList<>();
            for (byte[] rowKey : rowKeys) {
                Delete delete = new Delete(rowKey);
                deletes.add(delete);
            }
            if (!deletes.isEmpty()) {
                table.delete(deletes);
            }
            flag = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return flag;
    }

    public void close() throws IOException {
        if (conn != null) {
            conn.close();
        }
    }

    public static Set<String> gettagidset(String content) {
        if (content == null || content.length() == 0) {
            return null;
        }
        String[] tagids=content.replaceAll("\\[","").replaceAll("\\]","").split(SEPARATE);
        HashSet<String> set = new HashSet<>();
        for (String tagid : tagids) {
            set.add(tagid);
        }
        //System.out.println("------------gettagidset=");
        for (String s : set) {
            //System.out.println("s="+s);
        }
        return set;
    }

    public String query(String hbaseTableName, List<HRow> rows) {
        HashMap<String, String> columnMap = new HashMap<>();//每一行数据
        try {
            HTable table = (HTable) getConnection().getTable(TableName.valueOf(hbaseTableName));
            List<Get> gets = new ArrayList<>();
            for (HRow hRow : rows) {
                Get get = new Get(hRow.getRowKey());
                for (HRow.HCell hCell : hRow.getCells()) {
                    get.addColumn(Bytes.toBytes(hCell.getFamily()),Bytes.toBytes(hCell.getQualifier()));
                    //byte[] attribute = get.getAttribute("tag_id");
                }
                gets.add(get);
            }
            if (!gets.isEmpty()) {
                Result[] results = table.get(gets);
                //System.out.println("-----results.length="+results.length);
                String rowKey = null;
                if(null != results && results[0] != null){
                    List<Cell> cells = results[0].listCells();
                    if(null != cells){
                        for (Cell cell : cells) {
                            if (rowKey == null) {
                                rowKey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                            }
                            columnMap.put(Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()), Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
                        }
                    }

                }
                //System.out.println("---联查大宽表结果-----------------columnMap="+columnMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(columnMap);
    }

}