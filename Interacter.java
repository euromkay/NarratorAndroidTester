package iowrapper;

import android.NarratorService;
import android.alerts.NamePrompt;
import android.alerts.PlayerPopUp;
import android.app.Environment;
import android.content.Intent;
import android.day.ActivityDay;
import android.screens.ActivityHome;
import android.screens.ListingAdapter;
import android.setup.ActivityCreateGame;
import android.setup.SetupListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import shared.ai.Controller;
import shared.logic.Narrator;
import shared.logic.PlayerList;
import voss.narrator.R;

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
		clickButton(R.id.home_join);
	}
	public void clickButton(int id){
		((Button) getEnvironment().getActive().findViewById(id)).click();
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
	
	protected void doubleClick(){
		((ActivityDay)getEnvironment().getActive()).onDoubleTap();
	}
	
	protected void clickListing(ListView lv, String name){
		ActivityCreateGame ac = (ActivityCreateGame) getEnvironment().getActive();
		ListingAdapter list = (ListingAdapter) lv.adapter;
		
		int position = list.data.indexOf(name);
		ac.onItemClick(lv, null, position, 0);
	}
	
	public HostSub newPlayer(String name) {
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		ac.onClick(new View(R.id.roles_show_Players));
		
		PlayerPopUp popUp = (PlayerPopUp) ac.getFragmentManager().get("playerlist");
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
