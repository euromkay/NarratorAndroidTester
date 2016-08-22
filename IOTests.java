package iowrapper;

import java.util.Random;

import android.CommunicatorPhone;
import android.GUIController;
import android.app.Environment;
import android.day.ActivityDay;
import android.setup.ActivityCreateGame;
import android.util.Log;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import shared.ai.Controller;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.Team;
import shared.logic.exceptions.PlayerTargetingException;
import shared.logic.templates.BasicRoles;
import shared.roles.Blocker;
import shared.roles.Bodyguard;
import shared.roles.Driver;
import shared.roles.Mayor;
import shared.roles.RandomRole;

public class IOTests extends TestCase{
	private Interacter i;
	
	
	
	public IOTests(String name) {
		super(name);
	}
	
	
	
	public void testSingleConnection(){
		IOWrapper wrap = new IOWrapper();
		long seed = new Random().nextLong();
		//long seed = Long.parseLong("-616941299922450565");
		System.out.println(seed);
		
		Host h = wrap.startHost(seed); 
		h.newPlayer("HostSub");
		Narrator n1 = h.getNarrator();
		assertTrue(n1.getAllPlayers().get(0).getCommunicator().getClass() == CommunicatorPhone.class);
		
		Client c1 = wrap.startClient("Inet1");
		
		h.addRole(BasicRoles.Chauffeur(), Mafia);
		
		assertEquals(c1.getNarrator().getAllRoles().size(), 1);
		
		h.addRole(BasicRoles.Bodyguard(), Town);
		assertEquals(c1.getNarrator().getAllRoles().size(), 2);
		
		h.addRole(BasicRoles.Mayor(), Town);

		assertTrue(n1.getAllPlayers().get(0).getCommunicator().getClass() == CommunicatorPhone.class);
		h.clickStart(seed);


		assertTrue(n1.getAllPlayers().get(0).getCommunicator().getClass() == CommunicatorPhone.class);
		

		wrap.startBrain();
		assertTrue(n1.getAllPlayers().get(0).getCommunicator().getClass() == CommunicatorPhone.class);
		assertEquals(3, wrap.getBrain().slaves.size());
		
		assertFalse(c1.getNarrator() == h.getNarrator());
		assertTrue(c1.getNarrator().isStarted());
		
		
		assertTrue(n1 == h.getNarrator());
		
		assertEquals(3, h.getPlayers().size());
		assertEquals(1, c1.getPlayers().size());
		assertEquals(h.getNarrator().isDay(), c1.getNarrator().isDay());
		
		System.out.println();
		
		
		Player bg = h.getNarrator().getPlayerByName("Master");
		Player mayor = h.getNarrator().getPlayerByName("HostSub");
		Player chauf = c1.getNarrator().getPlayerByName("Inet1");
		
		assertFalse(bg.getNarrator().getAllPlayers().has(chauf));
		
		Controller h_control = h.getController();
		Controller c_control = c1.getController();
		
		assertTrue(bg.is(Bodyguard.class));
		assertTrue(mayor.is(Mayor.class));
		assertTrue(chauf.is(Driver.class));
		
		assertTrue(h.getNarrator().isNight());

		c_control.endNight(chauf);
		
		h_control.endNight(bg);
		h_control.endNight(mayor);
		
		assertTrue(mayor.hasDayAction());
		Player mayorC = Controller.Translate(c1.getNarrator(), mayor);
		assertTrue(mayorC.hasDayAction());
		
		h_control.doDayAction(mayor);
		
		String TAG = "testingSingleConnection";
		
		while(mayorC.hasDayAction())
			Log.m(TAG, "waiting for c1 for mayor reveal");
		
		c_control.vote(chauf, mayor);
		
		while(mayor.getVoteCount() == 0)
			Log.m(TAG, "waiting for host to get vote from c_control");
		
		while(mayorC.getVoteCount() == 0)
			Log.m(TAG, "waiting for c1 to get vote confirm from host");
		
		
		h_control.skipVote(mayor);
		
		while((i = wrap.ws.isOnPhase(Narrator.DAY_START)) != null){
			Log.m(TAG, "waiting for " + i .getName() + " to change to night");
		}
		
		c_control.setNightTarget(chauf, mayor, Driver.TARGET1);
		c_control.setNightTarget(chauf, bg, Driver.TARGET2);
		c_control.endNight(chauf);
		
		h_control.endNight(bg);
		h_control.endNight(mayor);
		
		while((i = wrap.ws.isOnPhase(Narrator.NIGHT_START)) != null){
			Log.m(TAG, "waiting for " + i .getName() + " to change to day");
		}
		
		h_control.skipVote(mayor);
		
		while((i = wrap.ws.isOnPhase(Narrator.DAY_START)) != null){
			Log.m(TAG, "waiting for " + i .getName() + " to change to night");
		}
		
		c_control.setNightTarget(chauf, mayor, Team.KILL);
		h_control.setNightTarget(bg, mayor, Bodyguard.GUARD);
		

		c_control.endNight(chauf);
		
		h_control.endNight(bg);
		h_control.endNight(mayor);
		
		while((i = wrap.ws.notEndedGame()) != null){
			Log.m(TAG, "waiting for " + i .getName() + " to change to end game");
		}
		
		assertFalse(h.getNarrator().isInProgress());
		assertFalse(c1.getNarrator().isInProgress());
		
		wrap.close();
	}
	
	public void testSingleConnectionClientSub(){
		IOWrapper wrap = new IOWrapper();
		long seed = new Random().nextLong();
		//long seed = Long.parseLong("-616941299922450565");
		//System.out.println(seed);
		
		Host h = wrap.startHost(seed); 
		
		Client c1 = wrap.startClient("Inet1");
		c1.newPlayer("Inet2");
		
		h.addRole(BasicRoles.Chauffeur(), Mafia);
		assertEquals(c1.getNarrator().getAllRoles().size(), 1);
		
		h.addRole(BasicRoles.Bodyguard(), Town);
		assertEquals(c1.getNarrator().getAllRoles().size(), 2);
		h.addRole(BasicRoles.Mayor(), Town);
		h.dayStart();
		
		h.clickStart(seed);

		

		wrap.startBrain();
		assertEquals(3, wrap.getBrain().slaves.size());
		
		assertFalse(c1.getNarrator() == h.getNarrator());
		assertTrue(c1.getNarrator().isStarted());
		
		assertEquals(3, h.getPlayers().size());
		assertEquals(2, c1.getPlayers().size());
		assertEquals(h.getNarrator().isDay(), c1.getNarrator().isDay());
		
		System.out.println();
		
		
		Player bg = h.getNarrator().getPlayerByName("Master");
		Player mayor = c1.getNarrator().getPlayerByName("Inet1");
		Player chauf = c1.getNarrator().getPlayerByName("Inet2");
		
		assertFalse(bg.getNarrator().getAllPlayers().has(chauf));
		
		Controller h_control = h.getController();
		Controller c_control = c1.getController();
		
		assertTrue(bg.is(Bodyguard.class));
		assertTrue(mayor.is(Mayor.class));
		assertTrue(chauf.is(Driver.class));
		
		assertTrue(h.getNarrator().isDay());
		
		assertTrue(mayor.hasDayAction());
		Player mayorH = Controller.Translate(h.getNarrator(), mayor);
		assertTrue(mayorH.hasDayAction());
		
		c_control.doDayAction(mayor);
		
		String TAG = "testingSingleConnection2";
		
		while(mayorH.hasDayAction())
			Log.m(TAG, "waiting for host for mayor reveal");
		
		c_control.vote(chauf, mayor);
		
		while(mayor.getVoteCount() == 0)
			Log.m(TAG, "waiting for host to get vote from c_control");
		
		while(mayorH.getVoteCount() == 0)
			Log.m(TAG, "waiting for c1 to get vote confirm from host");
		
		ActivityDay ad = (ActivityDay) c1.getEnvironment().getActive();
		
		while(!ad.actionList.contains(mayor.getSkipper())){
			Log.m(TAG, "waiting for c1mayor to get voteSkip on radar");
		}
		
		c_control.skipVote(mayor);
		
		while((i = wrap.ws.isOnPhase(Narrator.DAY_START)) != null){
			Log.m(TAG, "waiting for " + i .getName() + " to change to night");
		}
		
		c_control.setNightTarget(chauf, mayor, Driver.TARGET1);
		c_control.setNightTarget(chauf, bg, Driver.TARGET2);
		c_control.endNight(chauf);
		
		h_control.endNight(bg);
		c_control.endNight(mayor);
		
		while((i = wrap.ws.isOnPhase(Narrator.NIGHT_START)) != null){
			Log.m(TAG, "waiting for " + i .getName() + " to change to day");
		}
		
		c_control.skipVote(mayor);
		
		while((i = wrap.ws.isOnPhase(Narrator.DAY_START)) != null){
			Log.m(TAG, "waiting for " + i .getName() + " to change to night");
		}
		
		c_control.setNightTarget(chauf, mayor, Team.KILL);
		h_control.setNightTarget(bg, mayor, Bodyguard.GUARD);
		

		c_control.endNight(chauf);
		
		h_control.endNight(bg);
		c_control.endNight(mayor);
		
		while((i = wrap.ws.notEndedGame()) != null){
			Log.m(TAG, "waiting for " + i .getName() + " to change to day");
		}
		
		assertFalse(h.getNarrator().isInProgress());
		assertFalse(c1.getNarrator().isInProgress());
		
		wrap.close();
	}
	
	private void sleep(int seconds){
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void tesstZMassive(){
		IOWrapper wrap = new IOWrapper();

		long seed = Long.parseLong("-616941299922450565");
	
		Host h = wrap.startHost(seed); 

		System.out.println("host connected");
		
		String ip = h.getIP();
		Client c1 = wrap.startClient("Inet1");
		c1.connect(ip);
		c1.newPlayer("Inet1Sub");
		c1.newPlayer("Inet1Sub2");
		
		checkEquality(h.getNarrator(), c1.getNarrator());
		
		Client c2 = wrap.startClient("Inet2");
		c2.connect(ip);
		
		h.addRole(BasicRoles.MassMurderer(), "Neutrals");
		h.addRandomRole();
		h.addRandomRole();
		h.addRandomRole();
		h.addRandomRole();
		h.addRandomRole();
		h.addRandomRole();
		h.addRandomRole();
		
		Client c3 = wrap.startClient("Inet3");
		c3.connect(ip);
		
		h.newPlayer("Host2");
		h.newPlayer("HostSub1");
		
		Narrator n_host = h.getNarrator();
		Narrator n_client1 = c1.getNarrator();
		Narrator n_client2 = c2.getNarrator();
		Narrator n_client3 = c3.getNarrator();
		
		assertEqual(n_host.getAllPlayers(), n_client3.getAllPlayers());
		assertEquals(n_host.getPlayerCount(), n_client2.getPlayerCount());
		
		checkEquality(n_host, n_client3, n_client1, n_client2 );
		
		wrap.start();

		n_host    = h.getNarrator();
		n_client1 = c1.getNarrator();
		n_client2 = c2.getNarrator();
		n_client3 = c3.getNarrator();
		checkEquality(n_host, n_client1, n_client2, n_client3);
		
		GUIController gui_host = h.getController();
		PlayerList hostPlayers = h.getPlayers();
		assertEquals(3, hostPlayers.size());
		
		GUIController gui_client = c1.getController();
		PlayerList clientPlayers = c1.getPlayers();
		assertEquals(3, clientPlayers.size());

		wrap.startBrain();
		
		assertEquals(wrap.getBrain().size(), n_host.getPlayerCount());
		
		wrap.doActions();
		int i = 0;
		while(n_client1.isInProgress() && i < 1){
			
			wrap.doActions();
			i++;
		}

		//qWait();
		checkEquality(n_host, n_client1, n_client2, n_client3);
		
		c1.close();
		c2.close();
		c3.close();
		//h.close();
		
		
		//System.out.println(n_host.getEvents(Event.PRIVATE, false));
	}
	protected void assertEqual(PlayerList plist1, PlayerList plist2) {
		if(plist1.size() != plist2.size()){
			System.out.println(plist1.toString());
			System.out.println(plist2.toString());
			throw new AssertionFailedError();
		}
	}

	private void checkEquality(Narrator host, Narrator... clients){
		for(Narrator c: clients){
			assertEquals(host, c);
		}
	}
	
	
	

	
	public void testTexting(){
		IOWrapper wrap = new IOWrapper();
		long seed = new Random().nextLong();
		//System.out.println(seed);
		
		
		Host h = wrap.startHost(seed);

		TextClient t  = wrap.startTexter("text", "5", h.getEnvironment());
		TextClient t2 = wrap.startTexter("text2", "6", h.getEnvironment());
		
		h.addRole(BasicRoles.Escort(), Town);
		h.addRole(BasicRoles.Consort(), Mafia);
		h.addRole(BasicRoles.SerialKiller(), "Neutrals");
		
		h.clickStart(seed);
		
		Player esc = h.getNarrator().getPlayerByName("Master");
		Player con = t.getNarrator().getPlayerByName("text");
		Player sk = t2.getNarrator().getPlayerByName("text2");
		
		
		Controller hCon = h.getController();
		Controller tCon = t.getController();
		Controller t2Con = t2.getController();
		
		t2Con.skipVote(sk);
		tCon.skipVote(con);
		
		assertTrue(h.getNarrator().isNight());
		hCon.endNight(esc);
		t2Con.endNight(sk);
		tCon.endNight(con);
		
		
		t2Con.vote(sk, esc);
		hCon.vote(esc, con);
		tCon.vote(con, esc);
		
		tCon.setNightTarget(con, sk, Blocker.BLOCK);
		t2Con.endNight(sk);
		tCon.endNight(con);
		
		assertFalse(h.getNarrator().isInProgress());
		
		//wrap.close();
	}

	public static final String Town = "Town", Mafia = "Mafia", Yakuza = "Yakuza", Randoms = "Randoms", Neutrals = "Neutrals";			
		
	public void testDoubleClient(){
		IOWrapper wrap = new IOWrapper();
		long seed = new Random().nextLong();
		//System.out.println(seed);
		
		Host h = wrap.startHost(seed);
		
		Client c1 = wrap.startClient("C1");
		Client c2 = wrap.startClient("C2");
		
		
		
		h.addRole(BasicRoles.Detective(), Town);
		h.addRole(RandomRole.MafiaRandom(), "Randoms");
		

		Client c3 = wrap.startClient("C3");
		
		h.addRole(RandomRole.YakuzaRandom(), Randoms);
		h.addRole(BasicRoles.Jester(), "Neutrals");
		
		h.dayStart();
		
		h.clickStart(seed);
		
		
		
		assertTrue(c2.getPlayers().size() == c3.getPlayers().size());
		
		Player detec = h.getNarrator().getPlayerByName("Master");
		Player maf = c1.getNarrator().getPlayerByName("C1");
		Player yak  = c2.getNarrator().getPlayerByName("C2");
		Player jester = c3.getNarrator().getPlayerByName("C3");
		
		//Controller conh = h.getController();
		Controller con1 = c1.getController();
		GUIController con2 = c2.getController();
		Controller con3 = c3.getController();
		
		assertTrue(c3.getNarrator().isDay());
		
		con3.vote(jester, detec);
		con2.vote(yak, detec);
		con1.vote(maf, detec);
		
		
		while((i = wrap.ws.isOnPhase(Narrator.DAY_START)) != null){
			Log.m("MultiClient", "waiting for " + i.getName() + " to change to night");
		}
		assertTrue(detec.isDead());
		
		assertTrue(h.getNarrator().isNight());
		assertTrue(c1.getNarrator().isNight());
		assertTrue(c2.getNarrator().isNight());
		assertTrue(c3.getNarrator().isNight());

		con1.setNightTarget(maf, maf, Team.SEND);
		con3.endNight(jester);
		con1.cancelNightTarget(maf, maf, Team.SEND);
		con1.setNightTarget(maf, maf, Team.SEND);
		con2.endNight(yak);
		con1.setNightTarget(maf, maf, Team.SEND);
		con1.endNight(maf);
		
		while((i = wrap.ws.isOnPhase(Narrator.NIGHT_START)) != null){
			Log.m("MultiClient", "waiting for " + i.getName() + " to change to day");
		}
		
		assertEquals(4, wrap.getNarrators().size());
		for(Narrator na: wrap.getNarrators()){
			assertTrue(na.isDay());
			assertTrue(na.getMinLynchVote() == 2);
		}
		
		con1.vote(maf, jester);
		con3.vote(jester, maf);
		sleep(1);//have to wait for the vote to register
		con3.unvote(jester);
		con2.vote(yak, jester);
		
		while((i = wrap.ws.isOnPhase(Narrator.DAY_START)) != null){
			Log.m("MultiClient", "waiting for " + i.getName() + " to change to day");
		}

		assertFalse(c1.getNarrator().getAllPlayers().has(yak));
		try{
			con1.setNightTarget(yak, maf, Team.KILL);
			fail();
		}catch(PlayerTargetingException e){}
		
		con1.setNightTarget(maf, yak, Team.KILL);
		
		Player maf2 = Controller.Translate(c2.getNarrator(), maf);
		while(!(con2.dScreen.actionList.contains(maf2))){
			Log.m("MultiClient", "waiting for Client2 to update action panel");
		}
		
		con2.setNightTarget(yak, maf, Team.KILL);
		con1.endNight(maf);
		con2.endNight(yak);
		
		while((i = wrap.ws.notEndedGame()) != null){
			Log.m("Multi", "waiting for " + i .getName() + " to change to end game");
		}
		
		wrap.close();
	}
	
	private ActivityCreateGame getACG(Interacter i){
		Environment e = i.getEnvironment();
		return (ActivityCreateGame) e.getActive();
	}
	
	public void testBrainTexters(){
		IOWrapper wrap = new IOWrapper();
		
		long seed = new Random().nextLong();
		System.out.println(seed);
		
		Host h = wrap.startHost(seed);
		
		for(int i = 0; i < 20; i++){
			wrap.startTexter("Text"+i, i+"", h.getEnvironment());
			if(i != 0)
				h.addRandomRole();
		}
		
		ActivityCreateGame ac = getACG(h);
		
		assertEquals(19, ac.ns.local.getAllRoles().size());
		h.addRole(BasicRoles.Veteran(), Town);
		assertEquals(20, ac.ns.local.getAllRoles().size());
		h.addRole(RandomRole.YakuzaRandom(), Randoms);
		assertEquals(21, ac.ns.local.getAllRoles().size());
		
		h.clickStart(seed);
		
		
		wrap.startBrain();
		while(h.getNarrator().isInProgress()){
			wrap.doActions();
		}
		
	}
	
	public void testSingleClientBrain(){
		IOWrapper wrap = new IOWrapper();
		
		long seed = new Random().nextLong();
		//long seed = Long.parseLong("1446272076855549240");
		System.out.println(seed);
		
		Host h = wrap.startHost(seed);
		Client c = wrap.startClient("C1");
		c.newPlayer("C2");
		c.newPlayer("C3");
		
		h.addRandomRole();
		h.addRandomRole();
		h.addRole(BasicRoles.SerialKiller(), Neutrals);
		h.addRole(BasicRoles.Lookout(), Town);
		
		h.clickStart(seed);
		
		wrap.startBrain();
		while(h.getNarrator().isInProgress()){
			wrap.doActions();
		}
		
		
		wrap.close();
	}
	
	public void testSingleClientWithHostSubBrain(){
		IOWrapper wrap = new IOWrapper();
		
		long seed = new Random().nextLong();
		//long seed = Long.parseLong("2525295728080531086");
		System.out.println(seed);
		
		Host h = wrap.startHost(seed);
		h.newPlayer("H1");
		Client c = wrap.startClient("C1");
		c.newPlayer("C2");
		
		wrap.startTexter("T1", "423", h.getEnvironment());
		wrap.startTexter("Ta1", "422343", h.getEnvironment());

		h.addRandomRole();
		h.addRandomRole();
		h.addRandomRole();
		h.addRandomRole();
		
		c.newPlayer("C3");
		
		Client c2 = wrap.startClient("C1A");
		c2.newPlayer("C1B");
		
		h.addRandomRole();
		h.addRandomRole();
		wrap.startTexter("Tb1", "42223", h.getEnvironment());
		h.addRandomRole();
		h.addRole(BasicRoles.Jester(), Neutrals);
		h.addRole(BasicRoles.SerialKiller(), Neutrals);
		h.addRole(BasicRoles.Lookout(), Town);
		
		h.clickStart(seed);
		
		wrap.startBrain();
		while(h.getNarrator().isInProgress()){
			wrap.doActions();
		}
		
		
		wrap.close();
	}
	
	/*public void testExecPackaging(){
		newNarrator();

		for(int i = 0; i < 25; i++)
			addPlayer(BasicRoles.Citizen());
		
		Player exec = addPlayer(BasicRoles.Executioner());
		addPlayer(BasicRoles.SerialKiller());
		

		r.DAY_START = Narrator.NIGHT_START;
		r.exeuctionerImmune = false;
		r.exeuctionerWinImmune = true;
		
		n.startGame();
		
		Narrator copy = packageTest();
		
		
		String execID = exec.getName();
		
		String targetName = ((Executioner)exec.getRole()).getTarget(exec).getName();
		String copyName   = ((Executioner)copy.getPlayerByName(execID).getRole()).getTarget(exec).getName();
		
		assertEquals(targetName, copyName);
	}*/
	
	
}