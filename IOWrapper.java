package iowrapper;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Environment;
import android.content.Intent;
import android.os.Bundle;
import android.screens.ActivityHome;
import android.setup.ActivityCreateGame;
import android.util.Log;
import shared.ai.Brain;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.support.Random;
import voss.narrator.R;

public class IOWrapper {

	public WrapSynchr ws;
	public IOWrapper(){
		ws = new WrapSynchr();
	}
	
	private Brain brain;
	private Host host;
	private Random rand;
	private Long seed;
	
	public Host startHost(){
		return startHost(null);
	}
	public Host startHost(Long seed){
		Environment e = EnvironmentCreator();
		this.seed = seed;
		rand = new Random();
		if(seed != null)
			rand.setSeed(seed);
		host = new Host(e, rand, ws);
		ws.add(host);
		interacters.add(host);
		host.clickHost();
		host.clickNameHost();
		
		//ios.n = host.getNarrator();
		
		return host;
	}
	
	public void setSeed(long l){
		rand.setSeed(l);
	}

	private Environment EnvironmentCreator(){
		Environment e = new Environment();
		
		
		e.addView(R.id.day_horizontalShimmy);
		e.addView(R.id.create_info_wrapper);
		e.addView(R.id.create_chatHolder);
		
		e.addTextView(R.id.home_join);
		e.addButton(R.id.home_host);
		e.addTextView(R.id.home_login_signup);
		e.addTextView(R.id.home_tutorial);
		e.addTextView(R.id.home_currentGames, R.id.create_info_label);

		e.addRecyclerView(R.id.day_playerNavigationPane);

		e.addEditText(R.id.day_chatET, R.id.create_rulesET1, R.id.create_rulesET2, R.id.create_chatET);
		
		e.addScrollView(R.id.day_chatHolder);
		
		e.addListView(R.id.day_rolesList, R.id.roles_categories_LV, R.id.day_alliesList);
		e.addListView(R.id.roles_bottomLV, R.id.day_membersLV);
		e.addListView(R.id.roles_rolesList);
		e.addListView(R.id.day_actionList);
		
		e.addTextView(R.id.roles_categories_title, R.id.day_chatTV, R.id.day_roleLabel, R.id.day_alliesLabel);
		e.addTextView(R.id.roles_bottomLV_title);
		e.addTextView(R.id.day_membersLabel, R.id.day_title);
		e.addTextView(R.id.day_commandsLabel, R.id.day_rolesList_label);
		e.addTextView(R.id.roles_rightLV_title, R.id.create_rulesTV1, R.id.create_rulesTV2);
		e.addTextView(R.id.roles_hint_title);
		e.addTextView(R.id.day_currentPlayerTV, R.id.day_role_info);
		
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
		
		e.addCheckBox(R.id.create_check1, R.id.create_check2, R.id.create_check3);
		
		
		HashMap<Integer, String[]> resources = new HashMap<>();
	
		
		e.setResouces(resources);
		
		
		return e;
	}
	
	public Client startClient(String name) {
		Environment e = EnvironmentCreator();
		
		Client c = new Client(e, ws);
		ws.add(c);
		c.clickJoin();
		c.name = name;
		c.connect(host.getIP());
		
		ActivityHome ah = (ActivityHome) e.getActive();
		
		while(ah.getFragmentManager().get("namePrompt") == null)
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
		brain = new Brain(new PlayerList(), rand);
		brain.setTargetAnyone(true);
		
		interacters.remove(host);
		interacters.add(host);
		//host shouldn't readd people
		
		for(Interacter inc: interacters){
			for(Player p: inc.getPlayers()){
				brain.addSlave(p, inc.getController());
			}
		}
	}
	public Brain getBrain(){
		return brain;
	}
	public void doActions() {
		//System.out.println(host.getNarrator().getDayNumber());
		if(host.getNarrator().getDayNumber() == 3){
			//System.out.println(host.getNarrator().getLivePlayers());
		}
		//if(!host.getNarrator().isInProgress())
			//return;
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
		
		//Interacter i;
		//while((i = ws.isOnPhase(day)) != null){
			//Log.m("Brain", "waiting on " + i.getName() + " to change phase");
		//}
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
