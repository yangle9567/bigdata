package cn.northpark.flink;

import cn.hutool.core.date.format.FastDateFormat;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.TimeCharacteristic;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;
import org.codehaus.commons.nullanalysis.Nullable;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple;
import scala.collection.Iterable;


public class WaterMark {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //步骤一：设置时间类型，默认的是Processtime
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        DataStreamSource<String> dataStream = env.addSource(new TestSource());
        SumProcessFunction sumProcessFunction = new SumProcessFunction();
        dataStream.map(new MapFunction<String, Tuple2<String,Long>>() {
                    @Override
                    public Tuple2<String, Long> map(String line) throws Exception {
                        String[] fields = line.split(",");
                        //key/value
                        return new Tuple2<>(fields[0],Long.valueOf(fields[1]));
                    }
                    //作用：指定时间字段
                }).assignTimestampsAndWatermarks((WatermarkStrategy<Tuple2<String, Long>>) new EventTimeExtractor())
                .keyBy(0)
                .timeWindow(Time.seconds(10),Time.seconds(5)).process(sumProcessFunction).print().setParallelism(1);

        //.process(new SumProcessFunction()).print().setParallelism(1);
        env.execute("WindowWordCountAndTime");
    }



    private static class EventTimeExtractor implements AssignerWithPeriodicWatermarks<Tuple2<String,Long>> {
        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(System.currentTimeMillis());
        }

        //指定时间
        @Override
        public long extractTimestamp(Tuple2<String, Long> element, long l) {
            return element.f1;
        }
    }


    public static class  SumProcessFunction extends ProcessWindowFunction<Tuple2<String,Integer>,Tuple2<String,Integer>, Tuple, TimeWindow> {

        FastDateFormat dataformat= FastDateFormat.getInstance("HH:mm:ss");

        @Override
        public void process(Tuple tuple, Context context,
                            Iterable<Tuple2<String, Integer>> allElements,
                            Collector<Tuple2<String, Integer>> out) {
            int count = 0;
            for (Tuple2<String,Integer> e:allElements){
                count++;
            }
            out.collect(Tuple2.of(tuple.getField(0),count));
        }
    }
}


