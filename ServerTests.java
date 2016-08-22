package iowrapper;

import java.util.ArrayList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.client.WebSocketClient.WebListener;

import android.NActivity;
import android.alerts.PlayerPopUp;
import android.alerts.TeamEditor;
import android.app.Environment;
import android.app.FragmentManager;
import android.day.ActivityDay;
import android.os.Bundle;
import android.screens.ActivityHome;
import android.setup.ActivityCreateGame;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import json.JSONException;
import json.JSONObject;
import junit.framework.TestCase;
import nnode.NodeSwitch.SwitchListener;
import shared.logic.templates.BasicRoles;
import voss.narrator.R;

public class ServerTests extends TestCase{

	
	private static SwitchWrapper nSwitch;
	private void startSwitch(IOWrapper wrap){
		nSwitch = new SwitchWrapper();
		pseudoConnect(wrap);
	}
	
	private void pseudoConnect(IOWrapper wrap){
		WebSocketClient.wl = new WebListener(){
			public void onMessageReceive(String message) {
				nSwitch.addMessage(message);
			}
		};
		nSwitch.nSwitch.switchListener = new SwitchListener(){
			public void onSwitchMessage(String s){
				try {
					JSONObject jo = new JSONObject(s);
					String receiver = jo.getString("name");
					for(Environment e: wrap.envs){
						NActivity na = (NActivity) e.getActive();
						String iName = na.server.GetCurrentUserName();
						if(iName.equals(receiver)){
							na.ns.mWebSocketClient.onMessage(s);
							break;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	public void testJoinGame(){
		IOWrapper wrap = new IOWrapper();
		startSwitch(wrap);
		Host h1 = wrap.initHost();
		ActivityHome ah = (ActivityHome) h1.getEnvironment().getActive();
		assertFalse(ah.server.IsLoggedIn());
		
		h1.login(0);
		assertTrue(ah.server.IsLoggedIn());
		
		h1.clickButton(R.id.home_join);
		nSwitch.consume();
		nSwitch.consume();
		assertEquals(nSwitch.nSwitch.instances.get(0).host.getName(), h1.getName());
	}
	
	public void testHostGame(){
		IOWrapper wrap = new IOWrapper();
		startSwitch(wrap);
		Host h1 = wrap.initHost();
		ActivityHome ah = (ActivityHome) h1.getEnvironment().getActive();
		assertFalse(ah.server.IsLoggedIn());
		
		h1.login(0);
		assertTrue(ah.server.IsLoggedIn());
		
		h1.clickButton(R.id.home_host);
		nSwitch.consume();
		nSwitch.consume();
		assertEquals(nSwitch.nSwitch.instances.get(0).host.getName(), h1.getName());
		assertEquals(h1.getEnvironment().getActive().getClass(), ActivityCreateGame.class);
	}
	
	private ArrayList<Interacter> init(int clients){
		ArrayList<Interacter> interacters = new ArrayList<>();
		IOWrapper wrap = new IOWrapper();
		startSwitch(wrap);
		Host h1 = wrap.initHost();
		h1.login(0);
		h1.setSwitch(nSwitch);
		h1.hostGame();
		
		
		interacters.add(h1);
		
		for(int i = 0; i < clients; i++){
			Client c = wrap.initClient();
			c.login(i + 1);
			c.setSwitch(nSwitch);
			c.joinGame();
			interacters.add(c);
		}
		return interacters;
	}
	
	public void testEditRoles(){
		ArrayList<Interacter> interacters = init(2);
		Host h1 = (Host) interacters.get(0);

		h1.addTeam("Bro", "#5500F9");
		h1.clickFaction("Bro");
		h1.clickButton(R.id.create_editAlliesButton);
		
		TeamEditor tEditor = (TeamEditor) h1.getActivityCreateGame().getFragmentManager().getFragment(new Bundle(), ActivityCreateGame.EDIT_TEAM_PROMPT);
		ListView nonEmptyAdapter, emptyAdapter;
		if(tEditor.l1.size() != 0){
			nonEmptyAdapter = tEditor.l1;
			emptyAdapter = tEditor.l2;
		}else{
			emptyAdapter = tEditor.l2;
			nonEmptyAdapter = tEditor.l1;
		}
		nonEmptyAdapter.click(0);
		nSwitch.consume();
		
		assertEquals(1, emptyAdapter.size());
		
	}
	
	public void testPreGameChat(){
		ArrayList<Interacter> interacters = init(2);
		Host h1 = (Host) interacters.get(0);
		
		//tests the switch to chat
		ActivityCreateGame ach = h1.getActivityCreateGame();
		
		if(ach.chatVisible())
			h1.clickButton(R.id.create_toChat);
		
		assertFalse(ach.chatVisible());
		h1.clickButton(R.id.create_toChat);
		assertTrue(ach.chatVisible());
		h1.clickButton(R.id.create_toChat);
		assertFalse(ach.chatVisible());
		
		h1.chat("hi");
		EditText hChat = (EditText) h1.getEnvironment().getActive().findViewById(R.id.create_chatET);
		assertEquals(0, hChat.getText().toString().length());
		
		TextView chatTV = (TextView) h1.getEnvironment().getActive().findViewById(R.id.create_chatTV);
		String chatContents = chatTV.getText().toString();
		assertFalse("".equals(chatContents));
		
		//3 lines should be shown.  2 that people have joined. 1 that host said something.
		assertEquals(3, chatContents.split("<br>").length);
		
		
		Client c2 = (Client) interacters.get(2);
		c2.chat("sup");
		chatTV = (TextView) c2.getEnvironment().getActive().findViewById(R.id.create_chatTV);
		chatContents = chatTV.getText().toString();
		
		String[] messages = chatContents.split("<br>");
		assertTrue(messages[messages.length-1].contains("sup"));
	}
	
	public void testTrio(){
		IOWrapper wrap = new IOWrapper();
		startSwitch(wrap);
		Host h1 = wrap.initHost();
		h1.login(0);
		
		Client c1 = wrap.initClient();
		ActivityHome c1_ah = (ActivityHome) c1.getEnvironment().getActive();
		assertFalse(c1_ah.server.IsLoggedIn());
		c1.login(1);
		assertTrue(c1_ah.server.IsLoggedIn());
		
		Client c2 = wrap.initClient();
		ActivityHome c2_ah = (ActivityHome) c2.getEnvironment().getActive();
		assertFalse(c2_ah.server.IsLoggedIn());
		c2.login(2);
		assertTrue(c2_ah.server.IsLoggedIn());
		
		h1.clickButton(R.id.home_host);
		nSwitch.consume(4); //3 greets, 1 host request
		assertEquals(ActivityCreateGame.class, h1.getEnvironment().getActive().getClass());
		c1.clickButton(R.id.home_join);
		c2.clickButton(R.id.home_join);
		
		nSwitch.consume(2); //2 join requests
		
		assertEquals(3, nSwitch.nSwitch.instances.get(0).n.getPlayerCount());
		
		ArrayList<Interacter> interacters = new ArrayList<>();
		interacters.add(h1);
		interacters.add(c1);
		interacters.add(c2);
		
		FragmentManager fm = null;
		for(Interacter i: interacters){
			i.clickButton(R.id.roles_show_Players);
			fm = i.getEnvironment().getActive().getFragmentManager();
			PlayerPopUp pops = (PlayerPopUp) fm.getFragment(null, "playerlist");
			assertEquals(3, pops.players.length());
			pops.dismiss();
		}
		
		h1.addRole(BasicRoles.Agent(), "Mafia");
		nSwitch.consume();
		
		for(Interacter i: interacters){
			assertEquals(1, ((ActivityCreateGame) i.getEnvironment().getActive()).rolesListLV.size());
		}
		ActivityCreateGame hac = (ActivityCreateGame) h1.getEnvironment().getActive();
		ActivityCreateGame cac1 = (ActivityCreateGame) c1.getEnvironment().getActive();
		
		//removes role
		hac.rolesListLV.click(0);
		nSwitch.consume();
		
		for(Interacter i: interacters){
			assertEquals(0, ((ActivityCreateGame) i.getEnvironment().getActive()).rolesListLV.size());
		}
		
		PlayerPopUp popUp = (PlayerPopUp) cac1.getFragmentManager().getFragment(null, ActivityCreateGame.PLAYER_POP_UP);
		assertEquals(3, popUp.players.length());
		
		c1.clickFaction("Town");
		c1.clickListing(((ActivityCreateGame) c1.getEnvironment().getActive()).rolesLV, "Escort");
		CheckBox cBox = cac1.getManager().screenController.cBox[0];
		
		h1.clickFaction("Town");
		h1.clickListing(hac.rolesLV, "Escort");
		nSwitch.consume();
		
		CheckBox hBox = hac.getManager().screenController.cBox[0];
		boolean prevValue = hBox.isChecked();
		hBox.toggle();
		assertEquals(!prevValue, hBox.isChecked());
		nSwitch.consume();
		
		assertEquals(!prevValue, cBox.isChecked());
		assertEquals(!prevValue, hBox.isChecked());
		
		h1.addTeam("Bro", "#5500F9");
		nSwitch.consume();
		
		for(Interacter i: interacters){
			assertEquals(6, ((ActivityCreateGame) i.getEnvironment().getActive()).cataLV.size());
		}
		
		h1.addRole(BasicRoles.Citizen(), "Town");
		h1.addRole(BasicRoles.Witch(), "Neutrals");
		nSwitch.consume();
		nSwitch.consume();
		
		h1.clickStart();
		nSwitch.consume();
		
		assertEquals(h1.getEnvironment().getActive().getClass(), ActivityDay.class);
	}
	
	public void testDeleteButton(){
		ArrayList<Interacter> interacters = init(2);
		Host h = (Host) interacters.get(0);
		
		h.addTeam("Bro", "#5500F9");
		h.clickFaction("Bro");
		h.clickButton(R.id.create_deleteTeamButton);
		nSwitch.consume();
		
		for(Interacter i: interacters){
			assertEquals(5, ((ActivityCreateGame) i.getEnvironment().getActive()).cataLV.size());
		}
	}
	
	public void testReconnect(){
		Host h = (Host) init(0).get(0);
		h.getActivity().ns.mWebSocketClient.onClose(i, s, b);
		
	}
}