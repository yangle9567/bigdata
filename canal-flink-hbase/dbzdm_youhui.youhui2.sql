CREATE TABLE dbzdm_youhui_youhui (
  id                      int,
  editor_id               string,
  pubdate                 string,
  auto_updatetime         string
) WITH (
  'connector' = 'mysql-cdc',
  'hostname' = '10.45.0.55',
  'port' = '3403',
  'username' = 'bi_canal_user',
  'password' = 'CTI2xt1dbf4vur4z',
  'database-name' = 'dbzdm_youhui',
  'table-name' = 'youhui'
);

CREATE TABLE hbase (
   id STRING,
   cf1 ROW<id STRING,editor_id STRING,pubdate STRING,auto_updatetime STRING>,
   PRIMARY KEY (id) NOT ENFORCED
) WITH (
  'connector' = 'hbase-1.4',
  'table-name' = 'youhui2',
  'zookeeper.quorum' = '10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181'
);
insert into hbase select cast(id as string) id,Row(cast(id as string),editor_id,pubdate,auto_updatetime) cf1 from dbzdm_youhui_youhui;
