package iowrapper;

import java.util.ArrayList;

import android.app.Environment;
import android.content.Intent;
import android.day.ActivityDay;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.texting.CommunicatorText;
import android.texting.PhoneNumber;
import android.texting.TextController;
import android.texting.TextInput;
import android.util.Log;
import shared.ai.Controller;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.support.Communicator;

public class TextClient extends Interacter{

	private PhoneNumber number;
	public TextClient(String number, Environment e) {
		this.e = e;
		this.number = new PhoneNumber(number);
	}


	public Controller getController() {
		TextInput ti = new TextInput(){
			public void text(Player p, String message, boolean sync) {
				Log.i(p.toString(), message);
				TextClient.this.text(message);
			}
			
		};
		return new TextController(ti);
	}


	public PlayerList getPlayers() {
		PlayerList list = new PlayerList();
		for(Player p: getNarrator().getAllPlayers()){
			Communicator c = p.getCommunicator();
			if(c.getClass() == CommunicatorText.class){
				CommunicatorText cp = (CommunicatorText) c;
				if(cp.getNumber().equals(number))
					list.add(p);
			}
		}
		return list;
	}


	public ArrayList<String> getMessages() {
		return SmsManager.getDefault().records.get(number.toString());
	}

	
	public void text(String message) {
		Bundle b = new Bundle();
		b.putString("message", message);
		b.putString("number", number.toString());
		

		Intent i = new Intent();
		i.setBundle(b);
		
		ActivityDay ad = (ActivityDay) e.getActive();
		ad.intentReceiver.onReceive(null, i);
		
	}
	
	
}
