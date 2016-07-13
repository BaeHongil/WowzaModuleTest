package com.winiteck.http.websockettest;

import com.wowza.wms.amf.*;
import com.wowza.wms.application.*;
import com.wowza.wms.client.*;
import com.wowza.wms.http.HTTPProviderContext;
import com.wowza.wms.http.HTTPRequestAdapter;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify3;
import com.wowza.wms.util.VHostUtils;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.vhost.VHostSingleton;

public class MyModule extends ModuleBase {
	private IMediaStreamActionNotify3 myListener = new MyListener();

	public void doSomething(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("doSomething");
		sendResult(client, params, "Hello Wowza");
	}

	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("MyModule onAppStart: " + fullname);
	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();

		getLogger().info("MyModule onAppStop: " + fullname);
	}

	public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("MyModule onConnect: " + client.getClientId());
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("MyModule onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("MyModule onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("MyModule onDisconnect: " + client.getClientId());
	}

	public void onStreamCreate(IMediaStream stream) {
		/*IVHost vhost = (IVHost)VHostSingleton.getInstance("_defaultVHost_");
		HTTPProviderContext hp = vhost.getHTTPProviderContext();
		vhost.getHTTPProviderContext();
		vhost.getHostPortsList().get(0).getHttpProviders();
		VHostUtils utils = new VHostUtils();
		HTTPRequestAdapter mHTTPRequestAdapter; */
		stream.getClient();
		
		stream.addClientListener( myListener );
		getLogger().info("MyModule onStreamCreate2: " + stream.getName());
	}
	
	public void onStreamDestroy(IMediaStream stream) {
		stream.removeClientListener( myListener );
		getLogger().info("MyModule onStreamDestroy2: " + stream.getName());
	}
}
