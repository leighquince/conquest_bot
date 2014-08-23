package main;
import java.util.LinkedList;

public class SuperRegion {
	
	private int id;
	private int armiesReward;
	private LinkedList<Region> subRegions;
	
	public SuperRegion(int id, int armiesReward)
	{
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
	}
	
	public void addSubRegion(Region subRegion)
	{
		if(!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}
	
	
	public double getPercentageOwned(String playerName){
		double superRegionSize = 0;
		double iOwn = 0;
		for (Region region : this.subRegions) {
			superRegionSize++;
			if(region.ownedByPlayer(playerName))
			{
				iOwn++;
			}			
		}	
		return superRegionSize/iOwn;		
	}
	
	/**
	 * @return A string with the name of the player that fully owns this SuperRegion
	 */
	public String ownedByPlayer()
	{
		String playerName = subRegions.getFirst().getPlayerName();
		for(Region region : subRegions)
		{
			if (!playerName.equals(region.getPlayerName()))
				return null;
		}
		return playerName;
	}
	
	/**
	 * @return The id of this SuperRegion
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
	 */
	public int getArmiesReward() {
		return armiesReward;
	}
	
	
	@Override
	public String toString(){
		String output = "\n";
		output += String.format("============Super Region %s=============%n",this.id);
		output += String.format("Reward: %s%n",this.armiesReward);
		output += String.format("Size: %s%n",this.subRegions.size());
		output += String.format("Sub Regions:%n",this.id);
		for (Region region : this.subRegions) {
			output += String.format("\t Sub Region %s owned by %s%n",region.getId(), region.getPlayerName());	
		}
		output += String.format("========================================%n");
		return output;	
	}
	
	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public LinkedList<Region> getSubRegions() {
		return subRegions;
	}
}
