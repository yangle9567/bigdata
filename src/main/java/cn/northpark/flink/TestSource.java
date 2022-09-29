package cn.northpark.flink;


import cn.hutool.core.date.format.FastDateFormat;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.concurrent.TimeUnit;

public  class TestSource implements SourceFunction<String> {
    FastDateFormat dateformat =   FastDateFormat.getInstance("HH:mm:ss");
    @Override
    public void run(SourceContext<String> cxt) throws Exception {
        String currTime = String.valueOf(System.currentTimeMillis());
        while(Integer.valueOf(currTime.substring(currTime.length() - 4)) > 100){
            currTime=String.valueOf(System.currentTimeMillis());
            continue;
        }
        System.out.println("开始发送事件的时间："+dateformat.format(System.currentTimeMillis()));
        TimeUnit.SECONDS.sleep(3);
        //13s 第1个事件
        String event="flink,"+System.currentTimeMillis();//时间
        cxt.collect(event);

        TimeUnit.SECONDS.sleep(3);//16s 第2个事件
        cxt.collect("flink,"+System.currentTimeMillis());

        TimeUnit.SECONDS.sleep(3);
        //19s 第3个事件
        cxt.collect(event);

        TimeUnit.SECONDS.sleep(3000);



    }

    @Override
    public void cancel() {

    }
}
