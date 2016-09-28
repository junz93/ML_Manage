package com.common.subscribe;

import java.util.ArrayList;
import javax.xml.ws.Endpoint;
import org.apache.servicemix.wsn.push.Impl;
import wsn.wsnclient.command.SendWSNCommand;

public class Subscribe {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		for(int i=0;i<args.length;++i){
//			System.out.println(args[i]);
//		}
		
		String subWebSAddr =  "http://10.8.176.65:9012/wsn-subscribe";//�����û�webservice��ַ ip���뱾��
		String wsnAddr = "http://10.108.166.15:9000/wsn-core";//�������Ľڵ��ַ
		System.out.println("Starting Server");
		Impl implementor = new Impl();//��Ϣ�����߼�
		Endpoint endpint = Endpoint.publish(subWebSAddr, implementor);//�������շ���
//		endpint.stop();
		System.out.println("Server start!");
		
//		new MinaServer(8081);
		
		String[] topic = new String[]{"all:DCCP-4_down"};
		//String[] topic = new String[]{"all:DCCP-4_up","all:DCCP-4_down"};
		
		SendWSNCommand sendWSNCommand = new SendWSNCommand(subWebSAddr, wsnAddr);
		
		String str2 = null;
		
		for(int i=0;i<topic.length;i++)
		{
			try
			{		
				str2 = sendWSNCommand.subscribe(topic[i]);
				if(str2.equals("ok"))
					System.out.println(topic[i] + "  success in subscribe~~");
				else
					System.out.println(topic[i] + "  failed in subscribe~~");
			}
			catch (Exception e) 
			{
				// TODO Auto-generated c`atch block
				e.printStackTrace();	
			}
		}
	}
}
