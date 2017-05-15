package com.iip.nju.dao;

import com.iip.nju.model.AttachmentBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xu on 2017/5/15.
 */
public class AttachmentDao extends DAO {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentDao.class);
    public void saveAttachment(AttachmentBean attachment) {
        try{
            begin();
            getSession().save(attachment);
            commit();
            logger.info("save success!");
        }catch(Exception e) {
            rollback();
            logger.error("save failed!",e);
        }
    }
}
