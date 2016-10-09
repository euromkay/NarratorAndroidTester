package iowrapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.firebase.auth.FirebaseAuth;

import android.app.Activity;
import android.app.Environment;
import android.app.Environment.EnvironmentListener;
import android.content.Intent;
import android.os.Bundle;
import android.screens.ActivityHome;
import android.setup.ActivityCreateGame;
import android.telephony.SmsManager;
import android.util.Log;
import shared.ai.Brain;
import shared.ai.Controller;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.support.Random;
import voss.narrator.R;

public class IOWrapper {

	public WrapSynchr ws;
	public IOWrapper(){
		SmsManager.sms = null;
		ws = new WrapSynchr();
		envs = new ArrayList<>();
	}
	
	public ArrayList<Environment> envs;
	public Host initHost(){
		Environment e = EnvironmentCreator();
		envs.add(e);
		rand = new Random();
		setSeed(seed);
		FirebaseAuth.e = e;
		host = new Host(e, rand, ws);
		ws.add(host);
		interacters.add(host);
		return host;
	}
	
	private Brain brain;
	private Host host;
	private Random rand;
	private Long seed;
	
	public Host startHost(Long l){
		return startHost(l, -1);
	}
	
	public Host startHost(int i){
		return startHost(null, i);
	}
	
	public Host startHost(){
		return startHost(null, -1);
	}
	public Host startHost(Long seed, int login){
		initHost();
		if(login >= 0)
			host.login(login);
		host.clickHost();
		host.clickNameHost();
		setSeed(seed);
		
		
		return host;
	}
	
	public void setSeed(Long l){
		if(rand != null && l != null)
			rand.setSeed(l);
	}

	private Environment EnvironmentCreator(){
		final Environment e = new Environment(new EnvironmentListener(){
			public void onActivityChange(Activity a, Environment e){
				//NActivity na = (NActivity) a;
				//na.server.Destroy();
				FirebaseAuth.e = e;
			}
		});
		
		e.addView(R.id.day_horizontalShimmy);
		e.addView(R.id.create_info_wrapper);
		e.addScrollView(R.id.create_chatHolder);
		e.addView(R.layout.create_team_builder_layout);
		
		e.addButton(R.id.home_join);
		e.addTextView(R.id.create_info_description, R.id.create_chatTV);
		e.addButton(R.id.home_host);
		e.addButton(R.id.home_login_signup);
		e.addTextView(R.id.home_tutorial);
		e.addTextView(R.id.home_roleCard, R.id.create_info_label);

		e.addRecyclerView(R.id.day_playerNavigationPane);

		e.addEditText(R.id.day_chatET, R.id.create_rulesET1, R.id.create_rulesET2, R.id.create_chatET);
		
		e.addScrollView(R.id.day_chatHolder);
		
		e.addListView(R.id.day_rolesList, R.id.roles_categories_LV, R.id.day_alliesList);
		e.addListView(R.id.roles_bottomLV, R.id.day_membersLV);
		e.addListView(R.id.roles_rolesList);
		e.addListView(R.id.day_actionList);
		
		e.addTextView(R.id.roles_categories_title, R.id.day_chatTV, R.id.day_roleLabel, R.id.day_alliesLabel);
		e.addTextView(R.id.roles_bottomLV_title, R.id.newTeam_previewText);
		e.addTextView(R.id.day_membersLabel, R.id.day_title);
		e.addTextView(R.id.day_commandsLabel, R.id.day_rolesList_label);
		e.addTextView(R.id.roles_rightLV_title, R.id.create_rulesTV1, R.id.create_rulesTV2);
		e.addTextView(R.id.roles_hint_title);
		e.addTextView(R.id.day_currentPlayerTV, R.id.day_role_info);
		
		e.addButton(R.id.editTeamConfirm);
		e.addButton(R.id.create_editAlliesButton);
		e.addButton(R.id.create_editMembersButton);
		e.addButton(R.id.create_deleteTeamButton);
		e.addButton(R.id.create_createTeamButton);
		e.addButton(R.id.create_toChat);
		e.addButton(R.id.create_chatButton);
		e.addButton(R.id.roles_show_Players);
		e.addButton(R.id.roles_startGame);
		e.addButton(R.id.day_messagesButton);
		e.addButton(R.id.day_infoButton);
		e.addButton(R.id.day_button);
		e.addButton(R.id.day_playerDrawerButton);
		e.addButton(R.id.day_actionButton);
		e.addButton(R.id.day_chatButton);
		
		e.addDrawerLayout(R.id.day_main);
		
		e.addSpinner(R.id.day_frameSpinner);
		
		e.addCheckBox(R.id.create_check1, R.id.create_check2, R.id.create_check3, R.id.create_check4);
		
		
		HashMap<Integer, String[]> resources = new HashMap<>();
	
		
		e.setResouces(resources);
		
		
		return e;
	}
	
	public Client initClient(){
		Environment e = EnvironmentCreator();
		envs.add(e);
		Client c = new Client(e, ws);
		ws.add(c);
		return c;
	}
	
	public Client startClient(String name) {
		Client c = initClient();
		c.clickJoin();
		c.name = name;
		c.connect(host.getIP());
		
		ActivityHome ah = (ActivityHome) c.getEnvironment().getActive();
		
		while(ah.getFragmentManager().getFragment(null, "namePrompt") == null)
			Log.m("Client (IOWrapper)", "waiting for connect to host by detecting nameprompt");;
		
		c.setName(name);
		c.clickNameJoin();
		
		Interacter i;
		while((i = ws.missingPlayer(name)) != null){
			Log.m("Interacter",  i.getName() + " waiting for " + name);
		}
		
		interacters.add(c);
		
		return c;
	}

	public TextClient startTexter(String name, String number, Environment e) {
		Intent intent = new Intent();
		
		Bundle b = new Bundle();
		intent.setBundle(b);
		b.putString("number", number);
		b.putString("message", name);
		
		((ActivityCreateGame) host.e.getActive()).getManager().textAdder.onReceive(host.e.getActive(), intent);
		TextClient i = new TextClient(number, e);
		interacters.add(i);
		
		return i;
		
	}

	private ArrayList<Interacter> interacters = new ArrayList<Interacter>();
	public void startBrain(){
		
		interacters.remove(host);
		interacters.add(host);
		//host shouldn't readd people
		

		HashMap<Player, Controller> controls = new HashMap<>();
		for(Interacter inc: interacters){
			for(Player p: inc.getPlayers()){
				controls.put(p,  inc.getController());
			}
		}
		brain = new Brain(controls, rand);
		if(!host.getActivity().ns.server.IsLoggedIn()){
			brain.setNarrator(host.getActivity().ns.local);
		}
		
	}
	public Brain getBrain(){
		return brain;
	}
	public void doActions() {
		boolean day = host.getNarrator().isDay();
		if(day){
			brain.dayAction();
		}else
			brain.nightAction();
		
		if(!host.getNarrator().isInProgress() || ws.interacters.size() == 1)
			return;
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {}
	}

	public void start() {
		host.getNarrator().setSeed(seed);
		host.clickStart(seed);
		
	}

	public void close(){
		for(Interacter i: interacters){
			i.close();
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
	}

	public ArrayList<Narrator> getNarrators() {
		ArrayList<Narrator> list = new ArrayList<>();
		for(Interacter i: interacters)
			list.add(i.getNarrator());
		return list;
	}
	
	
}
