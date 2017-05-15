package com.iip.nju.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class DAO {
	
	private static final Logger logger = LoggerFactory.getLogger(DAO.class);
	
	@SuppressWarnings("rawtypes")
	private static final ThreadLocal SESSION_LOCAL = new ThreadLocal();
	
	//private static final SessionFactory sessionFactory = new Configuration().configure(new File("hibernate.cfg.xml")).buildSessionFactory();
	private static final SessionFactory sessionFactory;
	static{
		 Configuration cfg = new Configuration();
		 cfg.configure(new File("C:\\Users\\63117\\IdeaProjects\\zhijianCrawler_4.30\\src\\hibernate.cfg.xml"));
		 ServiceRegistry serviceRegistry =new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();
		 sessionFactory = cfg.buildSessionFactory(serviceRegistry);
	}
	
	protected DAO() {
	}
	
	/**
	 * 取得当前线程的会话
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Session getSession() {
		Session session = (Session)DAO.SESSION_LOCAL.get();
		if(session == null) {
			session = sessionFactory.openSession();
			DAO.SESSION_LOCAL.set(session);
		}
		return session;
	}
	
	protected void begin() {
		getSession().beginTransaction();
	}
	
	protected void commit() {
		getSession().getTransaction().commit();
	}
	
	@SuppressWarnings("unchecked")
	protected void rollback() {
		try {
			getSession().getTransaction().rollback();
		}catch(HibernateException e) {
			logger.error("rollback failed!",e);
		}
		
		try {
			getSession().close();
		}catch(HibernateException e) {
			logger.error("close session failed!",e);
		}
		
		DAO.SESSION_LOCAL.set(null);
	}
	
}
