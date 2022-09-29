import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义operator
 *
 * @author lixiyan
 * @date 2020/4/5 9:11 PM
 */
public class OperatorStateCustom extends RichFlatMapFunction<Tuple2<String, Long>, Tuple2<String, List<Tuple2<String, Long>>>> implements CheckpointedFunction {

    // 非正常数据
    private List<Tuple2<String, Long>> bufferedData;
    // checkPointedState
    private transient ListState<Tuple2<String, Long>> checkPointedState;
    // 需要监控的阈值
    private Long threshold;
    // 次数
    private Integer numberOfTimes;


    OperatorStateCustom(Long threshold, Integer numberOfTimes) {
        this.threshold = threshold;
        this.numberOfTimes = numberOfTimes;
        this.bufferedData = new ArrayList<>();
    }


    @Override
    public void flatMap(Tuple2<String, Long> value, Collector<Tuple2<String, List<Tuple2<String, Long>>>> out) throws Exception {
        Long inputVal = value.f1;
        // 超过阀值记录
        if (inputVal > threshold){
            bufferedData.add(value);
        }
        // 超过指定次数则输出报警信息
        if (bufferedData.size()>= numberOfTimes){
            // 输出状态实例的hashcode
            out.collect(Tuple2.of(checkPointedState.hashCode()+" 超过阀值",bufferedData));
            bufferedData.clear();

        }

    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        // 在进行快照时，将数据存储到checkPointedState
        checkPointedState.clear();
        //System.out.println("snapshotState");
        for (Tuple2<String, Long> element : bufferedData) {
            //System.out.println(element);
            checkPointedState.add(element);
        }
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        // 注意这里获取的是OperatorStateStore
        checkPointedState = context.getOperatorStateStore()
                .getListState(new ListStateDescriptor<>("abnormalData", TypeInformation.of(new TypeHint<Tuple2<String, Long>>() {
        })));

        //System.out.println("initializeState:"+checkPointedState.get());
        // 如果发生重启，则需要从快照中将状态进行恢复
        if (context.isRestored()){
            //System.out.println("aaaaaa");
            for (Tuple2<String, Long> element : checkPointedState.get()) {
                bufferedData.add(element);
            }
        }

    }
}