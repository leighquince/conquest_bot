package bot;

/**
 * This is a simple bot that does random (but correct) moves.
 * This class implements the Bot interface and overrides its Move methods.
 *  You can implements these methods yourself very easily now,
 * since you can retrieve all information about the match from variable “state”.
 * When the bot decided on the move to make, it returns an ArrayList of Moves. 
 * The bot is started by creating a Parser to which you add
 * a new instance of your bot, and then the parser is started.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import main.Region;
import main.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{
	@Override
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut)
	{
		//get southamerica 2 , safrica 4 and australia 6!
		int m = 4;
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();
		
		//get into southamerica and australia
		for (Region region : state.getPickableStartingRegions()) {
			if(region.getSuperRegion().getId() == 2 ||region.getSuperRegion().getId() == 6)
			{
				preferredStartingRegions.add(region);
				continue;
			}	
		}
		
		//try and get a neighbour
		@SuppressWarnings("unchecked")
		ArrayList<Region> chosenRegions = (ArrayList<Region>) preferredStartingRegions.clone();
		for (Region region : state.getPickableStartingRegions()) {
			if(!preferredStartingRegions.contains(region))
			{
				for (Region chosenRegion : chosenRegions) {
					if(chosenRegion.isNeighbor(region))
					{
						preferredStartingRegions.add(region);
						m++;
					}
					if(m == 6)
					{
						break;
					}
					
				}
			}
			
		} 
		
		if(m != 6)
		{
			//get into southamerica and australia
			for (Region region : state.getPickableStartingRegions()) {
				if(region.getSuperRegion().getId() == 4 )
				{
					preferredStartingRegions.add(region);
					m++;
					if(m == 6)
					{
						break;
					}
				}	
			}
		}
		
		return preferredStartingRegions;
	}

	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{
		
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armiesLeft = state.getStartingArmies();
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		LinkedList<Region> myRegions = new LinkedList<Region>();
		LinkedList<Region> enemyRegions = new LinkedList<Region>();
		LinkedList<SuperRegion> superRegionsAdded = new LinkedList<SuperRegion>();
		
		
		//get my regions
		for (Region region : visibleRegions) {
			if(region.ownedByPlayer(myName))
			{
				myRegions.add(region);
			}else{
				enemyRegions.add(region);
			}
		}
		
		int armiesPlacing = 1;
		for (Region region : myRegions) {
			if(!alreadyPlaced(placeArmiesMoves,region) && armiesLeft > 0)
			{	
				//region i own majority of where enemy has invaded
				if(region.getSuperRegion().ownedByPlayer() != myName)
				{
						
					if(region.getSuperRegion().getPercentageOwned(myName)>=0.75)
					{
						if(!this.surrondedByFriends(region, state))
						{
							armiesPlacing = armiesLeft <2 ? 1: (int)(armiesLeft*0.7);
							placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
							armiesLeft = armiesPlacing;
					
						}
					}
					//border with heavy enemy
				}
			}
		}
		
		for (Region region : myRegions) {
			if(alreadyPlaced(placeArmiesMoves,region) || !(armiesLeft >0))
			{
				break;
			}
				
			if(region.getSuperRegion().ownedByPlayer() != myName)
			{
				if(region.getSuperRegion().getPercentageOwned(myName)>=0.5 && !superRegionsAdded.contains(region.getSuperRegion()))
				{
					
					superRegionsAdded.add(region.getSuperRegion());
					armiesPlacing = armiesLeft == 1? 1: (int)(armiesLeft*0.5);
					placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
					armiesLeft = armiesPlacing;
				}
				
			}
		}
		
		
		//border with heavy enemy
		for (Region region : myRegions) {
			if(alreadyPlaced(placeArmiesMoves,region) || !(armiesLeft >0))
			{
				break;
			}
			if(region.getSuperRegion().ownedByPlayer() == myName)
			{
				for (Region neighbour : region.getNeighbors()) {
					if(armiesLeft > 0)
					{
						if((neighbour.getSuperRegion() != region.getSuperRegion()) && !neighbour.ownedByPlayer(myName))
						{
							if(neighbour.getArmies()>= region.getArmies())
							{
								armiesPlacing = armiesLeft == 1? 1: (int)(armiesLeft*0.5);
								armiesPlacing = armiesPlacing+region.getArmies()>=neighbour.getArmies()?armiesPlacing:armiesLeft;
								placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
								armiesLeft = armiesPlacing;
							}else{
								if(neighbour.getArmies()<(int)region.getArmies()*0.9)
								{
									armiesPlacing = armiesLeft == 1? 1: (int)(armiesLeft*0.2);
									placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
									armiesLeft = armiesPlacing;
								}
							}
						}
					}else{
						break;
					}
				}
			}
		}
		for (Region region : myRegions) {
			if(alreadyPlaced(placeArmiesMoves,region) || !(armiesLeft >0))
			{
				break;
			}
			
		 if(enemyIsWeak(region, state)){
				
				armiesPlacing = armiesLeft == 1? 1: (int)(armiesLeft*0.3);
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
				armiesLeft = armiesPlacing;
				
			}
			
		}
		
		if(armiesLeft >0)
		{
			Region region = myRegions.get((int)(Math.random()*myRegions.size()));
			placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesLeft));
		}
		return placeArmiesMoves;
	}

	
	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		LinkedList<Region> myRegions = new LinkedList<Region>();
		LinkedList<Region> enemyRegions = new LinkedList<Region>();
		
		
		
		//get my regions
		for (Region region : visibleRegions) {
			if(region.ownedByPlayer(myName))
			{
				myRegions.add(region);
			}else{
				enemyRegions.add(region);
			}
		}
		
		
		for (Region fromRegion : myRegions) {	
			if(surrondedByFriends(fromRegion,state))
			{
				if( mostInNeedNeighbour(fromRegion.getNeighbors(),state,fromRegion) != null)
				{	
					Region toRegion = mostInNeedNeighbour(fromRegion.getNeighbors(),state,fromRegion);
					if(fromRegion.getArmies() >2)
					{
						int armiesToMove = fromRegion.getArmies()-2;
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armiesToMove));
					}
				}
			}
				
		}
		
		//don't attack from the paces i have moved from
		for (AttackTransferMove movesSoFar : attackTransferMoves) {		
			myRegions.remove(movesSoFar.getFromRegion());
		}
		
		for (Region fromRegion : myRegions) {
			if(fromRegion.getSuperRegion().getPercentageOwned(myName)>=0.5)
			{
				if(!surrondedByFriends(fromRegion,state))
				{
					for (Region toRegion : fromRegion.getNeighbors()) {
						if(!toRegion.ownedByPlayer(state.getMyPlayerName()))
						{
							if(toRegion.getArmies() < fromRegion.getArmies()*0.6)
							{
								
								int armiesToMove = (int) (toRegion.getArmies()+(toRegion.getArmies()*0.6));
								if(armiesToMove < 4)
								{
									armiesToMove = 4;
								}
								if(armiesToMove >= fromRegion.getArmies())
								{
									continue;
								}
																
								attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armiesToMove));
								if((fromRegion.getArmies()-armiesToMove > 5))
								{
									continue;
								}else{
									break;
								}
							}
						}
					}	
				}
			}		
		}
		
		//don't attack from the paces i have moved from
		for (AttackTransferMove movesSoFar : attackTransferMoves) {		
			myRegions.remove(movesSoFar.getFromRegion());
		}
		
		//attack weak regions
		for (Region fromRegion1 : myRegions) {
			for (Region toRegion : fromRegion1.getNeighbors()) {
				if(!toRegion.ownedByPlayer(state.getMyPlayerName()))
				{
					if(toRegion.getArmies()-(fromRegion1.getArmies()*0.9)<0)
					{
						if(fromRegion1.getArmies() >2)
						{
							int armiesToMove = fromRegion1.getArmies()-2;
							//System.err.println(String.format("Attacking Region %s from %s with %s %n",toRegion.getId(),fromRegion.getId(),armiesToMove));
							attackTransferMoves.add(new AttackTransferMove(myName, fromRegion1, toRegion, armiesToMove));
							break;
						}
					}
				}
				
			}
			
		}		
		return attackTransferMoves;
	}
	
	private boolean alreadyPlaced(ArrayList<PlaceArmiesMove> placeArmiesMoves,
			Region region) {
		for ( PlaceArmiesMove placeArmiesMove : placeArmiesMoves) {
			
			if(placeArmiesMove.getRegion().equals(region))
			{
				return true;
			}
		}
		return false;
	}

	private boolean enemyIsWeak(Region region, BotState state) {
	
		
		for (Region neighbour : region.getNeighbors()) {
		
					if(!neighbour.ownedByPlayer(state.getMyPlayerName()))
					{
						if(neighbour.getArmies()<(int)region.getArmies()*0.8)
						{
								return true;
							
						}
					}
			
		}
		
		return false;
	}
	
	private boolean surrondedByFriendsINSuperRegion(Region region, BotState state)
	{
		for (Region neighbour : region.getNeighbors()) {
			
			if(!neighbour.ownedByPlayer(state.getMyPlayerName()) && neighbour.getSuperRegion().getId() == region.getSuperRegion().getId())
			{
				return false;
			}
	
		}

		return true;
		
	}
	
	private boolean surrondedByFriends(Region region, BotState state)
	{
		for (Region neighbour : region.getNeighbors()) {		
			if(!neighbour.ownedByPlayer(state.getMyPlayerName()))
			{
				return false;
			}
		}
		return true;
	}
	
	
	private Region mostInNeedNeighbour(LinkedList<Region> neighbours, BotState state, Region region)
	{
		Map<Region,Integer> mostIneed = new HashMap<Region,Integer>();
		for (Region neighboursNeighbours : neighbours) {
			if(neighboursNeighbours.ownedByPlayer(state.getMyPlayerName()))
			{
				for (Region  neighbour: neighboursNeighbours.getNeighbors()) {
					if(!neighbour.ownedByPlayer(state.getMyPlayerName()))
					{
						mostIneed.put(neighboursNeighbours, new Integer((int) (neighbour.getArmies()-(neighboursNeighbours.getArmies()*0.9))));
					}
				}	
			}
		}
		
		Integer rank = null;
		Region regionRank = null;
		
		for (Region region2 : mostIneed.keySet()) {
			if(rank == null)
			{
				regionRank = region2;
				rank = mostIneed.get(region2);
			}else{
				if(rank < mostIneed.get(region2))
				{
					regionRank = region2;
					rank = mostIneed.get(region2);
				}
			}
		}
			
		return rank == null || rank <= 0?null:regionRank;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
