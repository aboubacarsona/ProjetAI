package principal;
import java.util.List;
import java.util.Random;

public class RandomAgent extends Agent {

  Random rand;

  public RandomAgent(int vie, String position) {
    super(vie, position);
    rand = new Random();
  }

  public String getTypeAgent() {
	  return "Random";
  }
  public String coupdepointOucoupdepied(List<String> choices) {
    return choices.get(rand.nextInt(2));
  }
  public void feedback(double score) {
		//do nothing
  }

}
