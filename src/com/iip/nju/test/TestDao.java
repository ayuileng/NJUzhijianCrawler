package com.iip.nju.test;

import com.iip.nju.dao.WebDataDao;
import com.iip.nju.model.WebDataBean;
import org.junit.Test;

/**
 * Created by 63117 on 2017/4/30.
 */
public class TestDao {
    @Test
    public void testWebDataDao() {
        WebDataDao dao = new WebDataDao();
        WebDataBean webDataBean = new WebDataBean();
        webDataBean.setContent("qwe");
        webDataBean.setTitle("hehe");
        dao.saveWebData(webDataBean);

    }
}
