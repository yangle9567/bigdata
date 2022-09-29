package smzdm.sink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.canal.protocol.FlatMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.util.Bytes;
import smzdm.config.GlobalConfig;

import java.io.Serializable;
import java.util.*;


@Slf4j
    public class MyHbaseSyncService implements Serializable {
        private HbaseTemplate hbaseTemplate;                                    // HBase操作模板

        public MyHbaseSyncService(HbaseTemplate hbaseTemplate) {
            this.hbaseTemplate = hbaseTemplate;
        }

        public void sync(FlatMessage dml) {
            System.out.println("----dml--------->id="+dml.getId()+"   sqltype="+dml.getType()+" dbname.tbname="+dml.getDatabase()+"."+dml.getTable());

            if (null != dml){
                String type = dml.getType();
                String dbname = dml.getDatabase();
                String tableName = dml.getTable();

                /*String hbasetablename=dbname+"."+tableName;
                if(hbasetablename.equals("dbzdm_youhui.youhui")){
                    hbasetablename="youhui";
                }*/
                if(!hbaseTemplate.tableExists(GlobalConfig.HBASE_TABLE_NAME)){
                    hbaseTemplate.createTable(GlobalConfig.HBASE_TABLE_NAME,"cf1");
                }


                //System.out.println("dbname.tbname= "+dbname+"."+tableName);
                if (type != null && type.equalsIgnoreCase("INSERT")) {
                    insert(dml);
                } else if (type != null && type.equalsIgnoreCase("UPDATE")) {
                    update(dml);
                } else if (type != null && type.equalsIgnoreCase("DELETE")) {
                    delete(dml);
                }else if (type != null){
                    //System.out.println("--------------------DMLTYPE  typeerror--1--="+type);
                    //System.out.println(JSON.toJSONString(dml, SerializerFeature.WriteMapNullValue));
                    //System.out.println("--------------------DMLTYPE  typeerror---2--="+type);
                }
            }
        }

        private void insert(FlatMessage dml) {
            System.out.println("--------------------insert start--------------------------------");
            List<Map<String, String>> data = dml.getData();
            if (data == null || data.isEmpty()) {
                return;
            }

            List<HRow> rows = new ArrayList<>();
            for (Map<String, String> r : data) {
                HRow hRow = new HRow();
                String id = r.get("id");
                if(null != id){
                    hRow.setRowKey(Bytes.toBytes(id));
                }else{
                    //System.out.println("no column named id");
                    String s = "rowkey"+ new Date().toString();
                    hRow.setRowKey(s.getBytes());
                }
                convertData2Row(hRow, r);
                if (hRow.getRowKey() == null) {
                    throw new RuntimeException("empty rowKey: " + hRow.toString());
                }
                rows.add(hRow);

                if (!rows.isEmpty()) {
                    boolean isok = hbaseTemplate.puts(GlobalConfig.HBASE_TABLE_NAME, rows);
                    if(isok == false){
                        //System.out.println("insertfail"+JSON.toJSONString(dml, SerializerFeature.WriteMapNullValue));
                    }else {
                        //System.out.println("insertok--------->id="+dml.getId()+" auto_updatetime="+dml.getData().get(0).get("auto_updatetime")+"   sqltype="+dml.getType());
                    }
                    rows.clear();
                }
            }
            //System.out.println("--------------------insert end--------------------------------");
        }

        private void update(FlatMessage dml) {
            System.out.println("--------------------update start--------------------------------");
            List<Map<String, String>> data = dml.getData();
            if (data == null || data.isEmpty()){
                return;
            }
            ArrayList<HRow> rows = new ArrayList<>();
            for (Map<String, String> r : data) {
                HRow hRow = new HRow();
                String id = r.get("id");
                if(null != id){
                    hRow.setRowKey(Bytes.toBytes(id));
                }else{
                    hRow.setRowKey(new Date().toString().getBytes());
                }

                convertData2Row(hRow,r);
                if (hRow.getRowKey() == null){
                    throw new RuntimeException("empty rowKey: " + hRow.toString());
                }
                rows.add(hRow);

                if (!rows.isEmpty()) {
                    //更新
                    boolean isok = hbaseTemplate.puts(GlobalConfig.HBASE_TABLE_NAME, rows);
                    if(isok == false){
                        //System.out.println("updatefail"+JSON.toJSONString(dml, SerializerFeature.WriteMapNullValue));
                    }else {
                        //System.out.println("updateok--------->id="+dml.getId()+" auto_updatetime="+dml.getData().get(0).get("auto_updatetime")+"   sqltype="+dml.getType());
                    }

                    rows.clear();
                }
            }

            //System.out.println("--------------------update end--------------------------------");

        }

        public void delete(FlatMessage dml){
            System.out.println("--------------------delete start--------------------------------");
            List<Map<String, String>> data = dml.getData();
            if (data == null || data.isEmpty()){
                return;
            }
            ArrayList<HRow> rows = new ArrayList<>();
            Set<byte[]> rowKey = new HashSet<>();
            for (Map<String, String> r : data) {
                HRow hRow = new HRow();
                String id = r.get("id");
                rowKey.add(Bytes.toBytes(id));
                hRow.setRowKey(Bytes.toBytes(id));
                convertData2Row(hRow,r);
                if (hRow.getRowKey() == null){
                    throw new RuntimeException("empty rowKey: " + hRow.toString());
                }
                rows.add(hRow);

                if (!rows.isEmpty()) {
                    //删除
                    boolean isok = hbaseTemplate.deletes(GlobalConfig.HBASE_TABLE_NAME, rowKey);
                    if(isok == false){
                        //System.out.println("deletefail"+JSON.toJSONString(dml, SerializerFeature.WriteMapNullValue));
                    }else {
                        //System.out.println("deleteok--------->id="+dml.getId()+" auto_updatetime="+dml.getData().get(0).get("auto_updatetime")+"   sqltype="+dml.getType());
                    }
                    rows.clear();
                }
            }

            //System.out.println("--------------------update end--------------------------------");

        }

        public String query(FlatMessage dml) {
            System.out.println("--------------------query start--------------------------------");
            List<Map<String, String>> data = dml.getData();
            if (data == null || data.isEmpty()) {
                return null;
            }

            List<HRow> rows = new ArrayList<>();
            for (Map<String, String> r : data) {
                HRow hRow = new HRow();
                String id = r.get("id");
                if(null != id){
                    hRow.setRowKey(Bytes.toBytes(id));
                }else{
                    //System.out.println("no column named id");
                    String s = "rowkey"+ new Date().toString();
                    hRow.setRowKey(s.getBytes());
                }
                convertData2Row(hRow, r);
                if (hRow.getRowKey() == null) {
                    throw new RuntimeException("empty rowKey: " + hRow.toString());
                }
                rows.add(hRow);
            }
            if (!rows.isEmpty()) {
                String tagids=hbaseTemplate.gets(GlobalConfig.HBASE_TABLE_NAME, rows);
                //System.out.println("----tagids="+tagids);
                return tagids;
            }
            //System.out.println("--------------------query end--------------------------------");
            return null;
        }



        /**
         * 将Map数据转换为HRow行数据
         *
         * hbase映射配置
         * @param hRow 行对象
         * @param data Map数据
         */

        private static void convertData2Row(HRow hRow, Map<String, String> data) {
            String familyName = "cf1";

            for (Map.Entry<String, String> entry : data.entrySet()) {
                if (entry.getValue() != null) {

                    byte[] bytes = Bytes.toBytes(entry.getValue().toString());

                    String qualifier = entry.getKey();


                    hRow.addCell(familyName, qualifier, bytes);
                }
            }
        }


        public String queryBigwidetable(String articleid) {
            //System.out.println("--------------------queryBigwidetable start--------------------------------");
            /*HashMap<String, String> map = new HashMap<String,String>();
            map.put("rowkey",articleid);*/

            String bigwidetableinfo=null;
            List<HRow> rows = new ArrayList<HRow>();
            HRow hRow = new HRow();
            hRow.setRowKey(Bytes.toBytes(articleid));
            rows.add(hRow);
            if(null != rows){
                bigwidetableinfo=hbaseTemplate.query(GlobalConfig.HBASE_TABLE_NAME, rows);
            }
            //System.out.println("--------------------queryBigwidetable end--------------------------------");
            return bigwidetableinfo;
        }
}





