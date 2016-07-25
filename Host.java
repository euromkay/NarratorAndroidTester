package iowrapper;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.wifi.SocketHost;
import shared.ai.Computer;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.exceptions.IllegalGameSettingsException;
import shared.logic.support.Random;
import shared.logic.support.RoleTemplate;
import voss.narrator.R;

public class Host extends Interacter{

	private Random rand;
	public Host(Environment e, Random rand, WrapSynchr ws) {
		super(e, ws);
		this.rand = rand;
	}
	
	public void clickHost(){
		clickButton(R.id.home_host);
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
	
	public void clickStart(){
		clickStart(null);
	}
	public void clickStart(Long seed) {
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		if(seed != null)
			ac.getManager().ns.local.setSeed(seed);
		((Button)ac.findViewById(R.id.roles_startGame)).click();
		
		if(!getNarrator().isInProgress()){
			throw new IllegalGameSettingsException("Probably caused because roles were messed up.");
		}
		
		Interacter i;
		while((i = ws.notOnActivityDayYet()) != null){
			Log.m("Host", "waiting for " + i.getName() + " to change to activity day");
		}
	}

	public void addRole(RoleTemplate role, String factionName) {

		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();

		clickListing(ac.cataLV, factionName);
		clickListing(ac.rolesLV, role.getName());
			
		
		
		
		Interacter i;
		while((i = ws.missingRoles(getNarrator().getAllRoles().size())) != null){
			Log.m("Host", "waiting for " + i.getName() + " to add " + role.getName());
		}
	}

	public void addRandomRole() {
		addRole(SetupManager.getRandomRole(rand), "Randoms");
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
		clickButton(R.id.roles_show_Players);
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
		popUp.lv.click(popUp.players.indexOf(x));		
		popUp.dismiss();
		
	}
	
	public void nightStart(){
		ActivityCreateGame ac = (ActivityCreateGame) getEnvironment().getActive();
		clickListing(ac.cataLV, "Randoms");
		SetupScreenController sc = ac.getManager().screenController;
		
		CheckBox cb = sc.cBox[0];
		if(!cb.isChecked())
			return;
		cb.toggle();
	}

	public void dayStart() {
		ActivityCreateGame ac = (ActivityCreateGame) getEnvironment().getActive();
		clickListing(ac.cataLV, "Randoms");
		SetupScreenController sc = ac.getManager().screenController;
		
		CheckBox cb = sc.cBox[0];
		if(cb.isChecked())
			return;
		cb.toggle();
	}

	public void setAllComputers() {
		ActivityCreateGame ac = (ActivityCreateGame) e.getActive();
		ac.onClick(new View(R.id.roles_show_Players));
		
		PlayerPopUp popUp = (PlayerPopUp) ac.getFragmentManager().get("playerlist");
		
		EditText et = (EditText) popUp.mainView.findViewById(R.id.addPlayerContent);
		et.setText(PlayerPopUp.COMPUTER_COMMAND);
		
		Button addButton = (Button) popUp.mainView.findViewById(R.id.addPlayerConfirm);
		addButton.click();
		
	}

	
}
