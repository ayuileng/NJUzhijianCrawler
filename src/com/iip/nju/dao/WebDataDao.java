package com.iip.nju.dao;

import com.iip.nju.model.WebDataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xu on 2017/4/30.
 */
public class WebDataDao extends DAO {
    private static final Logger logger = LoggerFactory.getLogger(WebDataDao.class);

    public void saveWebData(WebDataBean webData) {
        try{
            begin();
            getSession().save(webData);
            commit();
            logger.info("save success!");
        }catch(Exception e) {
            rollback();
            logger.error("save failed!",e);
        }
    }

//    public List<WebDataBean> getWebData() {
//        try{
//            begin();
//            Query q = getSession().createQuery("from ");
//            List<WebDataBean> list = q.list();
//            logger.info("size="+list.size());
//            commit();
//            return list;
//        }catch(Exception e) {
//            rollback();
//            logger.error("getWebData failed!",e);
//        }
//        return null;
//    }
}
