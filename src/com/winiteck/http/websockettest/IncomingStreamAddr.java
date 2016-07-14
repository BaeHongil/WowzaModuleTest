package com.winiteck.http.websockettest;

public class IncomingStreamAddr {
	private int port;
	private String contextStr;
	
	public IncomingStreamAddr(int port, String contextStr) {
		this.port = port;
		this.contextStr = contextStr;
	}
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getContextStr() {
		return contextStr;
	}
	public void setContextStr(String contextStr) {
		this.contextStr = contextStr;
	}
}
