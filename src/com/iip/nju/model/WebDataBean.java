package com.iip.nju.model;

import java.util.Date;

/**
 * Created by xu on 2017/4/30.
 */
public class WebDataBean implements BasicBean {
    private Integer id;
    private String dataSource;
    private String url;
    private String title;
    private Date date;
    private String content;
    private String rawHTML;

    public void setRawHTML(String rawHTML) {
        this.rawHTML = rawHTML;
    }

    public String getRawHTML() {
        return rawHTML;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "WebDataBean{" +
                "id=" + id +
                ", dataSource='" + dataSource + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", content='" + content + '\'' +
                '}';
    }
}
