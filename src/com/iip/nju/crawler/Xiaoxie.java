package com.iip.nju.crawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.iip.nju.dao.WebDataDao;
import com.iip.nju.model.WebDataBean;
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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * 消协、315
 * http://www.cca.org.cn/jmxf/list/17.html
 * http://www.cca.org.cn/jmxf/list/16.html(暂时失效)
 * http://www.cca.org.cn/jmxf/list/13.html
 * http://www.cca.org.cn/tsdh/list/19.html
 * http://www.315.gov.cn/spzljd/
 * http://www.315.gov.cn/wqsj/index.html(配置关键字搜索)
 * <p>
 * Created by xu on 2017/5/8.
 */
public class Xiaoxie implements Crawler {
    private static WebDataDao dao = new WebDataDao();
    private static final Logger logger = LoggerFactory.getLogger(Xiaoxie.class);
    private static String[] cca = {"http://www.cca.org.cn/jmxf/list/17.html",
//            "http://www.cca.org.cn/jmxf/list/16.html",
            "http://www.cca.org.cn/jmxf/list/13.html",
            "http://www.cca.org.cn/tsdh/list/19.html"};
    private static String[] sanyaowu = {"http://www.315.gov.cn/spzljd/",
            "http://www.315.gov.cn/wqsj/index.html"};

    /**
     * @return
     * @throws IOException
     */
    private Set<String> getCCAurl() throws IOException {
        Set<String> pageUrls = new HashSet<>();
        Set<String> ccaUrls = new HashSet<>();
        for (String s : cca) {
            Document document = Jsoup.connect(s)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            pageUrls.add(s);
            Element page = document.select("div.page").first();
            Elements as = page.select("a[href]");
            for (Element a : as) {
                pageUrls.add(a.attr("abs:href"));
            }
        }
        for (String pageUrl : pageUrls) {
            Document document = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            Elements uls = document.select("ul.jm_ul");
            for (Element ul : uls) {
                Elements as = ul.select("li > a[href]");
                for (Element a : as) {
                    ccaUrls.add(a.attr("abs:href"));
                }
            }

        }
        return ccaUrls;
    }

    /**
     * @throws IOException
     */
    private void process_cca_urls() throws IOException {
        Set<String> ccAurl = getCCAurl();
        for (String s : ccAurl) {
            WebDataBean webData = new WebDataBean();

            Document document = Jsoup.connect(s)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            String title = document.select("div.title").first().text();
            String content = document.select("div.text_content").first().text();
            webData.setTitle(title);
            webData.setContent(content);
            webData.setRawHTML(document.html());
            webData.setUrl(s);
            webData.setDate(new Date());
            webData.setDataSource("中国消费者协会");
            dao.saveWebData(webData);
        }
    }

    /**
     * @throws IOException
     * @throws InterruptedException
     */
    private void getSanyaowuUrls() throws IOException, InterruptedException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        //设置webClient的相关参数
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setTimeout(50000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        //模拟浏览器打开一个目标网址
        HtmlPage rootPage = webClient.getPage(sanyaowu[0]);
        logger.info("为了获取js执行的数据 线程开始沉睡等待");
        Thread.sleep(1000);//主要是这个线程的等待 因为js加载也是需要时间的
        logger.info("线程结束沉睡");
        String xml = rootPage.asXml();
        Document document = Jsoup.parse(xml, "http://www.315.gov.cn/spzljd/");
        String text = document.select("div.more_more").first().text();
        String pageNum = text.substring(text.indexOf("共") + 1, text.indexOf("页"));//22
        Integer pagenum = Integer.parseInt(pageNum);
        Set<String> allPages = new HashSet<>();
        allPages.add(sanyaowu[0]);
        for (Integer i = 1; i < pagenum; i++) {
            allPages.add("http://www.315.gov.cn/spzljd/index_" + i.toString() + ".html");//http://www.315.gov.cn/spzljd/index_1.html
        }

        for (String allPage : allPages) {
            Document doc = Jsoup.connect(allPage)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            Elements lis = doc.select("li.dot1");
            for (Element li : lis) {
                String url = li.select("a[href]").first().attr("abs:href");
                Document document1 = Jsoup.connect(url)
                        .userAgent("Mozilla")
                        .timeout(0)
                        .get();
                WebDataBean webData = new WebDataBean();
                webData.setUrl(url);
                webData.setDate(new Date());
                webData.setDataSource("中国消费者权益保护网");
                webData.setRawHTML(document1.html());
                String title = "";
                String content = "";
                try {
                    title = document1.select("div.end_tab").first().text();
                    content = document1.select("div.TRS_Editor").first().text();
                } catch (NullPointerException e) {
                }
                webData.setTitle(title);
                webData.setContent(content);
                dao.saveWebData(webData);
            }
        }
    }

    /**
     * 网站检索爬虫
     */
    private void searchData() throws IOException {
        //TODO 搜索的内容需要配置
        Set<String> searchWord = new HashSet<>();
        String keyWord = "玩具";//模拟
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://gzhd.saic.gov.cn/saicsearch/index2.jsp");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("key", URLEncoder.encode(keyWord, "GBK")));
        params.add(new BasicNameValuePair("search_field", "all"));
        params.add(new BasicNameValuePair("database", "xbj"));
        params.add(new BasicNameValuePair("date", ""));
        params.add(new BasicNameValuePair("x", "0"));
        params.add(new BasicNameValuePair("y", "0"));
        params.add(new BasicNameValuePair("end_date", ""));
        params.add(new BasicNameValuePair("title", ""));
        params.add(new BasicNameValuePair("search_type", "yes"));
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addHeader("Cookie", "yoursessionname0=F7167BEAF851903CE72F7BA20D488BA1");
        post.addHeader("Host", "gzhd.saic.gov.cn");
        post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        post.addHeader("Accept-Encoding", "gzip, deflate");
        post.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
        post.addHeader("Connection", "keep-alive");
        post.addHeader("Upgrade-Insecure-Requests", "1");
        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
        post.setEntity(new UrlEncodedFormEntity(params, "GBK"));
        HttpResponse response = httpClient.execute(post);
        String result = EntityUtils.toString(response.getEntity(), "GBK");
        System.out.println(result);


    }


    @Override
    public void runCrawler() {
        try {
            process_cca_urls();
            getSanyaowuUrls();
        } catch (Exception e) {
            logger.error("xiaoxie error", e);
        }

    }

    @Test
    public void test() throws IOException {
        searchData();

    }

    @Test
    public void test1() throws IOException, InterruptedException {
        String keyWord = URLDecoder.decode("%CD%E6%BE%DF", "GBK");
        System.out.println(keyWord);
        String a = URLEncoder.encode("玩具", "GBK");
        System.out.println(a);
    }
}
