package iowrapper;

import android.parse.Server;
import junit.framework.TestCase;
import voss.narrator.R;

public class ServerTests extends TestCase{

	
	private static SwitchWrapper nSwitch;
	private void startSwitch(){
		nSwitch = new SwitchWrapper();
	}
	public void testServerJoinGame(){
		startSwitch();
		IOWrapper wrap = new IOWrapper();
		Host h1 = wrap.initHost();
		assertFalse(Server.IsLoggedIn());
		
		h1.login(0);
		assertTrue(Server.IsLoggedIn());
		
		h1.clickButton(R.id.home_join);
	}
}