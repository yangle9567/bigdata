import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 测试operator state
 *
 * @author lixiyan
 * @date 2020/4/5 10:35 PM
 */
public class testoperatorstate {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(11);
        //设置statebackend
        env.setStateBackend(new FsStateBackend("file:///Users/lionli/lixiyan/flink/flink_practice/flink/src/main/resources/"));
        CheckpointConfig config = env.getCheckpointConfig();
        // 任务流取消和故障时会保留Checkpoint数据，以便根据实际需要恢复到指定的Checkpoint
        config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 设置checkpoint的周期, 每隔100 ms进行启动一个检查点
        config.setCheckpointInterval(10000);
        // 设置模式为exactly-once
        config.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        // 检查点必须在一分钟内完成，或者被丢弃【checkpoint的超时时间】
        config.setCheckpointTimeout(60000);
        // 同一时间只允许进行一个检查点
        config.setMaxConcurrentCheckpoints(1);
        // 执行逻辑
        DataStreamSource<Tuple2<String, Long>> tuple2DataStreamSource = env.setParallelism(1).fromElements(
                Tuple2.of("a", 250L), Tuple2.of("a", 80L), Tuple2.of("a", 400L),
                Tuple2.of("a", 100L), Tuple2.of("a", 200L), Tuple2.of("a", 200L),
                Tuple2.of("b", 100L), Tuple2.of("b", 200L), Tuple2.of("b", 200L),
                Tuple2.of("b", 500L), Tuple2.of("b", 600L), Tuple2.of("b", 700L));

        tuple2DataStreamSource.flatMap(new OperatorStateCustom(10L, 3)).printToErr();
        env.execute("Operator State");
    }
}