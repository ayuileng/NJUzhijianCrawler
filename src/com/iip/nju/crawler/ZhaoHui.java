package com.iip.nju.crawler;

import com.iip.nju.dao.WebDataDao;
import com.iip.nju.model.WebDataBean;
import com.iip.nju.util.AttachmentUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xu on 2017/5/2.
 * http://www.dpac.gov.cn/xfpzh/
 * http://www.dpac.gov.cn/qczh/
 */
public class ZhaoHui implements Crawler {
    private static final Logger logger = LoggerFactory.getLogger(ZhaoHui.class);
    private String[] baseUrls = {"http://www.dpac.gov.cn/xfpzh/", "http://www.dpac.gov.cn/qczh/"};
    private WebDataDao dao = new WebDataDao();

    /**
     * 抓取完整的页面版块(“更多版块”)
     *
     * @return
     * @throws IOException
     */
    private Set<String> getAllPages() throws IOException {
        Set<String> allPages = new HashSet<>();
        for (String baseUrl : baseUrls) {
            Document document = Jsoup.connect(baseUrl)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            Elements divs = document.select("div.titb");
            for (Element div : divs) {
                allPages.add(div.select("a[href]").first().attr("abs:href"));
            }
        }
        return allPages;
    }

    /**
     * 抓取每一个页面的完整分页
     *
     * @return
     */
    private Set<String> getAllPagesUrls() throws IOException, InterruptedException {
        Set<String> allPages = getAllPages();
        Set<String> allPagesUrls = new HashSet<>();
        for (String allPage : allPages) {

            if (allPage.contains("xfyj")) {
                continue;
            }
            allPagesUrls.add(allPage);
            String html = AttachmentUtil.getHtmlAfterJsExcuted(allPage, logger);
            Document document = Jsoup.parse(html, allPage);
            Element pageDiv = document.select("div.page").first();
            String text = pageDiv.text();
            String pageNum = text.substring(text.indexOf("共") + 1, text.indexOf("页"));
            int num = Integer.parseInt(pageNum);
            //从第二页开始
            for (int i = 1; i < num; i++) {
                allPagesUrls.add(allPage + "index_" + i + ".html");
            }
        }
        return allPagesUrls;
    }

    /**
     * 获得所有要抓取的页面
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private Set<String> getAllUrls() throws IOException, InterruptedException {
        Set<String> allPagesUrls = getAllPagesUrls();
        Set<String> allUrls = new HashSet<>();
        for (String allPagesUrl : allPagesUrls) {
            Document document = Jsoup.connect(allPagesUrl)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            Element div = document.select("div.boxl_ul").first();
            Elements as = div.select("li > a[href]");
            for (Element a : as) {
                allUrls.add(a.attr("abs:href"));
            }

        }
        return allUrls;
    }

    /**
     * 抓取每一个url
     */
    private void prcess_each_url() throws IOException, InterruptedException {
        Set<String> allUrls = getAllUrls();
        for (String Url : allUrls) {
            WebDataBean webdata = new WebDataBean();
            webdata.setUrl(Url);
            Document document = Jsoup.connect(Url)
                    .userAgent("Mozilla")
                    .timeout(0)
                    .get();
            webdata.setRawHTML(document.html());
            webdata.setDate(new Date());
            webdata.setDataSource("国家质量总局缺陷产品管理中心");
            Element title = document.select("div.show_tit").first();
            webdata.setTitle(title.text());
            Element text = document.select("div.TRS_Editor").first();
            if (text == null) {
                webdata.setContent(document.text());
            } else {
                webdata.setContent(text.text());
            }
            dao.saveWebData(webdata);
        }
    }

    @Override
    public void runCrawler() {
        try {
            prcess_each_url();
        } catch (Exception e) {
            logger.error("save ZhaoHui webdata failed!", e);
        }
    }

    @Test
    public void test() throws IOException, InterruptedException {
        runCrawler();
    }

    @Test
    public void test2() throws IOException, InterruptedException {
        String url = "http://www.dpac.gov.cn/xfpzh/xfpgnzh/201704/t20170405_68773.html";

        Document document = Jsoup.connect(url)
                .userAgent("Mozilla")
                .timeout(0)
                .get();
        String title = document.title();
        System.out.println(title);
    }
}
