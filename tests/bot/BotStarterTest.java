package bot;



import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;

import main.Map;
import main.Region;
import main.SuperRegion;
import move.AttackTransferMove;

import org.junit.Before;
import org.junit.Test;

public class BotStarterTest {

	BotStarter bot;
	private BotParser parser;
	String[] parts;
	String output = "";
	
	
	@Before
	public void setUp(){
		this.bot = new BotStarter();
		this.parser = new BotParser(bot);
	}
	

	public void setPlayerName()
	{
		parts = "settings your_bot player1".split(" ");
		this.parser.getCurrentState().updateSettings(parts[1], parts[2]);
		assertEquals("player1", this.parser.getCurrentState().getMyPlayerName());
	}

	public void setOpponentName()
	{
		parts = "settings opponent_bot player2".split(" ");
		this.parser.getCurrentState().updateSettings(parts[1], parts[2]);
		assertEquals("player2", this.parser.getCurrentState().getOpponentPlayerName());
	}

	public void setUpMapSupperRegions()
	{
		parts = "setup_map super_regions 1 5 2 2 3 5 4 3 5 7 6 2".split(" ");
		this.parser.getCurrentState().setupMap(parts);
		assertEquals(this.parser.getCurrentState().getFullMap().getSuperRegion(1).getArmiesReward(),5);
		assertEquals(this.parser.getCurrentState().getFullMap().getSuperRegion(2).getArmiesReward(),2);
		assertEquals(this.parser.getCurrentState().getFullMap().getSuperRegion(3).getArmiesReward(),5);
		assertEquals(this.parser.getCurrentState().getFullMap().getSuperRegion(4).getArmiesReward(),3);
		assertEquals(this.parser.getCurrentState().getFullMap().getSuperRegion(5).getArmiesReward(),7);
		assertEquals(this.parser.getCurrentState().getFullMap().getSuperRegion(6).getArmiesReward(),2);
	}

	public void setUpMapRegions()
	{

		parts = "setup_map regions 1 1 2 1 3 1 4 1 5 1 6 1 7 1 8 1 9 1 10 2 11 2 12 2 13 2 14 3 15 3 16 3 17 3 18 3 19 3 20 3 21 4 22 4 23 4 24 4 25 4 26 4 27 5 28 5 29 5 30 5 31 5 32 5 33 5 34 5 35 5 36 5 37 5 38 5 39 6 40 6 41 6 42 6".split(" ");
		this.parser.getCurrentState().setupMap(parts);
		assertEquals(this.parser.getCurrentState().getFullMap().getRegion(1).getSuperRegion().getId(),1);
	}

	public void setUpMapNeighBours()
	{

		parts = "setup_map neighbors 1 2,4,30 2 4,3,5 3 5,6,14 4 5,7 5 6,7,8 6 8 7 8,9 8 9 9 10 10 11,12 11 12,13 12 13,21 14 15,16 15 16,18,19 16 17 17 19,20,27,32,36 18 19,20,21 19 20 20 21,22,36 21 22,23,24 22 23,36 23 24,25,26,36 24 25 25 26 27 28,32,33 28 29,31,33,34 29 30,31 30 31,34,35 31 34 32 33,36,37 33 34,37,38 34 35 36 37 37 38 38 39 39 40,41 40 41,42 41 42".split(" ");
		this.parser.getCurrentState().setupMap(parts);
		LinkedList<Region> neighbors = this.parser.getCurrentState().getFullMap().getRegion(1).getNeighbors();
		assertTrue(neighbors.size()>0);
	}
	public void setUpUpdateMap()
	{
		
		parts = "update_map 1 player1 18 2 player1 2 3 player1 1 4 player1 3 5 player1 2 7 player1 2 8 player1 2 9 player1 2 10 player1 112 11 player1 1 12 player1 2 13 player1 2 14 player1 17 15 player1 2 16 player1 2 17 player1 2 18 player1 2 19 player1 2 21 player1 19 24 player1 1 25 player1 2 26 player1 2 28 player1 55 29 player1 2 30 player1 2 39 player1 2 41 player1 2 6 neutral 2 20 player2 3 27 player2 5 32 player2 1 36 player2 90 22 player2 4 23 player2 34 31 player2 4 33 player2 21 34 player2 1 35 neutral 1 38 player2 1 40 neutral 1 42 neutral 2".split(" ");
		this.parser.getCurrentState().updateMap(parts);		
		assertEquals("player1",this.parser.getCurrentState().getVisibleMap().getRegion(1).getPlayerName());
	}
	
	private void setStartingArmies() {
		parts = "settings starting_armies 7".split(" ");
		this.parser.getCurrentState().updateSettings(parts[1], parts[2]);
		
		assertEquals(this.parser.getCurrentState().getStartingArmies(),7);
	}

	public void testSetup()
	{
		this.setPlayerName();
		this.setOpponentName();
		this.setUpMapSupperRegions();
		this.setUpMapRegions();
		this.setUpMapNeighBours();	
		this.setStartingArmies();
		this.setUpUpdateMap();
	}

	@Test
	public void testAStarSeach(){
		this.testSetup();
		
		
		ArrayList<AttackTransferMove> attackTransferMoves = this.bot.getAttackTransferMoves(this.parser.getCurrentState(), 2000L);
		for(AttackTransferMove move : attackTransferMoves)
			output = output.concat(move.getString() + ",\n");
		
		if(output.length() > 0)
			System.out.println(output);
		else
			System.out.println("No moves");
		
	}
}
