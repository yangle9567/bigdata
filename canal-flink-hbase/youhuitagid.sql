CREATE TABLE youhui_tag_type_item (
  id string,
  article_id bigint,
  tag_id bigint
) WITH (
  'connector' = 'jdbc',
  'url'='jdbc:mysql://10.45.0.55:3403/dbzdm_youhui?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false',
  'table-name' = 'youhui_tag_type_item',
  'driver' = 'com.mysql.jdbc.Driver',
  'username' = 'bi_canal_user',
  'password' = 'CTI2xt1dbf4vur4z'
);

CREATE TABLE hbase (
   rowkey STRING,
   cf1 ROW<id STRING,tag_id STRING>,
   PRIMARY KEY (id) NOT ENFORCED
) WITH (
  'connector' = 'hbase-1.4',
  'table-name' = 'youhui',
  'zookeeper.quorum' = '10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181'
);

insert into hbase
select cast(id as string) rowkey,Row(cast(id as string),tag_id) cf1
from (select article_id as id,listagg(cast(tag_id as string),',') as tag_id
from youhui_tag_type_item
group by article_id) a;