package com.common.subscribe;

import java.util.ArrayList;
import javax.xml.ws.Endpoint;

import org.apache.servicemix.wsn.push.NotificationProcessImpl;
import wsn.wsnclient.command.SendWSNCommand;
import wsn.wsnclient.command.SendWSNCommandWSSyn;

public class singleSubscribe 
{
	/**
	 * @param args
	 */
	public static String modelId;
	public static String topicPub;
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
//		for(int i=0;i<args.length;++i){
//			System.out.println(args[i]);
//		}
		
		String subWebSAddr =  "http://10.8.174.242:9016/wsn-subscribe";//�����û�webservice��ַ ip���뱾��
		String wsnAddr = "http://10.108.166.15:9000/wsn-core";//�������Ľڵ��ַ
		System.out.println("Starting Server");	
		NotificationProcessImpl implementor = new NotificationProcessImpl();//��Ϣ�����߼�
		Endpoint endpint = Endpoint.publish(subWebSAddr, implementor);//�������շ���
//		endpint.stop();
		System.out.println("Server start!");
		modelId = args[0];
		topicPub = args[2];
		String[] topic = new String[]{args[1]};
		//String[] topic = new String[]{"all:DCCP-4_up","all:DCCP-4_down"};
		
		SendWSNCommand sendWSNCommand = new SendWSNCommand(subWebSAddr, wsnAddr);
		
		String str2 = null;
		
		for(int i=0;i<topic.length;i++)
		{
			try
			{		
				str2 = sendWSNCommand.subscribe(topic[i]);
				if(str2 == "ok")
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




