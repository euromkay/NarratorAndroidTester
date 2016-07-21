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
		Player hammer = null;
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
