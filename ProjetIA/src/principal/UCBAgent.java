package principal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UCBAgent extends Agent {
  Random rand;
  private Map<String, Integer> counts;
  private Map<String, Double> avgReward;
  private String lastChoice = "";
  private int counTotal;

  public UCBAgent(int vie, String position){
	super(vie, position);
    rand = new Random();
    counts = new HashMap<>();
    avgReward = new HashMap<>();
    this.counTotal =0;
  }

  public String getTypeAgent() {
	  return "UCB";
  }

    public void feedback(double score) {
      // Update reward and count for the last choice
      int oldCount = this.counts.getOrDefault(this.lastChoice, 0);
      double oldReward = this.avgReward.getOrDefault(this.lastChoice, 0.0);
      this.avgReward.put(this.lastChoice, (oldCount * oldReward + score) / (oldCount + 1));
      this.counts.put(this.lastChoice, oldCount + 1);
      this.counTotal ++;
    }


  public String coupdepointOucoupdepied(List<String> choices) {
      String argmax = choices.get(0);
      double maxUCB = -1;


      for (String s : choices) {

        if (counts.getOrDefault(s, 0)== 0) {
          lastChoice = s;
            return s;
        }

        double ucb = calculateUCB(s);
        if (ucb > maxUCB) { 
          argmax = s;
          maxUCB = ucb;
        }else if(ucb == maxUCB){
          int h = rand.nextInt(2);
          if(h==1){argmax = s; maxUCB = ucb;}
        }
      }
      //tableau de meilleur
      lastChoice = argmax;
      return argmax;



    }

  private double calculateUCB(String action) {
    int c = counts.getOrDefault(action, 0);
    double v = avgReward.getOrDefault(action, 0.0);

    double explorationFactor = Math.sqrt(2 * Math.log(this.counTotal) / c);
    return v + explorationFactor;
  }



}