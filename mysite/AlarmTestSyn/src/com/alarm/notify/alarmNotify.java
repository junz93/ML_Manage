package com.alarm.notify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wsn.wsnclient.command.SendWSNCommand;
import wsn.wsnclient.command.SendWSNCommandWSAsyn;
import wsn.wsnclient.command.SendWSNCommandWSSyn;

public class alarmNotify 
{
	private static String localIP;
	private static String webserviceAddr = "http://10.8.174.242:9002/wsn-core-subscriber";
	private static String wsnAddr = "http://10.108.166.15:9000/wsn-core";
	private static int counter = 0;
	private static int totle = 0;
	private static boolean peak = false;
	private static int timePeriod = 0;
	private String[] args;

	// static final SendWSNCommandWSSyn command = new SendWSNCommandWSSyn(
	// webserviceAddr, wsnAddr);

	/**
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws UnknownHostException 
	{
		// webserviceAddr = args[0];
		System.out.println(webserviceAddr);
		// wsnAddr = args[1];
		System.out.println(wsnAddr);
		// totle = Integer.parseInt(args[2]);
		System.out.println(totle);
		// peak = Boolean.valueOf(args[3]);
		// timePeriod = Integer.parseInt(args[4]);
		System.out.println(timePeriod);
		System.out.println(peak);
		alarmNotify an = new alarmNotify();
		an.args=args;
		an.Notify();

	}

	public void Notify() 
	{
		// final SendWSNCommandWSAsyn command = new
		// SendWSNCommandWSAsyn(webserviceAddr, wsnAddr);
		final SendWSNCommandWSSyn command = new SendWSNCommandWSSyn(
				webserviceAddr, wsnAddr);
		File file = new File(args[0]);// ��ȡ���������ļ�

		BufferedReader reader = null;

		String s = null;
		
		while(true)
		{
			try 
			{
				reader = new BufferedReader(new FileReader(file));

				while((s = reader.readLine()) != null)
				{
					command.reliableNotify("all:DCCP-4_up", s, false, "A");
					counter++;
					System.out.println(counter+": "+s);
					if(counter % 100 == 0)
						System.out.println(System.currentTimeMillis() + " : һ"
								+ counter);
					try 
					{
						Thread.sleep(30);
					} 
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					
				}
			} 
			catch (FileNotFoundException e1)
			{
				
				e1.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			try
			{
				reader.close();
			} 
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
