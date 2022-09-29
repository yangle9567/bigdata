package cn.northpark.flink.window;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * Session窗口 :以2条数据的时间差来划分窗口,时间差>n,则触发窗口
 * @author bruce
 */
public class SessionWindow {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> source = env.socketTextStream("localhost", 4000);

        //spark 1
        //spark 2
        //java 3
        SingleOutputStreamOperator<Tuple2<String,Integer>> map = source.map(new MapFunction<String, Tuple2<String,Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String value) throws Exception {
                String[] lines = value.split(" ");
                return Tuple2.of(lines[0],Integer.parseInt(lines[1]));
            }
        });

        //先分组
        KeyedStream<Tuple2<String, Integer>, Tuple> keyed = map.keyBy(0);

        //按照分组后分窗口
//        WindowedStream<Tuple2<String, Integer>, Tuple, TimeWindow> window = keyed.timeWindow(Time.seconds(5));
        WindowedStream<Tuple2<String, Integer>, Tuple, TimeWindow> window = keyed.window(ProcessingTimeSessionWindows.withGap( Time.seconds(5)));


        SingleOutputStreamOperator<Tuple2<String, Integer>> summed = window.sum(1);

        summed.print();

        env.execute("SessionWindow");


    }
}
