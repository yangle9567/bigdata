package check;

import java.util.Map;

public class CheckSyncDW {
    public static void main(String[] args) throws Exception {
        /*MysqlUtil mysqlUtil = new MysqlUtil();
        Map<String,Map<String,String>> mysqlmap = mysqlUtil.getMap();
        HiveUtil hiveUtil = new HiveUtil();
        Map<String,Map<String,String>> hivemap = hiveUtil.getMap();
        //System.out.println("mysqlmap.size()="+mysqlmap.size()+"    hivemap.size()="+hivemap.size());
        for (String id : mysqlmap.keySet()) {
            if(hivemap.containsKey(id) && mysqlmap.get(id).size() == hivemap.get(id).size()){
                //两边id都存在并且列数相等，开始比较列值
                Map<String,String> mysqltempMap = mysqlmap.get(id);
                Map<String,String> hivetempMap = hivemap.get(id);
                boolean iscolumnsame = true;
                for (String columnName : mysqltempMap.keySet()) {
                    String mysqlcolumnvalue = mysqltempMap.get(columnName);
                    String hivecolumnvalue = hivetempMap.get(columnName);
                    if(!mysqlcolumnvalue.equals(hivecolumnvalue)){
                        iscolumnsame = false;
                    }
                }
                if(iscolumnsame == true){
                    //System.out.println(id+" ok");
                }else{
                    //System.out.println(id+" not ok");
                    //System.out.println("mysqltempMap="+mysqltempMap+"\t");
                    //System.out.println("hivetempMap="+hivetempMap+"\t");
            }else{
                //System.out.println("hivemap does not containskey "+id);
            }
        }*/

    }


}
