package com.iip.nju.crawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.iip.nju.dao.WebDataDao;
import com.iip.nju.model.WebDataBean;
import com.iip.nju.util.AttachmentUtil;
import com.iip.nju.util.DownLoadUtil;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
 * 江苏质监局爬虫
 * http://www.jsqts.gov.cn/zjxx/GovInfoPub/Department/moreinfo.aspx?categoryNum=001010
 * Created by xu on 2017/4/30.
 */
public class JiangSu implements Crawler {
    private static final Logger logger = LoggerFactory.getLogger(JiangSu.class);
    private static WebDataDao dao = new WebDataDao();
    private static String baseURL = "http://www.jsqts.gov.cn/zjxx/GovInfoPub/Department/moreinfo.aspx?categoryNum=001010";

    @Override
    public void runCrawler() {
        Map<String, Set<String>> downUrls = null;
        try {
            downUrls = process_each_url();
            String destinationDirectory = "./attachment/jiangsu";
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
        } catch (Exception e) {
            logger.error("jiangsu crawler failed", e);
        }

    }

    /**
     * 获取总的页数(需要等待js加载完毕，因为网页使用的是asp.net的pager插件)
     *
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private String getPageNum() throws InterruptedException, IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        //设置webClient的相关参数
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setTimeout(50000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        //模拟浏览器打开一个目标网址
        HtmlPage rootPage = webClient.getPage(baseURL);
        logger.info("为了获取js执行的数据 线程开始沉睡等待");
        Thread.sleep(1000);//主要是这个线程的等待 因为js加载也是需要时间的
        logger.info("线程结束沉睡");
        String xml = rootPage.asXml();
        Document document = Jsoup.parse(xml);
        Elements as = document.select("a[title]");
        for (Element a : as) {
            if ("尾页".equals(a.text())) {
                String tmp = a.attr("title");
                return tmp.substring(tmp.indexOf("第") + 1, tmp.indexOf("页"));
            }
        }
        return null;
    }

    /**
     * 获取所有的页面
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private ArrayList<Document> getAllPages() throws IOException, InterruptedException {
        ArrayList<Document> docs = new ArrayList<>();
        Integer i = Integer.parseInt(getPageNum());
        for (Integer i1 = 1; i1 <= i; i1++) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost post = new HttpPost(baseURL);
            //提交post表单
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("__EVENTTARGET", "MoreinfoList1$Pager"));
            params.add(new BasicNameValuePair("__EVENTARGUMENT", i1.toString()));//页数 这一项一定要设置
            //设置hhtp请求头部信息
            post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
            post.addHeader("Cookie", "yunsuo_session_verify=81cc7e6afbf54879a4bf96ada22dcc70; ASP.NET_SessionId=jakux245qcgqdr45vzq2sf55");
            post.addHeader("Upgrade-Insecure-Requests", "1");
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpclient.execute(post);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            Document document = Jsoup.parse(result, "http://www.jsqts.gov.cn/zjxx/GovInfoPub/Department/");
            docs.add(document);
        }
        return docs;
    }

    private Set<String> getAllUrl() throws IOException, InterruptedException {
        Set<String> allUrl = new HashSet<>();
        ArrayList<Document> allPages = getAllPages();
        for (Document allPage : allPages) {
            Elements as = allPage.select("a[onmouseout]");
            for (Element a : as) {
                allUrl.add(a.attr("abs:href"));
            }
        }
        return allUrl;
    }

    private Map<String, Set<String>> process_each_url() throws IOException, InterruptedException {
        Set<String> allUrl = getAllUrl();
        Map<String, Set<String>> downloadURLs = new HashMap<>();
        downloadURLs.put("xls", new HashSet<>());
        downloadURLs.put("xlsx", new HashSet<>());
        downloadURLs.put("doc", new HashSet<>());
        downloadURLs.put("docx", new HashSet<>());
        downloadURLs.put("rar", new HashSet<>());
        downloadURLs.put("zip", new HashSet<>());
        downloadURLs.put("pdf", new HashSet<>());
        for (String s : allUrl) {
            WebDataBean webdata = new WebDataBean();
            Document doc = Jsoup.connect(s)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();

            String title = doc.select("span#InfoDetail1_lblTitle").first().text();
            webdata.setTitle(title);
            webdata.setDate(new Date());
            webdata.setDataSource("江苏质监局");
            webdata.setUrl(s);
            Elements as = doc.select("iframe#navFrameContent");
            for (Element a : as) {
                Document document = Jsoup.connect("http://www.jsqts.gov.cn" + a.attr("src"))
                        .userAgent("Mozilla")
                        .timeout(0)
                        .get();
                webdata.setRawHTML(document.html());
                webdata.setContent(doc.text());
                dao.saveWebData(webdata);
                AttachmentUtil.downloadURLs(document, downloadURLs);
                Thread.sleep(1000);
            }
        }
        return downloadURLs;
    }

    @Test
    public void test() throws IOException, InterruptedException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        //设置webClient的相关参数
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setTimeout(50000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        //模拟浏览器打开一个目标网址
        HtmlPage rootPage = webClient.getPage(baseURL);
        logger.info("为了获取js执行的数据 线程开始沉睡等待");
        Thread.sleep(1000);//主要是这个线程的等待 因为js加载也是需要时间的
        logger.info("线程结束沉睡");
        String xml = rootPage.asXml();
        Document document = Jsoup.parse(xml);
        Elements as = document.select("a[title]");
        for (Element a : as) {
            if ("尾页".equals(a.text())) {
                String tmp = a.attr("title");
                String page = tmp.substring(tmp.indexOf("第") + 1, tmp.indexOf("页"));
                System.out.println(page);
            }
        }

    }

    @Test
    public void test1() throws IOException, InterruptedException {

        runCrawler();
    }

    @Test
    public void test2() throws IOException {
        String url = "http://www.jsqts.gov.cn/zjxx/GovInfoPub/Department/moreinfo.aspx?categoryNum=001010";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);

        //提交post表单
        List<NameValuePair> params = new ArrayList<>();
//        params.add(new BasicNameValuePair("__VIEWSTATE", ""));
        params.add(new BasicNameValuePair("__EVENTTARGET", "MoreinfoList1$Pager"));
        params.add(new BasicNameValuePair("__EVENTARGUMENT", "1"));//页数
//        params.add(new BasicNameValuePair("__VIEWSTATEENCRYPTED", ""));
//        params.add(new BasicNameValuePair("MoreinfoList1$syh", ""));
//        params.add(new BasicNameValuePair("MoreinfoList1$xxname", ""));
//        params.add(new BasicNameValuePair("MoreinfoList1$year", "请选择"));
//        params.add(new BasicNameValuePair("MoreinfoList1$select1", "请选择"));

        //设置hhtp请求头部信息
        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
        post.addHeader("Cookie", "yunsuo_session_verify=81cc7e6afbf54879a4bf96ada22dcc70; ASP.NET_SessionId=jakux245qcgqdr45vzq2sf55");
        post.addHeader("Upgrade-Insecure-Requests", "1");

        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = httpclient.execute(post);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
        Document document = Jsoup.parse(result);
        Elements as = document.select("a[herf]");
        for (Element a : as) {
            System.out.println(a.text());
        }
        //  System.out.println(document.html());

    }
}
