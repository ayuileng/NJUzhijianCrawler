package com.iip.nju.crawler;

import com.iip.nju.dao.WebDataDao;
import com.iip.nju.model.WebDataBean;
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
import java.util.*;

/**
 * 国家质量监督检验检疫总局
 * http://www.aqsiq.gov.cn/xxgk_13386/jlgg_12538/ccgg/
 * http://www.aqsiq.gov.cn/zjsj/zhxx/
 * Created by xu on 2017/5/3.
 */
public class GJZLJDJYJYZJ implements Crawler {
    private static WebDataDao dao = new WebDataDao();
    private static final Logger logger = LoggerFactory.getLogger(GJZLJDJYJYZJ.class);
    private static String[] baseURLS = {"http://www.aqsiq.gov.cn/xxgk_13386/jlgg_12538/ccgg/",
            "http://www.aqsiq.gov.cn/zjsj/zhxx/"};

    //todo 过滤词需要从配置文件读
    private static String[] fliterwords;

    /**
     * 获取所有的子版块的页面url
     *
     * @return
     */
    private Set<String> getAllPages() throws IOException, InterruptedException {
        Set<String> allPages = new HashSet<>();
        for (String baseURL : baseURLS) {
            Document doc = Jsoup.connect(baseURL)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            Elements tds = doc.select("td.more");
            for (Element td : tds) {
                String url = td.select("a[href]").first().attr("abs:href");
                allPages.add(url);
            }
//            Thread.sleep(1000);

        }
        return allPages;
    }

    private Set<String> getPageUrls() throws IOException, InterruptedException {
        Set<String> pageUrls = new HashSet<>();
        Set<String> allPages = getAllPages();
        for (String allPage : allPages) {
            Document document = Jsoup.connect(allPage)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            //选择td标签下的带href属性的a标签
            Elements as = document.select("td > a[href]");
            for (Element a : as) {
                pageUrls.add(a.attr("abs:href"));
            }
//            Thread.sleep(1000);
        }
        return pageUrls;
    }

    private Map<String, Set<String>> process_each_url() throws IOException, InterruptedException {
//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "1080");
        Set<String> pageUrls = getPageUrls();
        Map<String, Set<String>> downloadURLs = new HashMap<>();
        downloadURLs.put("xls", new HashSet<>());
        downloadURLs.put("xlsx", new HashSet<>());
        downloadURLs.put("doc", new HashSet<>());
        downloadURLs.put("docx", new HashSet<>());
        downloadURLs.put("rar", new HashSet<>());
        downloadURLs.put("zip", new HashSet<>());
        downloadURLs.put("pdf", new HashSet<>());
        for (String pageUrl : pageUrls) {
            WebDataBean webdata = new WebDataBean();
            Document document = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            webdata.setUrl(pageUrl);
            webdata.setRawHTML(document.html());
            webdata.setDate(new Date());
            webdata.setDataSource("国家质量监督检验检疫总局");
            Element title = document.select("h1").first();
            if (title != null) {
                webdata.setTitle(title.text());
            }
            Element content = document.select("div.TRS_Editor").first();
            if (content != null) {
                webdata.setContent(content.text());
            }
            dao.saveWebData(webdata);
            AttachmentUtil.downloadURLs(document, downloadURLs);
            Thread.sleep(1000);

        }
        return downloadURLs;
    }

    @Override
    public void runCrawler() {
        try {
            Map<String, Set<String>> downUrls = process_each_url();
            String destinationDirectory = "./attachment/GJZLJDJYJYZJ";
            //分类保存（便于之后读取，因为07前的版本和之后的不一样）（丑）
            Set<String> doc = downUrls.get("doc");
            for (String s : doc) {
                DownLoadUtil.download(s, destinationDirectory + "/doc/");
            }
            Set<String> docx = downUrls.get("docx");
            for (String s : docx) {
                DownLoadUtil.download(s, destinationDirectory + "/docx/");
            }
            Set<String> xls = downUrls.get("xls");
            for (String s : xls) {
                DownLoadUtil.download(s, destinationDirectory + "/xls/");
            }
            Set<String> xlsx = downUrls.get("xlsx");
            for (String s : xlsx) {
                DownLoadUtil.download(s, destinationDirectory + "/xlsx/");
            }
            Set<String> rar = downUrls.get("rar");
            for (String s : rar) {
                DownLoadUtil.download(s, destinationDirectory + "/rar/");
            }
            Set<String> zip = downUrls.get("zip");
            for (String s : zip) {
                DownLoadUtil.download(s, destinationDirectory + "/zip/");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test1() throws IOException, InterruptedException {
        runCrawler();
    }

}

