package org.apache.servicemix.wsn.push;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import javax.jws.WebService;

@WebService(endpointInterface="org.apache.servicemix.wsn.push.INotificationProcess",
		serviceName="INotificationProcess")
public class Impl implements INotificationProcess{
	private static int counter = 0;
	private byte[] buf = new byte[1024];
	private URLConnection connection = null;
	private InputStream in = null;
	public  void notificationProcess(String notification) 
	{	
		System.out.println(notification);
		
		counter++;
		
		if(counter%100 == 0)
			System.out.println(System.currentTimeMillis()+ "   counter:"+counter);	
	}
}