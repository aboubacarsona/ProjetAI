package principal;

import java.util.List;

public class Agent {

  private int agent_vie;
  private String agent_position;
  //private int agent_tuer;
  private int coup_reussi;
  private double gain_en_dollars;
  private double contribution;

  public Agent(int vie, String position) {
    this.agent_vie = vie;
    this.agent_position = position;
    //this.agent_tuer = 0;
    this.coup_reussi = 0;
    this.gain_en_dollars = 0.0;
    this.contribution = 0.0;
  }
  
  public String getTypeAgent() {
	  return "";
  }
  
  public double getContribution() {
	return contribution;
  }


  public void setContribution(double contribution) {
	this.contribution = contribution;
  }


  public void AddCoup_reussi() {
	  this.coup_reussi++;
  }
  public void setGainEnDollars(double gain) {
	  this.gain_en_dollars = gain;
  }
  public double getGainEnDollars() {
	  return this.gain_en_dollars;
  }
  public int getCoup_reussi() {
	  return this.coup_reussi;
  }
  /*public void Ajout_Tuer() {
	  this.agent_tuer = this.agent_tuer +1;
  }
  public int getTotalMort() {
	  return this.agent_tuer;
  }*/
  
  public int getVie() {
    return this.agent_vie;
  }

  public void setVie(int vie) {
    this.agent_vie = this.agent_vie + vie;
  }

  public String getPosition() {
    return this.agent_position;
  }

  public void setPosition(String position) {
    this.agent_position = position;
  }
  public String coupdepointOucoupdepied(List<String> choices) {
	    return "";
  }
  public void feedback(int utility) {
		    
  }
  
}