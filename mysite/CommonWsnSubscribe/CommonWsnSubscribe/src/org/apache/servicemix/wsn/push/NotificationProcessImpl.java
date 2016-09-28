package org.apache.servicemix.wsn.push;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jws.WebService;

@WebService(endpointInterface="org.apache.servicemix.wsn.push.INotificationProcess",
		serviceName="INotificationProcess")
public class NotificationProcessImpl implements INotificationProcess{
	private static int counter = 0;
	public  void notificationProcess(String notification) {
		counter ++;
		int startOfChineseCharacter = notification.indexOf("<common:attribute-chineseCharacter>") + 
								"<common:attribute-chineseCharacter>".length();
		int endOfChineseCharacter = notification.indexOf("</common:attribute-chineseCharacter>");
		String chineseCharacter = notification.substring(startOfChineseCharacter, endOfChineseCharacter);
		
		int startOfRandomNumber = notification.indexOf("<common:attribute-randomNumber>") + 
								"<common:attribute-randomNumber>".length();
		int endOfRandomNumber = notification.indexOf("</common:attribute-randomNumber>");
		String randomNumber = notification.substring(startOfRandomNumber, endOfRandomNumber);
		
		int startOfCounter = notification.indexOf("<common:attribute-counter>") + 
							"<common:attribute-counter>".length();
		int endOfCounter = notification.indexOf("</common:attribute-counter>");
		String strCounter = notification.substring(startOfCounter, endOfCounter);

		Date date = new Date();
		StringBuilder sb = new StringBuilder("[");
		sb.append(counter);
		sb.append("]:");
		sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date).toString());
		sb.append(" ");
		sb.append(chineseCharacter);
		sb.append(" ");
		sb.append(randomNumber);
		sb.append(" counter:");
		sb.append(strCounter);
		
		System.out.println(sb.toString());
	}

}

