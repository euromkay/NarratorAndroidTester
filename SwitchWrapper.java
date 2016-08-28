package iowrapper;

import java.util.ArrayList;

import json.JSONException;

import nnode.NodeSwitch;

public class SwitchWrapper {
	NodeSwitch nSwitch;
	public SwitchWrapper(){
		nSwitch = new NodeSwitch();
		messages = new ArrayList<>();
	}
	
	ArrayList<String> messages;
	public void addMessage(String message) {
		messages.add(message);
	}
	
	public void consume(int consumes){
		for(int i = 0; i < consumes; i++)
			consume();
	}
	
	public String consume(){
		if(messages.isEmpty())
			return null;
		try {
			String message = messages.remove(0);
			nSwitch.handleMessage(message);
			return message;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void consumeAll() {
		for(int i = 0; i < messages.size(); i++){
			consume();
		}
		
	}
}
