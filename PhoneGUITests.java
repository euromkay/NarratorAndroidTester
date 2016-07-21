package iowrapper;

import android.GUIController;
import android.setup.ActivityCreateGame;
import android.setup.SetupScreenController;
import android.widget.CheckBox;
import junit.framework.TestCase;
import shared.ai.Computer;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.exceptions.PlayerTargetingException;
import shared.logic.support.Random;
import shared.logic.support.rules.Rules;
import shared.logic.templates.BasicRoles;
import shared.roles.Driver;

public class PhoneGUITests extends TestCase{

	
	public PhoneGUITests(String name) {
		super(name);
	}
	
	public void testDriverAbilityTest(){
		IOWrapper wrap = new IOWrapper();
		
		Host h1 = wrap.startHost();  //no seed means no setting what seed we're using
		h1.newPlayer("BD1");
		h1.newPlayer("BD2");
		
		h1.addRole(BasicRoles.BusDriver(), "Town");
		h1.addRole(BasicRoles.BusDriver(), "Town");
		h1.addRole(BasicRoles.Chauffeur(), "Mafia");
		
		h1.nightStart();
		h1.clickStart();
		
		
		GUIController c = (GUIController) h1.getController();
		c.rand = new Random();
		c.rand.setSeed(0);
		
		PlayerList pl = h1.getPlayers();
		Player p1 = pl.getFirst(), p2 = pl.get(1);
		
		c.selectSlave(p1);
		assertTrue(c.dScreen.playerLabelTV.getText().equals(p1.getName()));
		
		
		c.swipeAbilityPanel(Driver.TEXT1);
		assertEquals(3, c.dScreen.actionList.size());
		c.setNightTarget(p1, p2, Driver.TEXT1);
		assertEquals(3, c.dScreen.actionList.size());
		

		c.swipeAbilityPanel(Driver.TEXT2);
		assertEquals(2, c.dScreen.actionList.size());
		assertFalse(p2.in(c.dScreen.actionList));
		try{
			c.setNightTarget(p1, p2, Driver.TEXT2);
			fail();
		}catch(PlayerTargetingException e){}
		assertEquals(2, c.dScreen.actionList.size());
	}
	
	
	
	public void testDoubleClick(){
		IOWrapper wrap = new IOWrapper();
		
		Host h = wrap.startHost();
		
		int playerSize = 15;
		
		for(int i = 0; i < playerSize - 1; i++)
			h.newPlayer(Computer.toLetter(i+2));
		
		h.setAllComputers();
		for(Player comp : h.getPlayers()){
			assertTrue(comp.isComputer());
		}
		
		h.addRole(BasicRoles.Framer(), "Mafia");
		h.addRole(BasicRoles.BusDriver(), "Town");
		
		for(int i = 0; i < playerSize - 2; i++){
			h.addRandomRole();
		}
		
		h.clickStart();
		
		assertTrue(h.getNarrator().isInProgress());
		
		while(h.getNarrator().isInProgress())
			h.doubleClick();
	}
	
	public void testRuleText(){
		IOWrapper wrap = new IOWrapper();
		
		Host h = wrap.startHost();
		
		h.addRole(BasicRoles.Consort(), "Mafia");
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		SetupScreenController sc = ac.getManager().screenController;
				
		
		
		CheckBox blockCB = sc.cBox[0];
		boolean prevVal = blockCB.isChecked();
		assertEquals(h.getNarrator().getRules().getBool(Rules.ROLE_BLOCK_IMMUNE), prevVal);
		blockCB.toggle();
		assertEquals(h.getNarrator().getRules().getBool(Rules.ROLE_BLOCK_IMMUNE), blockCB.isChecked());
		
		//click, test if value has changed
	}
	
	public void testRemoveRole(){
		IOWrapper wrap = new IOWrapper();
		
		Host h = wrap.startHost();
		
		h.addRole(BasicRoles.Consort(), "Mafia");
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		ac.rolesListLV.click(0);
		
		assertTrue(h.getNarrator().getAllRoles().isEmpty());
	}
}