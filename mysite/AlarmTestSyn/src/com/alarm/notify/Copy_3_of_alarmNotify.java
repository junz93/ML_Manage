package com.alarm.notify;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import wsn.wsnclient.command.SendWSNCommand;
import wsn.wsnclient.command.SendWSNCommandWSSyn;

public class Copy_3_of_alarmNotify {
	private static String localIP;
	private static String webserviceAddr = "http://10.109.253.38:9002/wsn-core-subscriber";
	private static String wsnAddr = "http://10.109.253.38:9000/wsn-core";
	private static int counter = 0;
	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException {

		System.out.println(webserviceAddr);
		System.out.println(wsnAddr);
		Copy_3_of_alarmNotify an = new Copy_3_of_alarmNotify();
		an.Notify();
	}
	public void Notify(){
		SendWSNCommandWSSyn command = new SendWSNCommandWSSyn(webserviceAddr, wsnAddr);	

		
		String mes="<value>30</value><mytopic>GJECHSYL</mytopic>";

		   while(true){
		for(int i=0;i<1000;i++){	
			counter ++;
//			System.out.println(System.currentTimeMillis());				    
//			System.out.println("time:"+new Date());	
		    command.reliableNotify("all:command", mes,true,"A");//发送 （四个参数： 消息主题，消息内容，是否接收非完整包，消息等级（A B C 三级））
		   } 	    
		    try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		 //   if(i>99980){
			System.out.println(System.currentTimeMillis()+"+"+counter + " notify is complete.");
		//    }		
		   }
	}
}