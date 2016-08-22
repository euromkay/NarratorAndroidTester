package iowrapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.firebase.auth.FirebaseAuth;

import android.NActivity;
import android.NarratorService;
import android.alerts.LoginAlert;
import android.alerts.NamePrompt;
import android.alerts.PlayerPopUp;
import android.app.Environment;
import android.app.FragmentManager;
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
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
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
		Button b = ((Button) getEnvironment().getActive().findViewById(id));
		if(b.getVisibility() != View.VISIBLE)
			throw new IllegalStateException("Button isn't visible");
		b.click();
	}
	
	
	public void login(int i){
		JSONObject creds = null;
		try {
			creds = getCredentials(i);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}
		
		clickButton(R.id.home_login_signup);
		FragmentManager fm = getEnvironment().getActive().getFragmentManager();
		LoginAlert lAlert = (LoginAlert) fm.getFragment(null, "logginer");
		try {
			lAlert.userET.setText(creds.getString("username"));
			lAlert.pwET.setText(creds.getString("password"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		((Button) lAlert.mainView.findViewById(R.id.login_loginButton)).click();
		this.name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
	}
	
	public static JSONArray getAllCreds(){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("creds.txt"));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    return new JSONArray(sb.toString());
		    
		}  catch (JSONException|IOException e){
			e.printStackTrace();
		} finally {
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
	
	public static JSONObject getCredentials(int i) throws JSONException{
		return getAllCreds().getJSONObject(i);
	}
	
	public String name;
	public void setName(String string) {
		name = string;
		ActivityHome aH = (ActivityHome) e.getActive();
		NamePrompt np = (NamePrompt) aH.getFragmentManager().getFragment(null, "namePrompt");
		
		EditText et = (EditText) np.mainView.findViewById(R.id.home_nameET);
		et.setText(string);
		name = string;
	}
	
	protected void doubleClick(){
		((ActivityDay)getEnvironment().getActive()).onDoubleTap();
	}
	
	protected void clickFaction(String name){
		ActivityCreateGame ac = (ActivityCreateGame) getEnvironment().getActive();
		clickListing(ac.cataLV, name);
	}
	protected void clickListing(ListView lv, String name){
		ListingAdapter list = (ListingAdapter) lv.adapter;
		
		int position = list.data.indexOf(name);
		lv.click(position);
	}
	
	public HostSub newPlayer(String name) {
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		this.clickButton(R.id.roles_show_Players);
		
		PlayerPopUp popUp = (PlayerPopUp) ac.getFragmentManager().getFragment(null, "playerlist");
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
	
	public ActivityCreateGame getActivityCreateGame() {
		return (ActivityCreateGame) getActivity();
	}
	
	public NActivity getActivity() {
		return (NActivity) getEnvironment().getActive();
	}
	
	public void chat(String message) {
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		if(!ac.chatVisible())
			clickButton(R.id.create_toChat);
		EditText et = (EditText) ac.findViewById(R.id.create_chatET);
		et.inputText(message);
		clickButton(R.id.create_chatButton);
		if(nSwitch != null)
			nSwitch.consume();
	}
	
	protected SwitchWrapper nSwitch;
	public void setSwitch(SwitchWrapper nSwitch) {
		this.nSwitch = nSwitch;
	}
}
