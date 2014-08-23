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
import main.RegionNode;
import main.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{
	
	ArrayList<AttackTransferMove> attackTransferMoves;
	LinkedList<Region> visibleRegions;
	LinkedList<Region> myRegions;
	LinkedList<Region> enemyRegions;
	
	
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
			boolean bothered = false;
			for (Region neighbour : region.getNeighbors()) {
				if(neighbour.getPlayerName().equals(state.getMyPlayerName()) && neighbour.getSuperRegion().getId() != region.getSuperRegion().getId() && neighbour.getArmies() <3 )
				{
					bothered = true;
				}
				
			}
			if(!this.shouldAttack(region, state) && bothered)
			{
				armiesPlacing = armiesLeft-1;
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
				armiesLeft = armiesLeft-armiesPlacing;
			}
		}
		for (Region region : myRegions) {
			if(!alreadyPlaced(placeArmiesMoves,region) && armiesLeft > 0)
			{	
				
				if(region.getSuperRegion().ownedByPlayer() == myName)
				{
					if(!this.surrondedByFriends(region, state))
					{	
						for (Region neighbour : region.getNeighbors()) {
								
							if(neighbour.getArmies() > region.getArmies()*0.9 && neighbour.getPlayerName().equals(state.getOpponentPlayerName()))
							{	
								armiesPlacing = armiesLeft;
								if(armiesPlacing<2)
								{
									armiesPlacing = armiesLeft;
								}
								placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
								armiesLeft = armiesLeft-armiesPlacing;
							
							}					
						}
					}
				}
			}
		}
		
		for (Region region : myRegions) {
			if(!alreadyPlaced(placeArmiesMoves,region) && armiesLeft > 0)
			{	
				
				if(region.getSuperRegion().ownedByPlayer() == myName)
				{
					if(!this.surrondedByFriends(region, state))
					{	
						for (Region neighbour : region.getNeighbors()) {
								
							if(neighbour.getArmies() > region.getArmies()*0.9 && !neighbour.getPlayerName().equals(state.getMyPlayerName()))
							{	
								armiesPlacing = (int) (armiesLeft*0.8);
								if(armiesPlacing<2)
								{
									armiesPlacing = armiesLeft;
								}
								placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
								armiesLeft = armiesLeft-armiesPlacing;
							
							}					
						}
					}
				}
			}
		}
		
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
							armiesLeft = armiesLeft-armiesPlacing;
					
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
				if(region.getSuperRegion().getPercentageOwned(myName)>=0.5)
				{
					
					armiesPlacing = armiesLeft == 1? 1: (int)(armiesLeft*0.5);
					placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
					armiesLeft = armiesLeft-armiesPlacing;
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
						if((neighbour.getSuperRegion() != region.getSuperRegion()) && neighbour.ownedByPlayer(state.getOpponentPlayerName()))
						{
							if(neighbour.getArmies()>= region.getArmies())
							{
								armiesPlacing = armiesLeft == 1? 1: armiesLeft;
								armiesPlacing = armiesPlacing+region.getArmies()>=neighbour.getArmies()?armiesPlacing:armiesLeft;
								placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
								armiesLeft = armiesPlacing;
							}else{
								if(neighbour.getArmies()<(int)region.getArmies()*0.9)
								{
									armiesPlacing = armiesLeft == 1? 1: (int)(armiesLeft*0.2);
									placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesPlacing));
									armiesLeft = armiesLeft-armiesPlacing;
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
				armiesLeft = armiesLeft-armiesPlacing;
				
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
		
		attackTransferMoves = new ArrayList<AttackTransferMove>();		
		visibleRegions = state.getVisibleMap().getRegions();
		myRegions = new LinkedList<Region>();
		enemyRegions = new LinkedList<Region>();
		
		getMineAndEnemyRegions(state);

		
		
		//support friends
		supportFriends(state);
		
		//don't attack from the paces i have moved from
		for (AttackTransferMove movesSoFar : attackTransferMoves) {		
			myRegions.remove(movesSoFar.getFromRegion());
		}
		
		attackInSuperRegionIOwnAtLestHalfOf(state);
		
		//don't attack from the paces i have moved from
		for (AttackTransferMove movesSoFar : attackTransferMoves) {		
			myRegions.remove(movesSoFar.getFromRegion());
		}
		
		attackWeakEnemies(state);	
		
		bully(state);
		return attackTransferMoves;
	}


	private void bully(BotState state) {
		List<Region> attackFrom = new ArrayList<Region>();
		for (Region enemyRegion : enemyRegions) {
			int myForces = 0;
			int hisForces = enemyRegion.getArmies();
			for (Region enemyNeigbour : enemyRegion.getNeighbors()) {
				
				if(enemyNeigbour.getPlayerName().equals(state.getMyPlayerName()) && this.getNumberOfSurroundingEnemyRegions(enemyNeigbour, state)==1)
				{
					attackFrom.add(enemyNeigbour);
					myForces += (int)(enemyNeigbour.getArmies()*0.5);
				}
				
			}
			
			if(myForces>hisForces)
			{
				for (Region region : attackFrom) {
					attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), region, enemyRegion, (int) (region.getArmies()*0.5)));
					region.setArmies((int)(region.getArmies()*0.5));
				}
			}
			
		}
		
	}

	private void attackWeakEnemies(BotState state) {
		for (Region fromRegion : myRegions) {
			for (Region toRegion : fromRegion.getNeighbors()) {
				if(toRegion.getArmies() < fromRegion.getArmies()*0.7 && !toRegion.ownedByPlayer(state.getMyPlayerName()) &&shouldAttack(fromRegion,state))
				{
					
					int armiesToMove = getNumberOfSurroundingEnemyRegions(fromRegion, state)==1?fromRegion.getArmies()-1:(int) (toRegion.getArmies()+(toRegion.getArmies()*0.7));
					if(armiesToMove < 4)
					{
						armiesToMove = 4;
					}
					if(armiesToMove >= fromRegion.getArmies())
					{
						continue;
					}
					
					if((fromRegion.getArmies()-armiesToMove < 5) && getNumberOfSurroundingEnemyRegions(fromRegion, state)>1)
						continue;
													
					attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), fromRegion, toRegion, armiesToMove));
					
					
					if((fromRegion.getArmies()-armiesToMove > 5))
					{
						fromRegion.setArmies(fromRegion.getArmies()-armiesToMove);
						continue;
					}else{
						break;
					}
				}
				
			}
			
		}
	}

	private void attackInSuperRegionIOwnAtLestHalfOf(BotState state) {
		for (Region fromRegion : myRegions) {
			if(fromRegion.getSuperRegion().getPercentageOwned(state.getMyPlayerName())>=0.5)
			{
				if(!surrondedByFriends(fromRegion,state))
				{
					for (Region toRegion : fromRegion.getNeighbors()) {
						if(!toRegion.ownedByPlayer(state.getMyPlayerName()) && toRegion.getSuperRegion().equals(fromRegion.getSuperRegion()) && shouldAttack(fromRegion,state))
						{
							if(toRegion.getArmies() < fromRegion.getArmies()*0.7)
							{
								
								int armiesToMove = getNumberOfSurroundingEnemyRegions(fromRegion,state)==1?fromRegion.getArmies()-1:(int) (toRegion.getArmies()+(toRegion.getArmies()*0.7));
								if(armiesToMove < 4)
								{
									armiesToMove = 4;
								}
								if(armiesToMove >= fromRegion.getArmies())
								{
									continue;
								}
																
								attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), fromRegion, toRegion, armiesToMove));
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
	}
	
	private boolean shouldAttack(Region region, BotState state)
	{
		int myArmies = region.getArmies();
		int enemyAround = 0;
		for (Region toRegion : region.getNeighbors()) {
			if(toRegion.ownedByPlayer(state.getOpponentPlayerName()))
			{
				enemyAround+=toRegion.getArmies();
			}
		}
		return  myArmies>enemyAround;
	}
	
	private int getNumberOfSurroundingEnemyRegions(Region region, BotState state){
		
		int myArmies = region.getArmies();
		int numberOfEnemy =0;
		for (Region toRegion : region.getNeighbors()) {
			if(!toRegion.ownedByPlayer(state.getMyPlayerName()))
			{			
				numberOfEnemy++;
			}	
		}
		return numberOfEnemy;
	}

	private void supportFriends(BotState state) {
		for (Region fromRegion : myRegions) {	
			if(surrondedByFriends(fromRegion,state))
			{
				if( mostInNeedNeighbour(fromRegion.getNeighbors(),state,fromRegion) != null)
				{	
					Region toRegion = mostInNeedNeighbour(fromRegion.getNeighbors(),state,fromRegion);
					if(fromRegion.getArmies() >1)
					{
						int armiesToMove = fromRegion.getArmies()-1;
						attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), fromRegion, toRegion, armiesToMove));
						toRegion.setArmies(toRegion.getArmies()+armiesToMove);
					}
				}else if(fromRegion.getArmies()>2){
					List<RegionNode> open = new ArrayList<RegionNode>(); 
					List<RegionNode> closed = new ArrayList<RegionNode>(); 
					
					open.add(new RegionNode(null,fromRegion,0));
					RegionNode regionNode = closestFriendInNeed(open,closed,state);
					if(regionNode  != null)
					{
//						Region toRegion = unPickPath(regionNode);
//						if(toRegion != null)
//						{
//							int armiesToMove = fromRegion.getArmies()-1;
//							attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), fromRegion, toRegion, armiesToMove));
//							toRegion.setArmies(toRegion.getArmies()+armiesToMove);
//							
//						}
						
						int depth = regionNode.getDistanceFromStart();
						int current = 10;
						while(current <= depth)
						{
							Region toRegion = unPickPath(regionNode,current);
							int armiesToMove = fromRegion.getArmies()-1;
							attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), fromRegion, toRegion, armiesToMove));
							fromRegion = toRegion;
							current +=10;
						}
						
					}
				}
			}			
		}
	}

	private void getMineAndEnemyRegions(BotState state) {
		for (Region region : visibleRegions) {
			if(region.ownedByPlayer(state.getMyPlayerName()))
			{
				myRegions.add(region);
			}else{
				enemyRegions.add(region);
			}
		}
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
			
		return rank == null?null:regionRank;
	}
	
	
	private Region unPickPath(RegionNode goal, int depth)
	{
		if(goal.getDistanceFromStart() == depth)
		{
			return goal.getRegion();
		}else if(goal.getDistanceFromStart() == 0)
		{
			return null;
		}
		else{
			return unPickPath(goal.getParentRegion(),depth);
		}
	}
	
	private RegionNode closestFriendInNeed(List<RegionNode> open,List<RegionNode> closed, BotState state)
	{
		RegionNode currentNode = getLowestCostNode(open);
		open.remove(currentNode);
		closed.add(currentNode);

		for (Region neighBourToOpen : currentNode.getRegion().getNeighbors()) {
			boolean cont = true;
			for (RegionNode regionNode : closed) {
				if(regionNode.getRegion().equals(neighBourToOpen))
				{
					cont = false;
					break;
				}
			}
			if(!cont)
			{
				break;
			}
			
			if(neighBourToOpen.getPlayerName().equals(state.getMyPlayerName()))
			{
				
				boolean add = true;
				for (RegionNode regionNode : open) {
					if(regionNode.getRegion().equals(neighBourToOpen))
					{
						if(currentNode.getDistanceFromStart()+10<regionNode.getDistanceFromStart())
						{
							regionNode.setParentRegion(currentNode);
							regionNode.setDistanceFromStart(currentNode.getDistanceFromStart()+10);
							add=false;
							break;
						}
					}
				}
				if(add)
				{
					open.add(new RegionNode(currentNode,neighBourToOpen,currentNode.getDistanceFromStart()+10));
				}
			}
			if(!neighBourToOpen.getPlayerName().equals(state.getMyPlayerName()))
			{
				return new RegionNode(currentNode,neighBourToOpen,currentNode.getDistanceFromStart()+10);
			}
		}
		
		if(open.isEmpty())
		{
			return null;
		}else{
			return closestFriendInNeed(open,closed,state);
		}
		
	}
	private RegionNode getLowestCostNode(List<RegionNode> open) {
		Integer distance = 1000000000;
		RegionNode node = null;
		for (RegionNode regionNode : open) {
			if(regionNode.getDistanceFromStart()< distance)
			{
				distance = regionNode.getDistanceFromStart();
				node = regionNode;
			}
			
		}
		
		return node;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
