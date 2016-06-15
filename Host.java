package iowrapper;

import java.util.Arrays;
import java.util.Random;

import android.CommunicatorPhone;
import android.GUIController;
import android.alerts.NamePrompt;
import android.alerts.PhoneBookPopUp;
import android.alerts.PlayerPopUp;
import android.app.Environment;
import android.day.ActivityDay;
import android.screens.ActivityHome;
import android.setup.ActivityCreateGame;
import android.setup.SetupManager;
import android.setup.SetupScreenController;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.wifi.SocketHost;
import shared.ai.Computer;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.exceptions.IllegalGameSettingsException;
import shared.logic.support.Constants;
import shared.logic.support.RoleTemplate;
import voss.narrator.R;

public class Host extends Interacter{

	private Random rand;
	public Host(Environment e, Random rand, WrapSynchr ws) {
		super(e, ws);
		this.rand = rand;
	}
	
	public void clickHost(){
		ActivityHome aH = (ActivityHome) e.getActive();
		aH.onClick(new View(R.id.home_host));
	}

	public void clickNameHost() {
		ActivityHome aH = (ActivityHome) e.getActive();
		
		NamePrompt np = (NamePrompt) aH.getFragmentManager().get("namePrompt");
		EditText et = (EditText) np.mainView.findViewById(R.id.home_nameET);
		et.setText("Master");
		super.setName("Master");
		
		np.posClick();
		
		PhoneBookPopUp pop = (PhoneBookPopUp) aH.getFragmentManager().get("phoneBookPopup");
		pop.onClick(null);
		
		
	}
	
	public String getIP(){
		
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		return ac.rolesLeftTV.getText();
	}

	public boolean notBroadcasting(){
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		SocketHost sh = (SocketHost) ac.getManager().ns.socketHost;
		if(sh == null)
			return true;
		return !sh.isLive();
	}
	
	public void clickStart(long seed) {
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		ac.getManager().ns.local.setSeed(seed);
		ac.onClick(new View(R.id.roles_startGame));
		
		if(!getNarrator().isInProgress()){
			throw new IllegalGameSettingsException("Probably caused because roles were messed up.");
		}
		
		Interacter i;
		while((i = ws.notOnActivityDayYet()) != null){
			Log.m("Host", "waiting for " + i.getName() + " to change to activity day");
		}
	}

	

	public void addRole(RoleTemplate randomRole) {

		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		
		String color = randomRole.getColor();
		AdapterView<?> v = new AdapterView(R.id.roles_categories_LV);
		AdapterView<?> v2 = new AdapterView(R.id.roles_bottomLV);
		int id;
		if(randomRole.isRandom()){
			ac.onItemClick(v, null, ActivityCreateGame.RANDOM, 0);
			id = R.array.roles_randomRoles;
		}else if(color == Constants.A_TOWN){
			ac.onItemClick(v, null, ActivityCreateGame.TOWN, 0);
			id = R.array.roles_townRoles;
		}else if(color == Constants.A_MAFIA){
			ac.onItemClick(v, null, ActivityCreateGame.MAFIA, 0);
			id = R.array.roles_mafiaRoles;
		}else if(color == Constants.A_YAKUZA){
			ac.onItemClick(v, null, ActivityCreateGame.YAKUZA, 0);
			id = R.array.roles_mafiaRoles;
		}else{
			ac.onItemClick(v, null, ActivityCreateGame.NEUTRAL, 0);
			id = R.array.roles_neutralRoles;
		}	
		
		String[] array = ac.getResources().getStringArray(id);
		int click = Arrays.asList(array).indexOf(randomRole.getName());
		ac.onItemClick(v2, null, click, id);
		
		Interacter i;
		while((i = ws.missingRoles(getNarrator().getAllRoles().size())) != null){
			Log.m("Host", "waiting for " + i.getName() + " to add " + randomRole.getName());
		}
	}

	public void addRandomRole() {
		addRole(SetupManager.getRandomRole(rand));
	}

	public GUIController getController() {
		ActivityDay ad = (ActivityDay) e.getActive();
		
		return new GUIController(ad);
	}
	
	public PlayerList getPlayers() {
		PlayerList ret = new PlayerList();
		for(Player p: getNarrator().getAllPlayers()){
			if(p.getCommunicator().getClass() == CommunicatorPhone.class){
				ret.add(p);
			}
		}
		return ret;
	}

	
	
	public void newComputer() {

		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		ac.onClick(new View(R.id.roles_show_Players));
		
		PlayerPopUp popUp = (PlayerPopUp) ac.getFragmentManager().get("playerlist");
		popUp.onCreateView(ac.getLayoutInflater(), null, null);
		EditText et = (EditText) popUp.mainView.findViewById(R.id.addPlayerContent);
		String name = Computer.toLetter(ac.getNarrator().getPlayerCount()+1);
		et.setText(name);
		
		popUp.onClick(new View(0));
		
		Player x = null;;
		for (Player p: popUp.players){
			
			if(p.getName().equals(name)){
				x = p;
				break;
			}
		}
		popUp.onItemClick(null, new TextView(0), popUp.players.indexOf(x), 0);
		
		popUp.dismiss();
		
	}

	public void dayStart() {
		ActivityCreateGame ac = (ActivityCreateGame) getEnvironment().getActive();
		ac.onItemClick(ac.cataLV, null, ActivityCreateGame.RANDOM, 0);
		SetupScreenController sc = ac.getManager().screenController;
		CheckBox cb = sc.cBox[0];
		if(cb.isChecked())
			return;
		cb.setChecked(true);
		sc.onCheckedChanged(cb, false);//false is unused
			
			
	}

	
}
