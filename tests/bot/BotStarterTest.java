package bot;



import org.junit.Before;
import org.junit.Test;

public class BotStarterTest {

	BotStarter bot;
	private BotParser parser;
	
	
	@Before
	public void setUp(){
		this.bot = new BotStarter();
		this.parser = new BotParser(bot);
		this.parser.run();
		
	}
	
	@Test
	public void testBotPicksSouthaAfrica()
	{
		
	}
}
