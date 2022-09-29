CREATE TABLE youhui_tag_type_item (
  id string,
  article_id string,
  tag_id STRING
) WITH (
  'connector' = 'cdc-mysql',
  'hostname' = '10.42.132.50',
  'port' = '3306',
  'username' = 'canal',
  'password' = 'canal',
  'database-name' = 'dbzdm_youhui',
  'table-name' = 'youhui_tag_type_item'
);

CREATE TABLE kafka_youhuitag (
  id string,
  article_id string,
  tag_id STRING
) WITH (
    'connector' = 'kafka',
    'topic' = 'test',
    'scan.startup.mode' = 'earliest-offset',
    'properties.bootstrap.servers' = '10.45.4.146:9092',
    'format' = 'json'
);
insert into kafka_youhuitag select * from youhui_tag_type_item;

