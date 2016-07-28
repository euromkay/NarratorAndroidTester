package iowrapper;

import java.util.ArrayList;

import android.widget.TextView;
import junit.framework.TestCase;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.templates.BasicRoles;
import voss.narrator.R;

public class TextTests  extends TestCase{
	
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
		assertEquals(3, bMessages.size());
		
		PlayerList players = h.getNarrator().getAllPlayers();
		Player host = players.getPlayerByName(h.getName());
		Player charles = players.getPlayerByName("Charles");
		
		h.getController().endNight(host);
		h.getController().endNight(charles);
		
		brian.text("End night");
		
		assertTrue(h.getNarrator().isDay());
		
		TextView dayLabel = (TextView) h.getController().dScreen.findViewById(R.id.day_title);
		assertEquals(dayLabel.getText().toString(), "<u>Day 1</u>");
	}

}
