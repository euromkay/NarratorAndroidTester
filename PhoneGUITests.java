package iowrapper;

import android.GUIController;
import android.alerts.TeamBuilder;
import android.alerts.TeamEditor;
import android.graphics.Color;
import android.screens.ListingAdapter;
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
import shared.logic.Team;
import shared.logic.exceptions.PlayerTargetingException;
import shared.logic.support.Constants;
import shared.logic.support.FactionManager;
import shared.logic.support.Random;
import shared.logic.support.rules.Rules;
import shared.logic.templates.BasicRoles;
import shared.roles.Citizen;
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

	public void testNoFactionSelected(){
		IOWrapper wrap = new IOWrapper();
		Host h = wrap.startHost();
		
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		assertEquals(ac.rolesLV.getVisibility(), View.GONE);
		assertEquals(ac.findViewById(R.id.create_info_wrapper).getVisibility(), View.GONE);
		h.clickFaction("Town");
		assertEquals(ac.findViewById(R.id.create_info_wrapper).getVisibility(), View.VISIBLE);
		assertEquals(ac.rolesLV.getVisibility(), View.VISIBLE);
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
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#FFFFFF"));
		
		tb.colorInput.setText("3f4");
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#33FF44"));
		
		tb.colorInput.setText("#44444");
		((Button) tb.mainView.findViewById(R.id.newTeam_submit)).click();
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#FF0000"));
		assertEquals(tb.preview.getText().toString(), TeamBuilder.RGB_ERROR_CODE);
		
		tb.nameInput.setText("Town");
		tb.colorInput.setText("0FF");
		((Button) tb.mainView.findViewById(R.id.newTeam_submit)).click();
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#FF0000"));
		assertEquals(tb.preview.getText().toString(), FactionManager.TEAM_TAKEN);
		
		tb.nameInput.setText("Bro");
		tb.colorInput.setText(Constants.A_ARSONIST);
		((Button) tb.mainView.findViewById(R.id.newTeam_submit)).click();
		assertEquals(tb.preview.getCurrentTextColor(), Color.parseColor("#FF0000"));
		assertEquals(tb.preview.getText().toString(), FactionManager.COLOR_TAKEN);
		
		tb.colorInput.setText("#FFE");
		((Button) tb.mainView.findViewById(R.id.newTeam_submit)).click();
		assertEquals(ac.ns.fManager.factions.size(), ac.cataLV.size());
		
		CheckBox cb = ac.getManager().screenController.cBox[0];
		assertEquals("Has Faction kill", cb.getText());
	}
	
	public void testButtonVisibility(){
		IOWrapper wrap = new IOWrapper();
		Host h = wrap.startHost();
		
		trioInvisible(h);
		h.clickFaction("Town");
		trioInvisible(h);
		h.clickFaction("Mafia");
		trioInvisible(h);
		h.clickFaction("Randoms");
		trioInvisible(h);
		h.addRole(BasicRoles.BusDriver(), "Town");
		trioInvisible(h);
		
		h.addTeam("Bro", "#3FA");
		
		trioVisible(h);
	}
	
	private void trioVisible(Host h){
		testVisibility(h, View.VISIBLE);
	}
	
	private void trioInvisible(Host h){
		testVisibility(h, View.GONE);
	}
	
	private void testVisibility(Host h, int visibility){
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		Button editAlly = (Button) ac.findViewById(R.id.create_editAlliesButton);
		Button editRoles = (Button) ac.findViewById(R.id.create_editMembersButton);
		Button deleteTeam = (Button) ac.findViewById(R.id.create_deleteTeamButton);
		
		assertEquals(editAlly.getVisibility(), visibility);
		assertEquals(editRoles.getVisibility(), visibility);
		assertEquals(deleteTeam.getVisibility(), visibility);
	}
	
	public void testDeleteTeam(){
		IOWrapper wrap = new IOWrapper();
		Host h = wrap.startHost();
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();

		int size = ac.ns.fManager.factions.size();
		
		h.addTeam("Bro", "#3FA");
		assertEquals(size + 1, ac.cataLV.size());
		
		h.clickButton(R.id.create_deleteTeamButton);
		assertEquals(size, ac.cataLV.size());
	}
	
	public void testEditAllies(){
		IOWrapper wrap = new IOWrapper();
		Host h = wrap.startHost();
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		
		h.addTeam("Bro", "#3FA");
		Team broTeam = ac.ns.local.getTeam("#33FFAA");
		
		h.clickButton(R.id.create_editAlliesButton);

		TeamEditor te = (TeamEditor) ac.getFragmentManager().get("editTeam");
		assertEquals(te.getDialog().getTitle(), TeamEditor.EDITING_ALLIES_TITLE);
		
		//test headers
		assertEquals(((TextView) te.mainView.findViewById(R.id.editTeamTV1)).getText().toString(), TeamEditor.ALLIES_TITLE);
		assertEquals(((TextView) te.mainView.findViewById(R.id.editTeamTV2)).getText().toString(), TeamEditor.ENEMIES_TITLE);
		
		ListingAdapter la = (ListingAdapter) te.l1.adapter;
		String color = la.colors.get(0);
		Team newEnemyTeam = ac.ns.local.getTeam(color);
		
		assertFalse(newEnemyTeam.isEnemy(broTeam));
		te.l1.click(0);
		assertTrue(newEnemyTeam.isEnemy(broTeam));
		
		te.l2.click(0);
		assertFalse(newEnemyTeam.isEnemy(broTeam));
	}
	
	public void testEditRoles(){
		IOWrapper wrap = new IOWrapper();
		Host h = wrap.startHost();
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		
		h.addTeam("Bro", "#3FA");
		h.clickButton(R.id.create_editMembersButton);

		TeamEditor te = (TeamEditor) ac.getFragmentManager().get("editTeam");
		assertEquals(te.getDialog().getTitle(), TeamEditor.EDITING_ROLES_TITLE);
		
		//test headers
		assertEquals(((TextView) te.mainView.findViewById(R.id.editTeamTV1)).getText().toString(), TeamEditor.AVAILABLE_ROLES_TITLE);
		assertEquals(((TextView) te.mainView.findViewById(R.id.editTeamTV2)).getText().toString(), TeamEditor.BLACKLISTED_ROLES_TITLE);
		
		ListingAdapter la2 = (ListingAdapter) te.l2.adapter;
		ListingAdapter la1 = (ListingAdapter) te.l1.adapter;
		int posOfBD = la2.data.indexOf(BasicRoles.BUS_DRIVER);
		assertFalse(la1.data.contains(BasicRoles.BUS_DRIVER));
		assertTrue(la2.data.contains(BasicRoles.BUS_DRIVER));
		
		te.l2.click(posOfBD);

		la2 = (ListingAdapter) te.l2.adapter;
		la1 = (ListingAdapter) te.l1.adapter;
		assertFalse(la2.data.contains(BasicRoles.BUS_DRIVER));
		assertTrue(la1.data.contains(BasicRoles.BUS_DRIVER));
	}
	
	public void testDeleteTeamPurgeRole(){
		IOWrapper wrap = new IOWrapper();
		Host h = wrap.startHost();
		ActivityCreateGame ac = (ActivityCreateGame) h.getEnvironment().getActive();
		
		h.addTeam("Bro", "#3FA");
		h.clickButton(R.id.create_editMembersButton);
		
		TeamEditor te = (TeamEditor) ac.getFragmentManager().get("editTeam");
		ListingAdapter la2 = (ListingAdapter) te.l2.adapter;
		int posOfCit = la2.data.indexOf(Citizen.ROLE_NAME);
		
		te.l2.click(posOfCit);//get it? piece of poop
		te.l2.click(0);
		
		h.clickButton(R.id.editTeamConfirm);
		h.addRole(BasicRoles.Citizen().setColor("#33FFAA"), "Bro");
		
		h.clickFaction("Randoms");
		ListingAdapter la = (ListingAdapter) ac.rolesLV.adapter;
		int broIndex = la.data.indexOf("Bro Random");
		ac.rolesLV.click(broIndex);
		
		assertEquals(2, ac.rolesListLV.size());
		
		h.clickFaction("Bro");
		h.clickButton(R.id.create_deleteTeamButton);
		
		assertEquals(0, ac.rolesListLV.size());
	}
	//on deleting a team, the roles list should be purged completely
}