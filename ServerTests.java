package iowrapper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.client.WebSocketClient.WebListener;
import org.json.JSONException;

import android.parse.Server;
import junit.framework.TestCase;
import voss.narrator.R;

public class ServerTests extends TestCase{

	
	private static SwitchWrapper nSwitch;
	private void startSwitch(){
		nSwitch = new SwitchWrapper();
	}
	
	private void pseudoConnect(Interacter i){
		WebSocketClient.wl = new WebListener(){
			public void onMessageReceive(String message) {
				try {
					nSwitch.nSwitch.handleMessage(message);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	public void testJoinGame(){
		startSwitch();
		IOWrapper wrap = new IOWrapper();
		Host h1 = wrap.initHost();
		assertFalse(Server.IsLoggedIn());
		
		pseudoConnect(h1);
		
		h1.login(0);
		assertTrue(Server.IsLoggedIn());
		
		h1.clickButton(R.id.home_join);
		assertEquals(nSwitch.nSwitch.instances.get(0).host.getName(), h1.getName());
	}
	
	public void testHostGame(){
		startSwitch();
		IOWrapper wrap = new IOWrapper();
		Host h1 = wrap.initHost();
		assertFalse(Server.IsLoggedIn());
		
		pseudoConnect(h1);
		
		h1.login(0);
		assertTrue(Server.IsLoggedIn());
		
		h1.clickButton(R.id.home_host);
		assertEquals(nSwitch.nSwitch.instances.get(0).host.getName(), h1.getName());
	}
}