package org.apache.servicemix.wsn.push;

import java.awt.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import wsn.wsnclient.command.NotificationMessageHolderType.Message;
import wsn.wsnclient.command.SendWSNCommandWSSyn;
import com.common.subscribe.singleSubscribe;

@WebService(endpointInterface="org.apache.servicemix.wsn.push.INotificationProcess",
		serviceName="INotificationProcess")
public class NotificationProcessImpl implements INotificationProcess
{
	private static int counter = 0;
	private byte[] buf = new byte[1024];
	private URLConnection connection = null;
	private InputStream in = null;
	private int size = 0;
	private Map<Integer, Data> map = null;
	private static String webserviceAddr = "http://10.8.174.242:9002/wsn-core-subscriber";
	private static String wsnAddr = "http://10.108.166.15:9000/wsn-core";
	public  void notificationProcess(String notification) 
	{
		String id = singleSubscribe.modelId;
		String topicPub = singleSubscribe.topicPub;
		String tmp = notification;
		int tmpCounter = 0;
		notification = notification.trim();
		String[] str = notification.split("\\s+");
		int num = Integer.parseInt(str[0]);
		int begin =-1, end = -1;
		if(str[2].contains("-"))
		{
			begin = Integer.parseInt(str[2].substring(0, str[2].lastIndexOf("-")));
			end = Integer.parseInt(str[2].substring(str[2].lastIndexOf("-")+1,str[2].length()));
		}
		else
		{
			begin = Integer.parseInt(str[2]);
			end = begin;
		}
		if(map == null)
		{
			size = Integer.parseInt(str[1]);
			map = new HashMap<Integer, Data>();
		}
		Data m = null;
		if(!map.containsKey(num))
		{
			m = new Data(size);
			map.put(num, m);
		}
		else
		{
			m = map.get(num);
		}
		for(int i =begin; i<=end; i++)
		{
			m.add(i, str[3+i-begin]);
		}
		if(m.isFull())
		{
			notification = m.getString();
		
			counter++;
			
			System.out.println(counter+": "+num+" "+notification);
			
			tmpCounter = counter;
			
			String addr = "10.8.174.242:8000";
		
			//notification = notification.replaceAll("\\s+",";");
		
			//System.out.println(notification);
		
			try
			{
				URL url = new URL("http://"+addr+"/manage/id/"+id+"/data/"+notification);
				connection = url.openConnection();
				in = connection.getInputStream();	
				int len = in.read(buf);
				String s = new String(buf,0,len);
				int resultNum = s.trim().split("\\s+").length;
				s = num + " " + resultNum + " 0-" + (resultNum-1) + " " + s;
				System.out.println("result "+tmpCounter+": "+s);
				final SendWSNCommandWSSyn command = new SendWSNCommandWSSyn(webserviceAddr, wsnAddr);
				command.reliableNotify(topicPub, s, false, "A");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (in != null) 
				{
					try 
					{
						in.close();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}

class Data
{
	private int counter, size;
	private String[] value;
	
	public Data(int n) 
	{
		size = n;
		value = new String[size];
	}
	
	public void add(int i, String s)
	{
		if(counter != size)
			counter++;
		value[i] = s;
	}
	
	public String getString()
	{
		StringBuilder strb = new StringBuilder();
		for(int i=0; i<size-1 ; i++)
		{
			strb.append(value[i]);
			strb.append(";");
		}
		strb.append(value[size-1]);
		return strb.toString();
	}
	
	public boolean isFull()
	{
		return counter == size;
	}
	
}
