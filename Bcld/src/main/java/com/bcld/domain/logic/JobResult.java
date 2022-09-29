package com.bcld.domain.logic;

public interface JobResult {

    public String vulnerability = "vulnerability";
    public String trojan = "trojan";
    public String usability = "usability";
    public String tamper = "tamper";
    public String illegal_link = "illegal_link";
    public String keyword = "keyword";
    public String new_vulnerability = "new_vulnerability";

    /**
     * 获取任务结果的类型
     * 
     * @return vulnerability,trojan,usability,tamper,illegal_link,keyword,new_vulnerability
     */
    public String getJobCategory();

}
