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
import java.util.LinkedList;

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
		int armies = 2;
		int armiesLeft = state.getStartingArmies();
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		

		
		//priority
		 //First move gain a super region of size 4!
		 //defend super region borders that neighbour enemy (always try to have a matching number of defenders)
		 //gain regions in super regions where i have a presence
		  //Priorities this with the weighting of how many men i have in the super region vs the number the enemy has
		
		//get a super region
		for (Region region : visibleRegions) {
			if(armiesLeft > 0)
			{
				if(region.getSuperRegion().getId() == 6 || region.getSuperRegion().getId() == 2 )
				{
					if(region.getSuperRegion().ownedByPlayer() != myName)
					{
						if(region.ownedByPlayer(myName))
						{
							placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armiesLeft/2));
							armiesLeft = armiesLeft/2;
						}	
					}
					
				}
			}
		}
		 
		
		while(armiesLeft > 0)
		{
			double rand = Math.random();
			int r = (int) (rand*visibleRegions.size());
			Region region = visibleRegions.get(r);
			
			if(region.ownedByPlayer(myName))
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
				armiesLeft -= armies;
			}
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
		String myName = state.getMyPlayerName();
		int armies = 5;
		
		for(Region fromRegion : state.getVisibleMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(myName)) //do an attack
			{
				ArrayList<Region> possibleToRegions = new ArrayList<Region>();
				possibleToRegions.addAll(fromRegion.getNeighbors());
				
				while(!possibleToRegions.isEmpty())
				{
					double rand = Math.random();
					int r = (int) (rand*possibleToRegions.size());
					Region toRegion = possibleToRegions.get(r);
					
					if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > toRegion.getArmies()+(toRegion.getArmies()/7)) //do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else if(toRegion.getPlayerName().equals(myName) && (toRegion.getSuperRegion().getId() != fromRegion.getSuperRegion().getId()) && fromRegion.getArmies() > 2) //do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else
						possibleToRegions.remove(toRegion);
				}
			}
		}
		
		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
