CREATE TABLE youhui_tag_type_item (
  id string,
  article_id bigint,
  tag_id bigint
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
   id STRING,
   cf1 ROW<id STRING,tag_id STRING>,
   PRIMARY KEY (id) NOT ENFORCED
) WITH (
  'connector' = 'hbase-1.4',
  'table-name' = 'bigwidetable',
  'zookeeper.quorum' = '10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181'
);
insert into hbase
select cast(id as string) id,Row(cast(id as string),tag_id) cf1
from (select article_id as id,listagg(cast(tag_id as string),',') as tag_id
from youhui_tag_type_item
group by article_id) a;