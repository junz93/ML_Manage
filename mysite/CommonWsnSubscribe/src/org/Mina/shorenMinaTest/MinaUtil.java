/**
 * @author shoren
 * @date 2013-6-5
 */
package org.Mina.shorenMinaTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import org.Mina.shorenMinaTest.filters.ShorenCodecFactory;
//import org.Mina.shorenMinaTest.handlers.DatagramAcceptorHandler;
//import org.Mina.shorenMinaTest.handlers.DatagramConnectorHandler;
import org.Mina.shorenMinaTest.handlers.SocketAcceptorHandler;
import org.Mina.shorenMinaTest.handlers.SocketConnectorHandler;
//import org.Mina.shorenMinaTest.msg.tcp.*;
import org.Mina.shorenMinaTest.msg.WsnMsg;
//import org.Mina.shorenMinaTest.queues.MsgQueueMgr;
//import shorenMinaTest.test.ServerTest;
/**
 *
 */
public class MinaUtil {
	public static int testMax = 300;
	//for test
	protected static int senders = 0;
	
	public static int tPort = 3;
	public static int uPort = 30002;
	
	public static synchronized void incSenders()
	{
		senders++;
	}
	
	public static int getSenders(){
		return senders;
	}
	
	
	protected static Logger logger  =  Logger.getLogger(MinaUtil.class); 
	//ͨ������Ŀ���ʱ�䣬������60��󣬹ر�ͨ��
	protected static final int IDLE_TIME = 60*5;  //unit is second
	
		//���������ĸ���,�ܸ�����session�ĸ���
	protected static int TCPBlockedCount = 0;  
	protected static int UDPBlockedCount = 0;
   //ͨ�����ܸ�������������session��ʱ������
	protected static int TCPTotalCount = 0;
	protected static int UDPTotalCount = 0;
	

	//����״̬����һ�����ֱ�ʾ����״̬	
	public static final int HEALTHY = 17;
	public static final int HEALTHY_TCP = 1;
	public static final int UNWELL_TCP = 2;
	public static final int SICK_TCP = 4;
	public static final int ILL_TCP = 8;
	
	public static final int HEALTHY_UDP = 16;
	public static final int UNWELL_UDP = 32;
	public static final int SICK_UDP = 64;
	public static final int ILL_UDP = 128;
	
	
	protected static int state = HEALTHY;
	protected static int last_state = state;  //delete?


	//����ͨ����״̬
	public static final String SHealthy = "healthy";
	public static final String SSick = "sick";
	public static final String SDead = "dead";
	
	public static int maxThree = 0;//��¼���Ͷ��е���󳤶�
	

	public static int freeze_timeIni = 75;//��ʼ����Ӽ��
    public static int freeze_time = freeze_timeIni; //���ʱ��?ms
 	public static int freeze_timeMax = 100;//���ʱ������������100ms
 	public static int freeze_timeMin = 50;//��Ӽ����������20ms
 	//public static int PreConfigedTimeMin = MinaUtil.getMinEnQueueTime();
 	
 	public static ArrayList<String> forwardIP = null ;
 	
 	public static int checkRatio = 20;
	
	/**
	 * unwell��ʾ����������ʵ�������Ϣ��ӱ�����
	 * sick�ǳ������������أ���������ٶȡ�
	 * ��tcp��udp�����������أ������̳߳طַ�����ʱ�ӵ����ȼ���ʼ�����ˣ�ͬʱ������ӱ��ؽ�Ϊ0��
	 * */	
	public static final double TcpRatio_unwell = 0.3;  
	public static final double TcpRatio_sick = 0.5;
	public static final double UdpRatio_unwell = 0.3;
	public static final double UdpRatio_sick = 0.5;
	
	
	/**
	 * ÿ��ͨ����Queue���ȷ�ֵ.
	 * ��TCP��UDP��ֵ��ͬ������SingleChannelQueueThread�������ã���Ϊsession��attribute��
	 *���÷�Χ�� С��minthʱ�����н���������minth��С��maxthʱ������sick���㶪���ʣ�
	 *          �ӵ����ȼ���ʼ����
	 * */
	protected static final int singleMinth = 3000;
	protected static final int singleMaxth = 4000;
	
	
	/**
	 * ʹ�õ�����򵥵�RED�㷨�����㶪���ʡ�
	 * ������
	 * double weight��Ȩ��
	 * int avg�����е�ƽ�����ȣ���ʼ��Ϊ0����ʾ���ǵ�ǰ���еĳ��û���ֵ��
	 * int qLength����ǰ���еĳ���
	 * double maxp �������ʵ����ֵ
	 * int count�����ϴζ����𣬵����ڽ��յ���Ϣ����Ŀ
	 * */
	public static int calPacket_loss_rate(IoSession session){				
		double rate = 0;
		double weight = (Double) session.getAttribute("weight");
		int avg = (Integer) session.getAttribute("avg");
		int qLength = (Integer) session.getAttribute("qLength");
		double maxp = (Double) session.getAttribute("maxp");
		int count = (Integer) session.getAttribute("count");
		
		avg = (int) ((1 - weight)*avg + weight * qLength);
		double tempRate = maxp * (avg - singleMinth)/(singleMaxth - singleMinth);
		
		rate = tempRate * (1 - count * tempRate);
		int lossCount = (int) (qLength * rate);

		//System.out.println("�����ʼ��㣺avg = "+avg+"   tempRate = "+tempRate+"   rate = "+rate+"   qLentgh= "+qLength+"   lossCount = "+lossCount);
		
		session.setAttribute("lossCount", lossCount);
		session.setAttribute("avg", avg);
		session.setAttribute("count", 0);
		if(tempRate < 0 )
			return 0;
		else
		    return lossCount;
	}
	
	
	/**
	 * ��ʼ��һЩ����,��Щ������Ҫ����
	 * double weight��Ȩ��
	 * int avg�����е�ƽ�����ȣ���ʼ��Ϊ0����ʾ���ǵ�ǰ���еĳ��û���ֵ��
	 * int qLength����ǰ���еĳ���
	 * double maxp �������ʵ����ֵ
	 * int count�����ϴζ����𣬵����ڽ��յ���Ϣ����Ŀ
	 * */
	public static void iniSessionReferance(IoSession session){
		session.setAttribute("weight", 0.5);
		session.setAttribute("avg", 0);
		session.setAttribute("qLength", 0);
		session.setAttribute("maxp", 0.5);
		session.setAttribute("count", 0);
		
		session.setAttribute("lowestPriority", 2);
		session.setAttribute("lossCount", 0);
		session.setAttribute("state", SHealthy);
		session.setAttribute("last_state", SHealthy);
		
		/*if(ServerTest.test){
			session.setAttribute("inCount", 0);
			session.setAttribute("outCount", 0);
			session.setAttribute("totalCount", 0);
		}*/
		
	}
	
	public static int a = 0;
	//��ȡ�����ļ��е���Ӽ��
/*	public static int getMinEnQueueTime(){
		Configuration configuration;
		configuration = new Configuration();
		boolean ManagerOn = configuration.configure2();
		return a = configuration.EnQueueTime;
	}*/

	
	public static int getSingleminth() {
		return singleMinth;
	}

	public static int getSinglemaxth() {
		return singleMaxth;
	}

	public static int getTCPTotalCount() {
		return TCPTotalCount;
	}

	public static int getUDPTotalCount() {
		return UDPTotalCount;
	}

	//TCPȫ��ͨ������
	public static synchronized void inTCPTotalCount(){
		TCPTotalCount++;	
	}
	
	//UDPȫ��ͨ������
	public static synchronized void deTCPTotalCount(){
		TCPTotalCount--;
	}
	
	//��ͨ����������������������
	public static synchronized void inUDPTotalCount(){
		UDPTotalCount++;
	}
	
	//��ͨ����������󣬼�������������
	public static synchronized void deUDPTotalCount(){
		UDPTotalCount--;
	}
		

	
	//��ͨ����������������������
	public static synchronized void inTCPBlockCount(){
		TCPBlockedCount++;
	//	System.out.println("TCPblockedCount = "+ TCPBlockedCount);
	}
	
	//��ͨ����������󣬼�������������
	public static synchronized void deTCPBlockCount(){
		TCPBlockedCount--;
	//	System.out.println("TCPBlockedCount--");
	}
	
	//��ͨ����������������������
	public static synchronized void inUDPBlockCount(){
		UDPBlockedCount++;
	//	System.out.println("UDPBlockedCount++");
	}
	
	//��ͨ����������󣬼�������������
	public static synchronized void deUDPBlockCount(){
		
		UDPBlockedCount--;
		if(UDPBlockedCount<0){
			UDPBlockedCount = 0;
		}
	//	System.out.println("UDPBlockedCount--");
	}
	

	public static int getTCPBlockCount() {
		return TCPBlockedCount;
	}

	public static int getUDPBlockedCount() {
		return UDPBlockedCount;
	}
	
	public static int getState() {
		return state;
	}

	public static int getLast_state() {
		return last_state;
	}


	public static void setLast_state(int last_state) {
		MinaUtil.last_state = last_state;
	}


	/**
	 * ������Ϊͬ����ֻ����MsgQueueMgr�вŻ���á�
	 * */
	public static void setState(int state) {
		MinaUtil.state = state;
	}
	
	//�������ʱ������С���ѹ��
	public static void inFreezeTime(){
		freeze_time += 1;
		if(freeze_time > freeze_timeMax){
			freeze_time = freeze_timeMax;
		}
	}
	
//�������ʱ�������ӷ����ٶ�
	public static void deFreezeTime(){
		freeze_time -= 1;
		if(freeze_time < freeze_timeMin){
			freeze_time = freeze_timeMin;
		}
	}
	
	
	//create NioSocketAcceptor  with port
	public static NioSocketAcceptor createSocketAcceptor(String ip, int port){
		// ����һ����������server�˵�Socket
		NioSocketAcceptor acceptor = new NioSocketAcceptor();
		try {  
			// ���ù�����  
			setFilters(acceptor);
			//����threadPool����ʹ�ô��̳߳ؽ����߳�ִ��ҵ���߼���IoHandler������
            //acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(3,3)); 
            // ���ö�ȡ���ݵĻ�������С  
            acceptor.getSessionConfig().setReadBufferSize(8190000);
            
            acceptor.getSessionConfig().setMaxReadBufferSize(200000000);//100MB
            // ��дͨ��10�����޲����������״̬  
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, IDLE_TIME);  
            // ���߼�������  
            acceptor.setHandler(new SocketAcceptorHandler());  
            // �󶨶˿�  
            acceptor.bind(new InetSocketAddress(InetAddress.getByName(ip), port));  
//            acceptor.getSessionConfig().set
            logger.info("����������ɹ�... tcp�˿ں�Ϊ��" + port); 
            System.out.println("����������ɹ�... tcp�˿ں�Ϊ��" + port);
        } catch (Exception e) {  
            logger.error("�����tcp�����쳣....", e);  
            e.printStackTrace();  
        }
		
		return acceptor;
	}
	
	
	//create NioSocketConnector with port and IP
	//ע��connector.dispose();
	public static NioSocketConnector createSocketConnector(){
		NioSocketConnector connector = new NioSocketConnector();
		// ���ù�����  
		setFilters(connector);
		//����threadPool����ʹ�ô��̳߳ؽ����߳�ִ��ҵ���߼���IoHandler������
//		connector.getFilterChain().addLast("threadPool", new ExecutorFilter(3,3));
		connector.setConnectTimeoutCheckInterval(30);
		connector.getSessionConfig().setSendBufferSize(100000000);//100MB
        connector.setHandler(new SocketConnectorHandler());//�����¼�������   
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, IDLE_TIME);
        //һ��connector�������Ӷ�����������������Ӳ���Ӧ�÷�������
       /* ConnectFuture cf = connector.connect(new InetSocketAddress(ip, port));//��������   
        cf.awaitUninterruptibly();//�ȴ����Ӵ������   
*/        
		return connector;
	}
	
		
	//create NioDatagramAcceptor with port
//	public static NioDatagramAcceptor createDatagramAcceptor(String ip, int port){
//		NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
//		// ���ù�����  
//		setFilters(acceptor);
//		acceptor.getSessionConfig().setReceiveBufferSize(200000000);//100MB
//		acceptor.setHandler(new DatagramAcceptorHandler());
//		// �󶨶˿�  
//        try {
//			acceptor.bind(new InetSocketAddress(InetAddress.getByName(ip), port));
//			logger.info("����������ɹ�... udp�˿ں�Ϊ��" + port);  
//		} catch (IOException e) {
//			logger.error("�����udp�����쳣....", e);
//			e.printStackTrace();
//		} 
//		return acceptor;
//	}
//	
//	//create NioDatagramConnector with port and IP
//	public static NioDatagramConnector createDatagramConnector(){
//		NioDatagramConnector connector = new NioDatagramConnector();
//		// ���ù�����  
//		setFilters(connector);		
//		connector.setHandler(new DatagramConnectorHandler());
//		connector.getSessionConfig().setSendBufferSize(100000000);//100MB
//		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, IDLE_TIME);
//	//	ConnectFuture cf = connector.connect(new InetSocketAddress(ip, port));//��������   
//   //     cf.awaitUninterruptibly();//�ȴ����Ӵ������   
//		return connector;
//	}
	
//	public static NioDatagramConnector CreatBoardcast(){
//		NioDatagramConnector connector = new NioDatagramConnector();
//		// ���ù�����  
//		setFilters(connector);		
//		connector.setHandler(new DatagramConnectorHandler());
//		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, IDLE_TIME);
//	//	ConnectFuture cf = connector.connect(new InetSocketAddress(ip, port));//��������   
//   //     cf.awaitUninterruptibly();//�ȴ����Ӵ������   
//		return connector;
//	}
	
	//ÿһ��client��server������һ��,������Ҫ�ǶԹ�����������
	//config acceptor and connector with filters
	//Ҫ�����Լ���codec
	public static void setFilters(IoService service){ 
		
//		service.getFilterChain().addLast("logger", new LoggingFilter());  
		service.getFilterChain().addLast("codec1",  
				                //new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));  
                new ProtocolCodecFilter(new ShorenCodecFactory(Charset.forName("UTF-8")))); 
		
//		service.getFilterChain().addLast("filterthreadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
	}
	
	
	@SuppressWarnings({ "rawtypes" })
	public static String msgToString(WsnMsg msg){
		Class type = msg.getClass();
		String className = type.getName();
		StringBuilder content = new StringBuilder();
		if(type != null)
		{
			content.append("className`");
			content.append(className);
			content.append("~");
			Field[] fields = type.getDeclaredFields();
			for(int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];
				try {
					String value = getFieldValue(field, msg);
					if(value != null){
						if(field.getName() == "doc"){
							int headLengeh = content.length();
							content.insert(0, String.valueOf(headLengeh)+";");
							//content.append(";");
							content.append(value);
							break;
						}
						content.append(field.getName());
						content.append("`");
						content.append(value);
						content.append("~");
					}						
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} 
			}
		}		
		return content.toString();
	}

	
	public static String getFieldValue(Field field, Object obj){
		String value = null;
		Object v = null;
		if(((field.getModifiers() & 0x05) == 0) || ((field.getModifiers() & 0x08) != 0))  //��public��protected,���ؿ�;����static
			return value;
		try {
			v = field.get(obj);
			if(v != null){				
				value = v.toString();				
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static WsnMsg stringToMsg(String smsg, WsnMsg msg){
		Class type = msg.getClass();
		if(type != null && smsg != "")
		{
			//��������ֵ��������
			String[] values = smsg.split("~");
			
			Map<String, String> valuesMap = new HashMap();
			for(int i=0; i < values.length; i++){
				String value = values[i];
				int index = value.indexOf("`");
				String k = value.substring(0, index);
				String v = value.substring(index + 1);
				valuesMap.put(k, v);
			}
			
			//Ϊ����ֵ
			Field[] fields = type.getDeclaredFields();
			for(int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];
				Object value = valuesMap.get(field.getName());
				if (field.getType() == Boolean.class)
				{
					value = new Boolean(value.equals("1")
							|| String.valueOf(value).equalsIgnoreCase("true"));
				}
				try {													
					field.set(msg, value);
					
				} catch (IllegalArgumentException e) {
					if(field != null){
						Class ftype = field.getType();
						Object v = convertValueFromString(ftype, value);
						try {
							field.set(msg, v);
						} catch (IllegalArgumentException e2) {
							
							e2.printStackTrace();
						} catch (IllegalAccessException e3) {
							
							e3.printStackTrace();
						}
					}
					
				} catch (IllegalAccessException e) {
					
					e.printStackTrace();
				}
			}
		}
		
		return msg;
	}
	
	public static WsnMsg stringToMsg(String smsg, String doc , WsnMsg msg){
		Class type = msg.getClass();
		if(type != null && smsg != "")
		{
			//��������ֵ��������
			String[] values = smsg.split("~");
			
			Map<String, String> valuesMap = new HashMap();
			for(int i=0; i < values.length; i++){
				String value = values[i];
				int index = value.indexOf("`");
				String k = value.substring(0, index);
				String v = value.substring(index + 1);
				valuesMap.put(k, v);
			}
			
			valuesMap.put("doc", doc);
			
			//Ϊ����ֵ
			Field[] fields = type.getDeclaredFields();
			for(int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];
				Object value = valuesMap.get(field.getName());
				if (field.getType() == Boolean.class)
				{
					value = new Boolean(value.equals("1")
							|| String.valueOf(value).equalsIgnoreCase("true"));
				}
				try {
					field.set(msg, value);
					
				} catch (IllegalArgumentException e) {
					if(field != null){
						Class ftype = field.getType();
						Object v = convertValueFromString(ftype, value);
						try {
							field.set(msg, v);
						} catch (IllegalArgumentException e2) {
							
							e2.printStackTrace();
						} catch (IllegalAccessException e3) {
							
							e3.printStackTrace();
						}
					}
					
				} catch (IllegalAccessException e) {
					
					e.printStackTrace();
				}
			}
		}
		
		return msg;
	}

	
	@SuppressWarnings("deprecation")
	protected static Object convertValueFromString(Class type, Object value)
	{
		if (value instanceof String && type.isPrimitive())
		{
			String tmp = (String) value;

			if (type.equals(boolean.class))
			{
				if (tmp.equals("1") || tmp.equals("0"))
				{
					tmp = (tmp.equals("1")) ? "true" : "false";
				}

				value = new Boolean(tmp);
			}
			else if (type.equals(char.class))
			{
				value = new Character(tmp.charAt(0));
			}
			else if (type.equals(byte.class))
			{
				value = new Byte(tmp);
			}
			else if (type.equals(short.class))
			{
				value = new Short(tmp);
			}
			else if (type.equals(int.class))
			{
				value = new Integer(tmp);
			}
			else if (type.equals(long.class))
			{
				value = new Long(tmp);
			}
			else if (type.equals(float.class))
			{
				value = new Float(tmp);
			}
			else if (type.equals(double.class))
			{
				value = new Double(tmp);
			}
		}else if(value instanceof String){
			if(type.equals(java.util.Date.class)){
				value = new Date((String)value);
			}
		}

		return value;
	}
	
//	public static MsgInsert geMsgTest(){
//		MsgInsert ms = new MsgInsert();
//		ms.tagetGroupName = "this group";
//		ms.name = "there is msg inserted";
//		ms.addr = "localAddress";
//		ms.id = 123456789;
//		ms.tPort = 10243;
//		ms.uPort = 10244;
//		return ms;
//	}
	
	//��ȡ��ǰ����Ӽ��
	public static int GetFreezeTimeMin(){
		return freeze_timeMin;
	}
	
	//������Ӽ������
	public static void SetFreezeTimeMin(int aim){
		freeze_timeMin = aim;
		if(freeze_timeMin > 100){
			freeze_timeMin = 100;
		}
		try {
			MinaUtil.UpdataConfigure(freeze_timeMin);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//�����º����Ӽ��д�������ļ�
	private static void UpdataConfigure(int newTime) throws IOException{
		int aimline = 29;
		BufferedReader br = new BufferedReader(new FileReader( "configure.txt"));
		StringBuffer sb = new StringBuffer(4096);
		String temp = null;
		String NewTime = String.valueOf(newTime);
		int line = 0;
		while((temp = br.readLine())!=null){
		  line++;
		  if(line == aimline){
			  sb.append("EnqueueTime:"+NewTime);
			  continue;
		  }
		  sb.append(temp).append( "\r\n");
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter( "configure.txt"));
		bw.write(sb.toString());
		bw.close();
	}
	
	//��ȡ������session
	public static String GetSession(IoSession session){
		return session.toString();
	}
	

	//��ʱ��������ת��Ŀ��ip
	public static ArrayList<String> putIP( ArrayList<String> forwardIP ){
		
		//forwardIP.add("10.109.253.23");
		forwardIP.add("10.109.253.19");
		return forwardIP;
	}
	
	@SuppressWarnings("null")
	public static ArrayList<String> putIP(){
		
		forwardIP.add("10.109.253.17");
		forwardIP.add("10.109.253.16");

		return forwardIP;
	}

}
