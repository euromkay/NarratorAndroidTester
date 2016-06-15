package iowrapper;

import android.app.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.GUIController;
import voss.narrator.R;
import android.alerts.IpPrompt;
import android.alerts.NamePrompt;
import android.day.ActivityDay;
import android.day.PlayerDrawerAdapter;
import android.screens.ActivityHome;
import android.wifi.SocketClient;
import shared.logic.PlayerList;

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
		NamePrompt np = (NamePrompt) aH.getFragmentManager().get("namePrompt");
		
		np.posClick();
		
	}
	public void connect(String ip) {
		ip = ip.substring("Host Code: ".length());
		ActivityHome aH = (ActivityHome) e.getActive();
		IpPrompt ipp = (IpPrompt) aH.getFragmentManager().get("ipprompt");
		
		EditText et = (EditText) ipp.mainView.findViewById(R.id.home_nameET);
		et.setText(ip);
		
		ipp.posClick();
		
		while(e.getService(SocketClient.class) == null){
			Log.m("Client " + name, "SocketClient service starting");
		}
		SocketClient sc = (SocketClient) e.getService(SocketClient.class);
		while(sc.getChat() == null){
			Log.m("Client " + name, "Preparing chat manager");
		}
		
	}
	
	public void close() {
		
	}
	
	public PlayerList getPlayers(){
		ActivityDay ad = (ActivityDay) e.getActive();
		while(ad.playerMenu == null || ad.playerMenu.adapter == null)
			Log.m("Client", "waiting for playerMenu to be created before giving out players");
		return ((PlayerDrawerAdapter) ad.playerMenu.adapter).getPlayersInView();
	}
	
	

	

}
