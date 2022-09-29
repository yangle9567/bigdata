package com.bcld;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * java -jar /opt/log.dat /opt/count.dat
 * 
 * @author MrBright
 * 
 */
public class UrlCount {

    public static void main(String[] args) {
        try {
            // String in = args[0];
            // String out = args[1];
            String in = "D:\\QQDownload\\log.dat";
            String out = "D:\\QQDownload\\logout.dat";

            File inFile = new File(in);
            File outFile = new File(out);

            FileReader fileReader = new FileReader(inFile);

            FileWriter fileWriter = new FileWriter(outFile);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            int allcount = 0;
            int tempCount = 0;
            String tempString = "";
            String temp;
            while ((tempString = bufferedReader.readLine()) != null) {
                allcount++;
                if (tempString.indexOf("开始漏洞扫描任务：") != -1) {
                    temp = tempString.substring(tempString.indexOf("开始漏洞扫描任务："));
                    temp = temp.replaceFirst("开始漏洞扫描任务：", "");
                    bufferedWriter.write(temp + "\n");
                    tempCount += 1;
                }
                if (tempString.indexOf("插入爬取队列") != -1) {
                    temp = tempString.substring(tempString.indexOf("插入爬取队列"));
                    temp = temp.replaceFirst("插入爬取队列", "");
                    bufferedWriter.write(temp + "\n");
                    tempCount += 1;
                }
            }
            bufferedWriter.write("链接总数" + tempCount + "\n");
            bufferedReader.close();
            bufferedWriter.flush();
            bufferedWriter.close();
            System.out.println("一共统计了" + allcount + "行数据,共" + tempCount + "个链接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
