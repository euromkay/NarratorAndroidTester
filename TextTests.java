package iowrapper;

import java.util.ArrayList;

import android.texting.CommunicatorText;
import android.texting.TextController;
import android.texting.TextHandler;
import android.texting.TextInput;
import android.widget.TextView;
import json.JSONException;
import json.JSONObject;
import junit.framework.TestCase;
import nnode.Instance;
import nnode.NodeSwitch;
import nnode.WebPlayer;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.Team;
import shared.logic.support.rules.Rules;
import shared.logic.templates.BasicRoles;
import shared.roles.Citizen;
import shared.roles.SerialKiller;
import voss.narrator.R;

public class TextTests extends TestCase{
	
	public void testEndNight(){
		IOWrapper wrap = new IOWrapper();
		Host h = wrap.startHost();
		h.newPlayer("Charles");
		h.addRole(BasicRoles.Citizen(), "Town");
		h.addRole(BasicRoles.Citizen(), "Town");
		h.addRole(BasicRoles.Mafioso(), "Mafia");
		TextClient brian = wrap.startTexter("A", "1", h.getEnvironment());
		
		h.clickStart();
		
		ArrayList<String> bMessages = brian.getMessages();
		assertFalse(bMessages.get(0).equals(bMessages.get(1)));
		assertEquals(10, bMessages.size());
		
		PlayerList players = h.getNarrator().getAllPlayers();
		Player host = players.getPlayerByName(h.getName());
		Player charles = players.getPlayerByName("Charles");
		
		h.getController().endNight(host);
		assertTrue(host.endedNight());
		h.getController().endNight(charles);
		assertTrue(charles.endedNight());
		
		brian.text("End niGht ");
		
		
		assertTrue(h.getNarrator().isDay());
		
		TextView dayLabel = (TextView) h.getController().dScreen.findViewById(R.id.day_title);
		assertEquals(dayLabel.getText().toString(), "<u>Day 1</u>");
	}
	
	public void testTextSplitFunctionality(){
		String text;
		ArrayList<String> splits;
		
		text = "Haosifj asd oisfjoisa sodfjiasdf. asd oisfjoisa sodfjiasdf. asd oisfjoisa sodfjiasdf. Haosifj asd oisfjoisa sodfjiasdf. Haosifj asd oisfjoisa sodfjiasdf. Haosifj asd oisfjoisa sodfjiasdf. Haosifj asd oisfjoisa sodfjiasdf. ";
		splits = split(text);
		
		assertEquals(2, splits.size());
		
		for(String s: splits){
			assertFalse(s.contains("\n"));
		}
	}
	
	public void testMafiaDeadSend(){
		Narrator n = Narrator.Default();
		n.addPlayer("A");
		n.addPlayer("B");
		n.addPlayer("C");
		n.addPlayer("D");
		TextHandler th = new TextHandler(n, n.getAllPlayers());
		TextController tc = new TextController(new TextInput(){

			public void text(Player p, String message, boolean sync) {
				th.text(p, message, sync);
			}
		});
		
		n.addRole(BasicRoles.Citizen());
		n.addRole(BasicRoles.Mafioso());
		n.addRole(BasicRoles.Mafioso());
		n.addRole(BasicRoles.Mafioso());
		
		n.getRules().setBool(Rules.DAY_START, Narrator.DAY_START);
		n.startGame();
		
		PlayerList pl = n.getAllPlayers();
		Player cit = null;
		for(Player p: pl.copy()){
			if(p.is(Citizen.class)){
				cit = p;
				pl.remove(p);
				break;
			}
		}
		
		Player toLynch = pl.getFirst();
		pl.remove(toLynch);
		
		for(Player p: pl)
			p.vote(toLynch);
		//cit.vote(toLynch);
		th.text(cit, "vote " + toLynch.getName().toLowerCase(), false);
		
		assertTrue(n.isNight());
		
		tc.setNightTarget(toLynch, cit, Team.KILL);
	}
	
	private ArrayList<String> split(String text){
		return CommunicatorText.splitMessages(text);
	}
	
	public void testInstancePreferring(){
		Narrator n = Narrator.Default();
		Player a = n.addPlayer("A");
		n.addPlayer("B");
		n.addPlayer("C");
		
		n.addRole(BasicRoles.Citizen());
		n.addRole(BasicRoles.Mafioso());
		n.addRole(BasicRoles.SerialKiller());
		
		NodeSwitch ns = new NodeSwitch();
		Instance i = new Instance(ns);
		
		
		try {
			JSONObject jo = new JSONObject();
			jo.put("message", "say null -prefer Serial Killer");
			jo.put("name", "A");
			WebPlayer np = new WebPlayer("A", ns);
			np.player = a;
			i.handlePlayerMessage(np, jo);
		} catch (JSONException e) {
			e.printStackTrace();
			fail();
		}
		
		n.startGame();
		
		assertTrue(a.is(SerialKiller.class));
	}
}
