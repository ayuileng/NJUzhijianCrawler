package com.iip.nju.crawler;

/**
 * Created by xu on 2017/4/30.
 */


import com.iip.nju.util.AttachmentUtil;
import com.iip.nju.util.DownLoadUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 爬取苏州质检网http://www.szqts.gov.cn/zhiliangchoucha.html
 * 的附件（本页面只需要爬附件）
 */
public class Suzhou implements Crawler {


    private static final Logger logger = LoggerFactory.getLogger(Suzhou.class);
    private static String baseURL = "http://www.szqts.gov.cn/zhiliangchoucha.html";

    /**
     * 读取所有页面
     *
     * @return
     * @throws IOException
     */
    private Set<String> baseURLS() throws IOException {
        Document doc = Jsoup.connect(baseURL)
                .userAgent("Mozilla")
                .timeout(0)
                .get();
        Element page = doc.select("#page").first();
        Elements pages = page.select("a[href]");
        Set<String> baseURLS = new HashSet<>();
        baseURLS.add(baseURL);
        for (Element element : pages) {
            baseURLS.add(element.attr("href"));
        }
        return baseURLS;
    }

    /**
     * 获取所有子页面的url
     *
     * @return
     * @throws IOException
     */
    private Set<String> pageURLs() throws IOException {
        Set<String> baseURLS = baseURLS();
        Set<String> urls = new HashSet<>();
        for (String url : baseURLS) {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            Elements lis = doc.select("li.list_li");
            for (Element li :
                    lis) {
                urls.add(li.select("a").attr("href"));
            }
        }
        return urls;
    }

    /**
     * 从每个单独的页面中提取出要下载的附件的url
     */
    private Map<String, Set<String>> downloadURLs() throws IOException, InterruptedException {
        Set<String> pageURLs = pageURLs();
        Map<String, Set<String>> downloadURLs = new HashMap<>();

        downloadURLs.put("xls", new HashSet<>());
        downloadURLs.put("xlsx", new HashSet<>());
        downloadURLs.put("doc", new HashSet<>());
        downloadURLs.put("docx", new HashSet<>());
        for (String url : pageURLs) {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            AttachmentUtil.downloadURLs(document,downloadURLs);
        }

        return downloadURLs;
    }

    /**
     * 下载附件
     */
    private void downloadAttachment() throws Exception {
        Map<String, Set<String>> stringSetMap = downloadURLs();
        String destinationDirectory = "./attachment/suzhou";
        //分类保存（便于之后读取，因为07前的版本和之后的不一样）（丑）
        Set<String> doc = stringSetMap.get("doc");
        for (String s : doc) {
            DownLoadUtil.download(s, destinationDirectory + "/doc/");
        }
        Set<String> docx = stringSetMap.get("docx");
        for (String s : docx) {
            DownLoadUtil.download(s, destinationDirectory + "/docx/");
        }
        Set<String> xls = stringSetMap.get("xls");
        for (String s : xls) {
            DownLoadUtil.download(s, destinationDirectory + "/xls/");
        }
        Set<String> xlsx = stringSetMap.get("xlsx");
        for (String s : xlsx) {
            DownLoadUtil.download(s, destinationDirectory + "/xlsx/");
        }

    }

    @Override
    public void runCrawler() {
        try {
            downloadAttachment();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("苏州质监局附件下载错误");
        }
    }

    @Test
    public void test() throws IOException, InterruptedException {
        Map<String, Set<String>> stringSetMap = downloadURLs();
        int i = 0;
        for (Set<String> strings : stringSetMap.values()) {
            for (String string : strings) {
                System.out.println(string);
            }
            i += strings.size();
        }
        System.out.println(i);
    }

    @Test
    public void test2() throws IOException {
        String url = "http://www.szqts.gov.cn/zhiliangchoucha/c606c23a7debc9a163eaf00739f95c5c.html";
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla")
                .timeout(5000)
                .get();
        Elements select = doc.select("a[href$=.xls]");
        for (Element element : select) {
            System.out.println(element.text());
        }
    }

    @Test
    public void test3() throws IOException, InterruptedException {
        runCrawler();
    }
}
