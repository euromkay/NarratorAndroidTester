package iowrapper;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import android.day.ActivityDay;

public class WrapSynchr {

	ArrayList<Interacter> interacters = new ArrayList<>();
	public Interacter missingPlayer(String name) {
		for(Interacter i: interacters){
			if (i.getNarrator().getPlayerByName(name) == null){				
				return i;
			}
		}
		return null;
	}
	public void add(Interacter i) {
		interacters.add(i);
	}
	
	public Interacter missingRoles(int size){
		for(Interacter i: interacters){
			try{
				if (i.getNarrator().getAllRoles().size() != size){				
					return i;
				}
			}catch(ConcurrentModificationException e){
				return missingRoles(size);
			}
		}
		return null;
	}

	public Interacter notOnActivityDayYet(){
		for(Interacter i: interacters){
			try{
				ActivityDay ad = (ActivityDay) i.getEnvironment().getActive();
			}catch(ClassCastException e){
				return i;
			}
		}
		
		return null;
	}
	public Interacter isOnPhase(boolean day) {
		for(Interacter i: interacters){
			if(i.getNarrator().isDay() == day && i.getNarrator().isInProgress())
				return i;
		}
		return null;
	}
	public Interacter notEndedGame() {
		for(Interacter i: interacters){
			if(i.getNarrator().isInProgress())
				return i;
		}
		return null;
	}
}
