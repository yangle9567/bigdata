CREATE TABLE youhui_tag_type_item (
  id string,
  article_id string,
  tag_id STRING
) WITH (
  'connector' = 'cdc-mysql',
  'hostname' = '10.45.0.55',
  'port' = '3403',
  'username' = 'bi_canal_user',
  'password' = 'CTI2xt1dbf4vur4z',
  'database-name' = 'dbzdm_youhui',
  'table-name' = 'youhui_tag_type_item'
);
CREATE TABLE hbase (
   rowkey STRING,
   cf1 ROW<id STRING,tag_id STRING>,
   PRIMARY KEY (rowkey) NOT ENFORCED
) WITH (
'connector' = 'hbase-1.4',
'table-name' = 'bigwidetable',
'zookeeper.quorum' = '10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181'
);
insert into hbase select rowkey,Row(id,article_id,tag_id) from (select article_id as rowkey,article_id as id,article_id,listagg(tag_id,',') as tag_id from youhui_tag_type_item  group by article_id) t;



