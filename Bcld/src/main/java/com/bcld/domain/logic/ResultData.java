package com.bcld.domain.logic;

import java.io.Serializable;

/**
 * @author liudecai
 * 
 */
public class ResultData implements Serializable {

    private static final long serialVersionUID = 5130552949774128768L;

    private String engineName;

    private String engineIp;

    private String cpu;

    private String memory;

    private String jobId;
    /**
     * 任务状态 0强行停止成功，1正在运行中，2扫描完成，3暂停成功，4引擎已启动.心跳
     *  5任务出错
     */
    private Integer status;

    /**
     * 进程号
     */
    private Integer processId;

    /**
     * 页面数或者站点数
     */
    private Integer jobQuantity;
    /**
     * 完成进度
     */
    private Integer jobPercent;
    /**
     * 扫描结果
     */
    private JobResult jobResult;
    
    /**
     * 资产Id用于删除日至分析资产日志
     */
    private String assetId;
    
    /**
     * 因为资产信息导致引擎无法序列化，只能取到orderid，用于删除job
     */
    private Long jobOrderId;
    

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getEngineIp() {
        return engineIp;
    }

    public void setEngineIp(String engineIp) {
        this.engineIp = engineIp;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public Integer getJobQuantity() {
        return jobQuantity;
    }

    public void setJobQuantity(Integer jobQuantity) {
        this.jobQuantity = jobQuantity;
    }

    public Integer getJobPercent() {
        return jobPercent;
    }

    public void setJobPercent(Integer jobPercent) {
        this.jobPercent = jobPercent;
    }

    public JobResult getJobResult() {
        return jobResult;
    }

    public void setJobResult(JobResult jobResult) {
        this.jobResult = jobResult;
    }
    
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }
    
    public Long getJobOrderId() {
        return jobOrderId;
    }

    public void setJobOrderId(Long jobOrderId) {
        this.jobOrderId = jobOrderId;
    }

}
