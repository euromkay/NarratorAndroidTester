package iowrapper;

import android.GUIController;
import android.alerts.TeamBuilder;
import android.graphics.Color;
import android.setup.ActivityCreateGame;
import android.setup.SetupScreenController;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import junit.framework.TestCase;
import shared.ai.Computer;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.exceptions.PlayerTargetingException;
import shared.logic.support.Constants;
import shared.logic.support.FactionManager;
import shared.logic.support.Random;
import shared.logic.support.rules.Rules;
import shared.logic.templates.BasicRoles;
import shared.roles.Driver;
import voss.narrator.R;

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
				
		TextView ruleLabel = (TextView) ac.findViewById(R.id.create_info_label);
		TextView ruleDescrip = (TextView) ac.findViewById(R.id.create_info_description);
		assertEquals(BasicRoles.CONSORT, ruleLabel.getText());
		assertEquals(ruleLabel.getCurrentTextColor(), Color.parseColor(Constants.A_MAFIA));
		
		CheckBox blockCB = sc.cBox[0];
		boolean prevVal = blockCB.isChecked();
		assertEquals(h.getNarrator().getRules().getBool(Rules.ROLE_BLOCK_IMMUNE), prevVal);
		blockCB.toggle();
		assertEquals(h.getNarrator().getRules().getBool(Rules.ROLE_BLOCK_IMMUNE), blockCB.isChecked());
		
		h.clickFaction("Mafia");
		assertEquals(ruleLabel.getText().toString(), ac.ns.fManager.getFaction(Constants.A_MAFIA).getName());
		assertEquals(ruleDescrip.getText().toString(), ac.ns.fManager.getFaction(Constants.A_MAFIA).getDescription());
		assertEquals(ruleDescrip.getVisibility(), View.VISIBLE);
		
		h.clickFaction("Randoms");
		assertEquals(ruleLabel.getVisibility(), View.GONE);
		assertEquals(ruleDescrip.getText().toString(), ac.ns.fManager.getFaction(Constants.A_RANDOM).getDescription());
		assertEquals(Rules.DAY_START[1], blockCB.getText().toString());
		blockCB.toggle();
		boolean dayStart = blockCB.isChecked();
		
		h.clickFaction("Neutrals");
		assertEquals(ruleLabel.getVisibility(), View.VISIBLE);
		
		h.newPlayer("J");
		h.newPlayer("R");
		h.addRole(BasicRoles.Agent(), "Mafia");
		h.addRole(BasicRoles.Citizen(), "Town");
		h.clickStart();
		
		assertEquals(dayStart, h.getNarrator().isDay());
	}
	
	public void testRemoveRole(){
		IOWrapper wrap = new IOWrapper();
		
		Host h = wrap.startHost();
		
		h.addRole(BasicRoles.Consort(), "Mafia");
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		ac.rolesListLV.click(0);
		
		assertTrue(h.getNarrator().getAllRoles().isEmpty());
	}

	public void testNewTeamButton(){
		IOWrapper wrap = new IOWrapper();
		Host h = wrap.startHost();
		
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		h.clickButton(R.id.create_createTeamButton);
		TeamBuilder tb = (TeamBuilder) ac.getFragmentManager().get("newTeam");
		
		tb.nameInput.setText("Bro");
		tb.colorInput.setText("3g4");
		assertEquals(tb.preview.getText().toString(), "Bro");
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#FFF"));
		
		tb.colorInput.setText("3f4");
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#3F4"));
		
		tb.colorInput.setText("#44444");
		((Button) tb.mainView.findViewById(R.id.newTeam_submit)).click();
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#F00"));
		assertEquals(tb.preview.getText().toString(), TeamBuilder.RGB_ERROR_CODE);
		
		tb.nameInput.setText("Town");
		tb.colorInput.setText("0FF");
		((Button) tb.mainView.findViewById(R.id.newTeam_submit)).click();
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#F00"));
		assertEquals(tb.preview.getText().toString(), FactionManager.TEAM_TAKEN);
		
		tb.nameInput.setText("Bro");
		tb.colorInput.setText(Constants.A_ARSONIST);
		((Button) tb.mainView.findViewById(R.id.newTeam_submit)).click();
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#F00"));
		assertEquals(tb.preview.getText().toString(), FactionManager.COLOR_TAKEN);
		
		tb.colorInput.setText("#FFE");
		((Button) tb.mainView.findViewById(R.id.newTeam_submit)).click();
		assertEquals(ac.ns.fManager.factions.size(), ac.cataLV.size());
	}
	
	public void testDayNightLocal(){
		
	}
	
}