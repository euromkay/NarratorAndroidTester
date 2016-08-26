package iowrapper;

import java.util.ArrayList;

import android.texting.CommunicatorText;
import android.widget.TextView;
import junit.framework.TestCase;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.templates.BasicRoles;
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
		assertEquals(5, bMessages.size());
		
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
		
		//new lines shouldn't be in the text that is sent
	}
	
	private ArrayList<String> split(String text){
		return CommunicatorText.splitMessages(text);
	}
}
