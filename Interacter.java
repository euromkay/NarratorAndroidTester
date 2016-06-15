package iowrapper;

import android.app.Environment;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.NarratorService;
import voss.narrator.R;
import android.alerts.NamePrompt;
import android.alerts.PlayerPopUp;
import android.screens.ActivityHome;
import android.setup.ActivityCreateGame;
import android.setup.SetupListener;
import shared.ai.Controller;
import shared.logic.Narrator;
import shared.logic.PlayerList;

public abstract class Interacter {

	protected Environment e;
	protected WrapSynchr ws;
	public Interacter(){
	}
	public Interacter(Environment e, WrapSynchr ws) {
		this.e = e;
		this.ws = ws;
		
		
		ActivityHome aH = (ActivityHome) e.startActivity(ActivityHome.class, new Intent());
		aH.creating(null);
		
	}
	public void clickJoin() {
		ActivityHome aH = (ActivityHome) e.getActive();
		aH.onClick(new View(R.id.home_join));
		
	}
	public String name;
	public void setName(String string) {
		name = string;
		ActivityHome aH = (ActivityHome) e.getActive();
		NamePrompt np = (NamePrompt) aH.getFragmentManager().get("namePrompt");
		
		EditText et = (EditText) np.mainView.findViewById(R.id.home_nameET);
		et.setText(string);
		name = string;
	}
	
	public HostSub newPlayer(String name) {
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		ac.onClick(new View(R.id.roles_show_Players));
		
		PlayerPopUp popUp = (PlayerPopUp) ac.getFragmentManager().get("playerlist");
		popUp.onCreateView(ac.getLayoutInflater(), null, null);
		EditText et = (EditText) popUp.mainView.findViewById(R.id.addPlayerContent);
		et.setText(name);
		
		popUp.onClick(null);
		
		Interacter i;
		while((i = ws.missingPlayer(name)) != null){
			Log.m("Interacter",  i.getName() + " waiting for " + name);
		}
		
		popUp.dismiss();
		
		return new HostSub(this);
	}

	public Environment getEnvironment() {
		return e;
	}
	
	public void close() {
		e.close();
	}
	
	public abstract Controller getController();
	
	public abstract PlayerList getPlayers();
	public void setSetupListener(SetupListener sl) {	
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		ac.getManager().addListener(sl);
			}
	public String getName() {
		if(name == null){
			e.toString();
		}
		return name;
	}
	
	public Narrator getNarrator() {
		NarratorService ns = (NarratorService) e.services.get(NarratorService.class);
		return ns.local;
	}
}
