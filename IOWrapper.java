package iowrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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
import shared.logic.support.Constants;
import shared.roles.Agent;
import shared.roles.Arsonist;
import shared.roles.Blackmailer;
import shared.roles.Bodyguard;
import shared.roles.BusDriver;
import shared.roles.Chauffeur;
import shared.roles.Citizen;
import shared.roles.Consort;
import shared.roles.CultLeader;
import shared.roles.Cultist;
import shared.roles.Detective;
import shared.roles.Doctor;
import shared.roles.Escort;
import shared.roles.Executioner;
import shared.roles.Framer;
import shared.roles.Godfather;
import shared.roles.Janitor;
import shared.roles.Jester;
import shared.roles.Lookout;
import shared.roles.Mafioso;
import shared.roles.MassMurderer;
import shared.roles.Mayor;
import shared.roles.SerialKiller;
import shared.roles.Sheriff;
import shared.roles.Veteran;
import shared.roles.Vigilante;
import shared.roles.Witch;
import voss.narrator.R;

public class IOWrapper {

	public WrapSynchr ws;
	private IOTests ios;
	public IOWrapper(IOTests s){
		ws = new WrapSynchr();
		ios = s;
	}
	
	private Brain brain;
	private Host host;
	private Random rand;
	private long seed;
	public Host startHost(long seed){
		Environment e = EnvironmentCreator();
		this.seed = seed;
		rand = new Random();
		rand.setSeed(seed);
		host = new Host(e, rand, ws);
		ws.add(host);
		interacters.add(host);
		host.clickHost();
		host.clickNameHost();
		
		while(host.notBroadcasting()){
			Log.m("Host", "waiting to connect");
		}
		
		ios.n = host.getNarrator();
		
		return host;
	}
	
	public void setSeed(long l){
		rand.setSeed(l);
	}

	private Environment EnvironmentCreator(){
		Environment e = new Environment();
		
		HashMap<Integer, String[]> resources = new HashMap<>();
		resources.put(R.array.roles_townRoles, new String[]{Citizen.ROLE_NAME, Sheriff.ROLE_NAME, Detective.ROLE_NAME, Lookout.ROLE_NAME, Doctor.ROLE_NAME, Escort.ROLE_NAME, BusDriver.ROLE_NAME, Bodyguard.ROLE_NAME, Vigilante.ROLE_NAME, Veteran.ROLE_NAME, Mayor.ROLE_NAME});
		resources.put(R.array.roles_mafiaRoles, new String[]{Godfather.ROLE_NAME, Mafioso.ROLE_NAME, Agent.ROLE_NAME, Blackmailer.ROLE_NAME, Consort.ROLE_NAME, Janitor.ROLE_NAME, Framer.ROLE_NAME, Chauffeur.ROLE_NAME});
		resources.put(R.array.roles_neutralRoles, new String[]{CultLeader.ROLE_NAME, Cultist.ROLE_NAME, Witch.ROLE_NAME, Arsonist.ROLE_NAME, SerialKiller.ROLE_NAME, MassMurderer.ROLE_NAME, Jester.ROLE_NAME, Executioner.ROLE_NAME});
		resources.put(R.array.roles_randomRoles, new String[]{Constants.ANY_RANDOM_ROLE_NAME, Constants.TOWN_RANDOM_ROLE_NAME, Constants.TOWN_PROTECTIVE_ROLE_NAME, Constants.TOWN_INVESTIGATIVE_ROLE_NAME, Constants.TOWN_KILLING_ROLE_NAME, Constants.MAFIA_RANDOM_ROLE_NAME, Constants.YAKUZA_RANDOM_ROLE_NAME, Constants.NEUTRAL_RANDOM_ROLE_NAME}); 
	
		
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
			Thread.sleep(3000);
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
