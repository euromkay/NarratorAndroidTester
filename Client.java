package iowrapper;

import json.JSONArray;

import android.GUIController;
import android.JUtils;
import android.NActivity;
import android.alerts.IpPrompt;
import android.alerts.NamePrompt;
import android.app.Environment;
import android.day.ActivityDay;
import android.day.PlayerDrawerAdapter;
import android.screens.ActivityHome;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import shared.logic.Narrator;
import shared.logic.PlayerList;
import voss.narrator.R;

public class Client extends Interacter{

	public Client(Environment e, WrapSynchr ws) {
		super(e, ws);
		
	}
	public void clickJoin() {
		ActivityHome aH = (ActivityHome) e.getActive();
		aH.onClick(new View(R.id.home_join));
		
	}
	public GUIController getController() {
		ActivityDay ad = (ActivityDay) e.getActive();
		
		return new GUIController(ad);
	}
	public void clickNameJoin() {
		ActivityHome aH = (ActivityHome) e.getActive();
		NamePrompt np = (NamePrompt) aH.getFragmentManager().getFragment(null, "namePrompt");
		
		np.posClick();
		
	}
	public void connect(String ip) {
		ip = ip.substring("Host Code: ".length());
		ActivityHome aH = (ActivityHome) e.getActive();
		IpPrompt ipp = (IpPrompt) aH.getFragmentManager().getFragment(null, "ipprompt");
		
		EditText et = (EditText) ipp.mainView.findViewById(R.id.home_nameET);
		et.setText(ip);
		
		ipp.posClick();
		
		
		
	}
	
	public void close() {
		
	}
	
	public PlayerList getPlayers(){
		ActivityDay ad = (ActivityDay) e.getActive();
		while(ad.playerMenu == null || ad.playerMenu.adapter == null)
			Log.m("Client", "waiting for playerMenu to be created before giving out players");
		JSONArray jArray = ((PlayerDrawerAdapter) ad.playerMenu.adapter).getPlayersInView();
		if(((NActivity) e.getActive()).server.IsLoggedIn()){
			return null;
		}else{
			Narrator n = ((NActivity) e.getActive()).ns.local;
			PlayerList players = new PlayerList();
			for(int i = 0; i < jArray.length(); i++)
				players.add(n.getPlayerByName(JUtils.getString(jArray, i)));
			return players;
		}
	}
	public void joinGame() {
		clickButton(R.id.home_join);
		nSwitch.consume(3);
	}
	
	

	

}
