package com.winiteck.http.websockettest;

import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.FastDateFormat;

import com.wowza.util.BufferUtils;
import com.wowza.util.SystemUtils;
import com.wowza.wms.http.*;
import com.wowza.wms.logging.*;
import com.wowza.wms.server.IServer;
import com.wowza.wms.server.Server;
import com.wowza.wms.util.WebSocketUtils;
import com.wowza.wms.vhost.*;
import com.wowza.wms.websocket.model.IWebSocketSession;
import com.wowza.wms.websocket.model.WebSocketContext;
import com.wowza.wms.websocket.model.WebSocketEventNotifyBase;
import com.wowza.wms.websocket.model.WebSocketMessage;

public class WebSocketTest extends HTTProvider2Base {

	private static final Class<WebSocketTest> CLASS = WebSocketTest.class;
	private static final String CLASSNAME = "HTTPProviderWebSocket";
	
	public static final String DATEFORMAT = "EEE, dd MMM yyyy HH:mm:ss";
	public static final int TIMER_INTERVAL = 6000;
	
	private FastDateFormat fastDateFormat = FastDateFormat.getInstance(DATEFORMAT, SystemUtils.gmtTimeZone, Locale.US);
	private Timer timer = null;
	private Object lock = new Object();

	// WebSocket listener
	class MyWebSocketListener extends WebSocketEventNotifyBase
	{
		@Override
		public void onCreate(IWebSocketSession webSocketSession)
		{
			WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyWebSocketListener.onCreate["+webSocketSession.getSessionId()+"]");
		}

		@Override
		public void onDestroy(IWebSocketSession webSocketSession)
		{
			WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyWebSocketListener.onDestroy["+webSocketSession.getSessionId()+"]");
		}

		@Override
		public void onMessage(IWebSocketSession webSocketSession, WebSocketMessage message)
		{
			// echo messages we receive back to the browser
			if (message.isText())
			{
				WebSocketMessage messageText = WebSocketMessage.createMessageText(webSocketSession.isMaskOutgoingMessages(), message.getValueString());
				webSocketSession.sendMessage(messageText);
				
				WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyWebSocketListener.onMessage["+webSocketSession.getSessionId()+"][text]: "+message.getValueString());
			}
			else if (message.isBinary())
			{
				WebSocketMessage messageBinary = WebSocketMessage.createMessageBinary(webSocketSession.isMaskOutgoingMessages(), message.getBuffer(), message.getOffset(), message.getLen());
				webSocketSession.sendMessage(messageBinary);
				
				WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyWebSocketListener.onMessage["+webSocketSession.getSessionId()+"][binary]: #"+BufferUtils.encodeHexString(message.getBuffer(), message.getOffset(), message.getLen()));
			}
		}
	}
	
	class MyTimerTask extends TimerTask
	{
		private IVHost vhost = null;
		private WebSocketContext webSocketContext = null;
		
		MyTimerTask(IVHost vhost)
		{
			this.vhost = vhost;
			this.webSocketContext = vhost.getWebSocketContext();
		}
		
		@Override
		public void run()
		{
			String messageStr = Server.getInstance().getVersion()+" date:"+fastDateFormat.format(new Date())+" GMT";
						
			// broadcast message to all active sessions attached to this HTTPProvider
			WebSocketMessage messageText = WebSocketMessage.createMessageText(webSocketContext.isMaskOutgoingMessages(), messageStr);
			broadcastWebSocketMessage(messageText);

			WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyTimerTask.run: "+messageStr);
		}
	}

	public void onHTTPRequest(IVHost vhost, IHTTPRequest req, IHTTPResponse resp)
	{
		StackTraceElement[] ste = new Throwable().getStackTrace();
		
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME + " constructor");
		for(StackTraceElement element : ste) {
			WMSLoggerFactory.getLogger(CLASS).info(element.getClassName() + "["+ element.getMethodName() +"]" + "("+ element.getLineNumber()+ ")");
		}
		
		if (!doHTTPAuthentication(vhost, req, resp))
			return;
		/*
		synchronized(lock)
		{
			// create timer task on first connection
			if (this.timer == null)
			{
				this.timer = new Timer();
				timer.scheduleAtFixedRate(new MyTimerTask(vhost), TIMER_INTERVAL, TIMER_INTERVAL);
			}
		}*/
		
		// is this an upgrade request
		if (req.isUpgradeRequest())
		{
			// it this an websocket upgrade request
			String upgradeType = req.getHeader("upgrade");
			if (upgradeType != null && upgradeType.equalsIgnoreCase(IWebSocketSession.HTTPHEADER_NAME))
			{
				// set response header to accept the upgrade
				resp.setHeader("Upgrade", IWebSocketSession.HTTPHEADER_NAME);
				resp.setHeader("Connection", "Upgrade");
				
				// set the security hash
				String webSocketKey = req.getHeader(IWebSocketSession.HTTPHEADER_SECKEY);
				if (webSocketKey != null)
				{
					String digestStr = WebSocketUtils.createSecResponse(webSocketKey);
					if (digestStr != null)
						resp.setHeader(IWebSocketSession.HTTPHEADER_SECACCEPT, digestStr);
				}
				
				// set 101 response code to accept upgrade request
				resp.setResponseCode(101);
				
				// insert WebSocket listener for this session
				resp.setUpgradeRequestProtocol(IHTTPResponse.UPGRADE_PROTOCOL_WEBSOCKETS, new MyWebSocketListener());
			}
			else
				resp.setResponseCode(404); // return 404 if not websocket upgrade request
		}
		else
			resp.setResponseCode(404); //return 404 if not upgrade request
	}

}
