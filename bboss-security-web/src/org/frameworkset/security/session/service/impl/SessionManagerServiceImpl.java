package org.frameworkset.security.session.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.frameworkset.security.session.entity.SessionCondition;
import org.frameworkset.security.session.entity.SessionInfoBean;
import org.frameworkset.security.session.impl.SessionHelper;
import org.frameworkset.security.session.service.SessionManagerService;
import org.frameworkset.security.session.statics.SessionAPP;
import org.frameworkset.security.session.statics.SessionConfig;
import org.frameworkset.security.session.statics.SessionInfo;

import com.frameworkset.util.ListInfo;
import com.frameworkset.util.SimpleStringUtil;
import com.frameworkset.util.StringUtil;

public class SessionManagerServiceImpl implements SessionManagerService {

	@Override
	public ListInfo querySessionDataForPage(SessionConfig config,SessionCondition condition,
			int offset, int pagesize) throws Exception {

		ListInfo list = null;
		try {

			Map<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("appKey", condition.getAppkey());
			queryParams.put("sessionid", condition.getSessionid());
			queryParams
					.put("createtime_start", condition.getCreatetime_start());
			queryParams.put("createtime_end", condition.getCreatetime_end());
			queryParams.put("host", condition.getHost());
			queryParams.put("referip", condition.getReferip());
			if(condition.getExtendAttributes() != null)
				queryParams.put("extendAttributes", condition.getExtendAttributes());
			if (!StringUtil.isEmpty(condition.getValidate())) {

				queryParams.put("validate",
						condition.getValidate().equals("1") ? "true" : "false");
			}
			List<SessionInfo> infoList = SessionHelper
					.getSessionStaticManager().getAllSessionInfos(   config, queryParams,
							pagesize, offset);
			
			List<SessionInfoBean> beanList = null;
			if (infoList != null && infoList.size() != 0) {
				beanList = new ArrayList<SessionInfoBean>();
				list = new ListInfo();
				long ctime = System.currentTimeMillis();
				for (SessionInfo info : infoList) {
					SessionInfoBean bean = new SessionInfoBean();
					bean.setAppKey(info.getAppKey());
					bean.setAttributes(info.getAttributes());
					bean.setCreationTime(info.getCreationTime());
					bean.setHost(info.getHost());
					bean.setLastAccessedTime(info.getLastAccessedTime());
					bean.setLongMaxInactiveInterval(info.getMaxInactiveInterval());
					if(info
							.getMaxInactiveInterval() > 0)
					{
						bean.setMaxInactiveInterval(SimpleStringUtil.formatTimeToString(info
								.getMaxInactiveInterval()));
						GregorianCalendar gc = new GregorianCalendar();
						gc.setTime(info.getLastAccessedTime());
						gc.add(Calendar.MILLISECOND,
								(int) info.getMaxInactiveInterval());

						bean.setLoseTime(gc.getTime());
						if(bean.getLoseTime().getTime() < ctime)
						{
							bean.setValidate(false);
						}
						else
							bean.setValidate(info.isValidate());

					}
					else
					{						
						bean.setValidate(info.isValidate());
						if(info.isValidate())
							bean.setMaxInactiveInterval("永不过期（注销session时才会失效）");
						else
							bean.setMaxInactiveInterval("已过期");
					}
					bean.setReferip(info.getReferip());
					bean.setSessionid(info.getSessionid());
					
					
					bean.setRequesturi(info.getRequesturi());
					bean.setLastAccessedUrl(info.getLastAccessedUrl());
					bean.setSecure(info.isSecure());
					bean.setHttpOnly(info.isHttpOnly());
					bean.setLastAccessedHostIP(info.getLastAccessedHostIP());
					bean.setExtendAttributes(info.getExtendAttributes());
					beanList.add(bean);
				}
				
			}
			if(list != null)
			{
				list.setMore(true);
				list.setResultSize(beanList.size());
				list.setDatas(beanList);
			}
			
		} catch (Exception e) {
			throw e;
		}
		return list;
	}

	@Override
	public List<SessionAPP> queryAppSessionData(String appKey,HttpServletRequest request) {

		return SessionHelper.getSessionStaticManager().getSessionAPP(request);
	}
	
	@Override
	public SessionAPP getSingleSessionAPP(HttpServletRequest request) {

		return SessionHelper.getSessionStaticManager().getSingleSessionAPP(request);
	}
	
	@Override
	public SessionAPP getSingleSessionAPP(String appKey) {

		return SessionHelper.getSessionStaticManager().getSingleSessionAPP(appKey);
	}

	@Override
	public void delSession(String appkey, String[] sessionids)  throws Exception{
		try {

			

			for (String sessionid : sessionids) {

				if (StringUtil.isNotEmpty(sessionid)) {

					SessionHelper.getSessionStaticManager().removeSessionInfo(
							appkey, sessionid);
				}
			}
		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	public SessionInfoBean getSessionInfo(String appkey, String sessionid)  throws Exception{
		try {

			SessionInfo info = SessionHelper.getSessionStaticManager()
					.getSessionInfo(appkey, sessionid);

			SessionInfoBean bean = new SessionInfoBean();
			bean.setAppKey(info.getAppKey());
			bean.setAttributes(info.getAttributes());
			bean.setCreationTime(info.getCreationTime());
			bean.setHost(info.getHost());
			bean.setLastAccessedTime(info.getLastAccessedTime());
			

			bean.setReferip(info.getReferip());
			bean.setSessionid(info.getSessionid());
			
			bean.setRequesturi(info.getRequesturi());
			bean.setLastAccessedUrl(info.getLastAccessedUrl());
			
			bean.setSecure(info.isSecure());
			bean.setHttpOnly(info.isHttpOnly());
			
			bean.setLongMaxInactiveInterval(info.getMaxInactiveInterval());
			if(info
					.getMaxInactiveInterval() > 0)
			{
				bean.setMaxInactiveInterval(SimpleStringUtil.formatTimeToString(info
						.getMaxInactiveInterval()));
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(info.getLastAccessedTime());
				gc.add(Calendar.MILLISECOND,
						(int) info.getMaxInactiveInterval());

				bean.setLoseTime(gc.getTime());
				if(bean.getLoseTime().getTime() < System.currentTimeMillis())
				{
					bean.setValidate(false);
				}
				else
					bean.setValidate(info.isValidate());

			}
			else
			{						
				bean.setValidate(info.isValidate());
				if(info.isValidate())
					bean.setMaxInactiveInterval("永不过期（注销session时才会失效）");
				else
					bean.setMaxInactiveInterval("已过期");
			}
			bean.setLastAccessedHostIP(info.getLastAccessedHostIP());

			return bean;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void delAllSessions(String appkey,String currentappkey,String currentsessionid) throws Exception {
		try {
			SessionHelper.getSessionStaticManager().removeAllSession(appkey,  currentappkey,currentsessionid);
		} catch (Exception e) {
			throw e;
		}
	}
	
	

	
}
