package org.frameworkset.nosql.redis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.DefaultApplicationContext;

public class RedisFactory {

	private static Map<String,RedisDB> dbs = new HashMap<String,RedisDB>();
	private static ThreadLocal<RedisHelperHolder> currentDB = new ThreadLocal<RedisHelperHolder>();
 
	static class RedisHelperHolder 
	{
		private int count;
		public int increament()
		{
			count = count + 1;
			return count;
		}
		
		public int decreament()
		{
			count = count - 1;
			
			return count;
		}
		private boolean allreleased()
		{
			return count <= 0;
		}
		
		public void release()
		{
			 
			 
				if(holderdbs != null)
				{
					Iterator<Entry<String, RedisHelper>>  its = holderdbs.entrySet().iterator();
					while(its.hasNext())
					{
						Entry<String, RedisHelper> entry = its.next();
						try {
							entry.getValue().release();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				this.holderdbs = null;
			 
		}
		
		public RedisHelper getRedisHelper()
		{
			return getRedisHelper("default");
		}
		public RedisHelper getRedisHelper(String dbname)
		{
			RedisHelper RedisHelper = null;
			if(holderdbs == null)
			{
				holderdbs = new HashMap<String,RedisHelper>();
			}
			else
			{
				RedisHelper = holderdbs.get(dbname);
			}
			if(RedisHelper != null)
			{
				 
				return RedisHelper;
			}
			else
			{
				RedisDB db = init(dbname);
				RedisHelper = new RedisHelper(db);
				 
				holderdbs.put(dbname, RedisHelper);
				return RedisHelper;
			}
		}
		private Map<String,RedisHelper> holderdbs = new HashMap<String,RedisHelper>();
	}
	public RedisFactory() {
		
	}
	private static RedisDB init(String dbname)
	{
		RedisDB db = dbs.get(dbname);
		if(db == null)
		{
			synchronized(RedisFactory.class)
			{
				db = dbs.get(dbname);
				if(db == null)
				{
					BaseApplicationContext context = DefaultApplicationContext.getApplicationContext("redis.xml");
					db = context.getTBeanObject(dbname, RedisDB.class);
					dbs.put(dbname, db);
				}
				
			}
		}
		return db;
	}
	
	public static RedisHelper getTXRedisHelper()
	{
		return getTXRedisHelper("default");
	}
	
	public static RedisHelper getTXRedisHelper(String dbname)
	{
		if(dbname == null)
			dbname = "default";
		RedisHelperHolder holder = currentDB.get();
		if(holder == null)
		{
			holder = new RedisHelperHolder();
			currentDB.set(holder);
			
		}
		holder.increament();
		return holder.getRedisHelper(dbname);
//		RedisDB db = init(dbname);
//		return new RedisHelper(db);
	}
	
	public static RedisHelper getRedisHelper()
	{
		return getRedisHelper("default");
	}
	
	public static RedisHelper getRedisHelper(String dbname)
	{
		 
		RedisDB db = init(dbname);
		return new RedisHelper(db);
	}
	public static void releaseTX()
	{
		RedisHelperHolder holder = currentDB.get();
		if(holder != null)
		{
			holder.decreament();
			if(holder.allreleased())
			{
				currentDB.set(null);
				holder.release();
			}
		}
	}

}
