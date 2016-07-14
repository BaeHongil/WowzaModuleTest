package com.winiteck.http.websockettest;

import com.wowza.wms.logging.*;
import com.wowza.wms.pushpublish.model.IPushPublishSession;
import com.wowza.wms.pushpublish.model.IPushPublishSessionNotify;
import com.wowza.wms.server.*;
import com.wowza.wms.stream.IMediaStream;

import java.util.Iterator;

import com.google.gson.Gson;
import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.application.IApplication;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.http.IHTTPProvider;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify3;
import com.wowza.wms.stream.MediaStreamActionNotify3Base;
import com.wowza.wms.vhost.HostPort;
import com.wowza.wms.vhost.HostPortList;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.websocket.model.WebSocketMessage;

public class MyListener extends MediaStreamActionNotify3Base
{	
	private void broadcastWebSocketStr(IVHost vhost, WebSocketTest mWebSocketTest, String messageStr) {
		boolean isMaskOutgoingMessages = vhost.getWebSocketContext().isMaskOutgoingMessages();
		WebSocketMessage messageText = WebSocketMessage.createMessageText(isMaskOutgoingMessages, messageStr);
		mWebSocketTest.broadcastWebSocketMessage(messageText);
	}
	
	private int getVhostStreamingPort(IVHost vhost) {
		HostPortList portList = vhost.getHostPortsList();
		for(int i = 0; i < portList.size(); i++) {
			HostPort hostPort = portList.get(i);
			if( hostPort.getTypeStr().equals("Streaming") )
				return hostPort.getPort();
		}
		
		return -1;
	}
	
	private WebSocketTest getWebSocketTest(IVHost vhost) {
		HostPortList portList = vhost.getHostPortsList();
		for(int i = 0; i < portList.size(); i++) {
			HostPort hostPort = portList.get(i);
			int port = hostPort.getPort();
			if( hostPort.getTypeStr().equals("Admin") ) {
				for(Iterator<IHTTPProvider> iter = hostPort.getHttpProviders().iterator(); iter.hasNext(); ) {
					IHTTPProvider mIhttpProvider = iter.next();
					if( mIhttpProvider instanceof WebSocketTest ) 
						return (WebSocketTest) mIhttpProvider;
				}
			}
		}
		
		return null;
	}

	@Override
	public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
	{
		IVHost vhost = stream.getClient().getVHost();
		int streamingPort = getVhostStreamingPort(vhost);
		IncomingStreamAddr mIncomingStreamAddr = new IncomingStreamAddr(streamingPort, stream.getContextStr());
		WebSocketTest mWebSocketTest = getWebSocketTest( vhost );
		
		Gson gson = new Gson();
		broadcastWebSocketStr(vhost, mWebSocketTest, "gson " + gson.toJson(mIncomingStreamAddr));
		broadcastWebSocketStr(vhost, mWebSocketTest, "gson end");
	
		WMSLoggerFactory.getLogger(null).info("onPublish: " + stream.getContextStr());
	}

	@Override
	public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
	{
		WMSLoggerFactory.getLogger(null).info("onUnPublish: " + stream.getName());
	}
}