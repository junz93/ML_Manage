package com.common.subscribe;

import java.util.ArrayList;
import javax.xml.ws.Endpoint;
import org.apache.servicemix.wsn.push.NotificationProcessImpl;
import wsn.wsnclient.command.SendWSNCommand;

public class singleSubscribe {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Starting Server");
		NotificationProcessImpl implementor = new NotificationProcessImpl();
		Endpoint.publish(args[0], implementor);
		System.out.println("Server start!");
		
		String[] topic = new String[]{"topic1", "topic2"};
		ArrayList<SendWSNCommand> sendWSNCommandList = new ArrayList<SendWSNCommand>();
		for(int i=0;i<topic.length;i++){
			SendWSNCommand sendWSNCommand = new SendWSNCommand(args[0], args[1]);
			sendWSNCommandList.add(sendWSNCommand);
		}	
		
		String str1 = null;
		String str2 = null;
		
		for(int i=0;i<topic.length;i++){
			try{
				str1 = sendWSNCommandList.get(i).createPullPoint();
				if(str1 == "ok")
					System.out.println(topic[i] + "  success in creatPullPoint~~");
				else
					System.out.println(topic[i] + "  failed in creatPullPoint~~");
				
				str2 = sendWSNCommandList.get(i).subscribe(topic[i]);
				if(str2 == "ok")
					System.out.println(topic[i] + "  success in subscribe~~");
				else
					System.out.println(topic[i] + "  failed in subscribe~~");
			}catch (Exception e) {
				// TODO Auto-generated c`atch block
				e.printStackTrace();	
			}
		}
	}
}
