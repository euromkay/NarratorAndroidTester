package iowrapper;

import java.util.ArrayList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.client.WebSocketClient.WebListener;
import org.json.JSONException;
import org.json.JSONObject;

import android.NActivity;
import android.alerts.PlayerPopUp;
import android.app.Environment;
import android.app.FragmentManager;
import android.day.ActivityDay;
import android.screens.ActivityHome;
import android.setup.ActivityCreateGame;
import android.widget.CheckBox;
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
				try {
					nSwitch.nSwitch.handleMessage(message);
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
					// TODO Auto-generated catch block
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
		assertEquals(nSwitch.nSwitch.instances.get(0).host.getName(), h1.getName());
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
		assertEquals(ActivityCreateGame.class, h1.getEnvironment().getActive().getClass());
		c1.clickButton(R.id.home_join);
		c2.clickButton(R.id.home_join);
		
		assertEquals(3, nSwitch.nSwitch.instances.get(0).n.getPlayerCount());
		
		ArrayList<Interacter> interacters = new ArrayList<>();
		interacters.add(h1);
		interacters.add(c1);
		interacters.add(c2);
		
		FragmentManager fm = null;
		for(Interacter i: interacters){
			i.clickButton(R.id.roles_show_Players);
			fm = i.getEnvironment().getActive().getFragmentManager();
			PlayerPopUp pops = (PlayerPopUp) fm.get("playerlist");
			assertEquals(3, pops.players.length());
			pops.dismiss();
		}
		
		h1.addRole(BasicRoles.Agent(), "Mafia");
		
		for(Interacter i: interacters){
			assertEquals(1, ((ActivityCreateGame) i.getEnvironment().getActive()).rolesListLV.size());
		}
		ActivityCreateGame hac = (ActivityCreateGame) h1.getEnvironment().getActive();
		ActivityCreateGame cac1 = (ActivityCreateGame) c1.getEnvironment().getActive();
		
		hac.rolesListLV.click(0);
		
		for(Interacter i: interacters){
			assertEquals(0, ((ActivityCreateGame) i.getEnvironment().getActive()).rolesListLV.size());
		}
		
		
		
		c1.clickFaction("Town");
		c1.clickListing(((ActivityCreateGame) c1.getEnvironment().getActive()).rolesLV, "Escort");
		CheckBox cBox = cac1.getManager().screenController.cBox[0];
		
		h1.clickFaction("Town");
		h1.clickListing(hac.rolesLV, "Escort");
		CheckBox hBox = hac.getManager().screenController.cBox[0];
		boolean prevValue = hBox.isChecked();
		hBox.toggle();
		
		assertEquals(!prevValue, hBox.isChecked());
		assertEquals(!prevValue, cBox.isChecked());
		
		h1.addTeam("Bro", "#5500F9");
		
		for(Interacter i: interacters){
			assertEquals(6, ((ActivityCreateGame) i.getEnvironment().getActive()).cataLV.size());
		}
		
		h1.addRole(BasicRoles.Citizen(), "Town");
		h1.addRole(BasicRoles.Witch(), "Neutrals");
		
		h1.clickStart();
		
		assertEquals(h1.getEnvironment().getClass(), ActivityDay.class);
	}
	
	//isalivetests in the player drawer.  it should list the user's name, if alive, else nope.
}