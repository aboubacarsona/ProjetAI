package principal;
import java.util.HashMap;
import java.util.List;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class Simulateur {

  Random rand;
  
  private int nbr_joueur; 		// Nombre de joueurs
  private int nbr_ennemi; 		// Nombre d'ennemis
  private double montant;		// Montant à partager à la fin du jeu
  HashMap<Integer, Agent> liste_agents_Ennemi; 			// Liste des agents ennemis
  HashMap<Integer, Agent>  liste_agents_Defenseur; 		// Liste des agents défenseurs
  private int nombre_de_vie; 		// Nombre de vies par agent
  List<String>  liste_de_position;		// Liste des positions possibles dans le jeu
  List<String>  liste_action;			// Liste des actions possibles
  List<List<Integer>> la_liste_de_coallition;			// Liste des coalitions possibles

  //Constructeur du simulateur
  Simulateur(int _nbrEnnemi, int _nbrJoueur, double montantToShare, int vie_par_agent) {
    rand = new Random();
    this.liste_agents_Ennemi = new HashMap<Integer, Agent>();
    this.liste_agents_Defenseur = new HashMap<Integer, Agent>();
    this.liste_de_position = new ArrayList<String>();
    this.liste_action = new ArrayList<String>();
    // Initialisation des actions possibles
    this.liste_action.add("Poing");
    this.liste_action.add("Pied");
    this.nombre_de_vie = vie_par_agent; // Initialisation du nombre de vies par agent
    this.la_liste_de_coallition = new ArrayList<List<Integer>>();
    // Initialisation du nombre de joueurs, d'ennemis et du montant à partager
    this.nbr_joueur = _nbrJoueur;
    this.nbr_ennemi = _nbrEnnemi;
    this.montant = montantToShare;
    
    // Initialisation des positions possibles
    liste_de_position.add("Gauche");    liste_de_position.add("Droite");    liste_de_position.add("Haut");    liste_de_position.add("Bas");    liste_de_position.add("Centre");
    
    // Affichage des informations sur le jeu
    AffichageInfoJeu();
    
    // Simulation des combats pour chaque position
    for (int i = 0; i < liste_de_position.size(); i++) {
      CombatParPosition(liste_de_position.get(i)); 
    }
    // Calcul du nombre d'ennemis morts
    int ennemi_mort = calculEnnemiMort();
    
	affectationGain(ennemi_mort);		//Affectation de gains
    
	// Debut d'affichage des infos agents
	System.out.println("Informations sur les agents");
	System.out.println("----------------------------");
	int total_coup = 0; double total_contrib = 0; double total_gain = 0;
	double gainEpsilon = 0; double gainUCB =0;
    for (int i : liste_agents_Defenseur.keySet()) {
    	total_coup += liste_agents_Defenseur.get(i).getCoup_reussi();
    	total_contrib += liste_agents_Defenseur.get(i).getContribution();
    	total_gain += liste_agents_Defenseur.get(i).getGainEnDollars();
    	System.out.printf("Agent "+i+"("+liste_agents_Defenseur.get(i).getCoup_reussi()+" coups) ==> "+liste_agents_Defenseur.get(i).getCoup_reussi()/ennemi_mort+" ennemi tué ==>\t|   contribution de: "+liste_agents_Defenseur.get(i).getContribution()+"\t|  gain : %.2f $\t|   Position :"+liste_agents_Defenseur.get(i).getPosition(), liste_agents_Defenseur.get(i).getGainEnDollars());
    	System.out.println();
    	if(liste_agents_Defenseur.get(i).getTypeAgent().equals("UCB"))
    		gainUCB += liste_agents_Defenseur.get(i).getGainEnDollars();
    	else
    		gainEpsilon += liste_agents_Defenseur.get(i).getGainEnDollars();
    }
    System.out.println("----------------------");
    System.out.printf("Total ("+total_coup+" coups )\t==>  Contribution: %.2f\t==>  gain :%.2f", total_contrib, total_gain);
    System.out.println();
    System.out.printf("Gain Agent Epsilon\t==>  %.2f $\t|\tGain Agent UCB\t==>  %.2f $",gainEpsilon, gainUCB);
    System.out.println();
    System.out.println();
    //Fin d'affichage infos agents
    
    
	EstUnCoeurOuNon(ennemi_mort);		//Affiche si le partage est dans le coeur ou non

	//Debut des infos sur les coallitions
    System.out.println("");
    System.out.println("Info sur les combats des quelques coalitions avec une contribution >= 1");
	System.out.println("----------------------------");
	int to_break = 0;
    for (List<Integer> liste_i : la_liste_de_coallition) { to_break++;
    	int gain = gainCoallition(liste_i, 0);
    	if(gain >=1)
    		System.out.println(liste_i+"\t ==>  "+gainCoallition(liste_i, 0)+" ennemi tué");
    	if(to_break >30)
    		break;
    }
    //Fin des infos sur les coallitions
    
  }

  //Creations des agents ainsi que la formation de coallition
  public void creationAgent(int nbr_ennemi, int nbr_joueur) {
    for (int i = 0; i < nbr_ennemi; i++) {
      liste_agents_Ennemi.put(i, new RandomAgent(nombre_de_vie, liste_de_position.get(rand.nextInt(5))));
    }
    for (int i = 0; i < nbr_joueur; i++) {
    	if(rand.nextInt(2)==0)
    		liste_agents_Defenseur.put(i, new UCBAgent(nombre_de_vie, liste_de_position.get(rand.nextInt(5))));
    	else
    		liste_agents_Defenseur.put(i, new EpsilonGreedyAgentDecrease(nombre_de_vie, liste_de_position.get(rand.nextInt(5)), 0.9, 0.99));
    }
    //Rempli la liste des coallitions
    GetAllCoalitions(liste_agents_Defenseur.keySet(), la_liste_de_coallition);
  }

  /*Simulation du combat pour une position donnée
  Combattre sur toutes les positions tant qu'on a des ennemi
  Si tous les defenseurs sont morts les ennemi passent aux centre pour retrouver d'autres defenseurs*/
  public void CombatParPosition(String pos) {
	
	HashMap<Integer, Agent> ennemis_in_pos = getAgentInPosition(liste_agents_Ennemi, pos);
	HashMap<Integer, Agent> defenseurs_in_pos = getAgentInPosition(liste_agents_Defenseur, pos);
	//System.out.println(pos+" "+ennemis_in_pos.size()+" "+defenseurs_in_pos.size());
	while (defenseurs_in_pos.size() > 0 && ennemis_in_pos.size() > 0) {	
		HashMap<Integer, Agent> defens = new HashMap<Integer, Agent>();
		defens.putAll(defenseurs_in_pos);
		for (int i : defens.keySet()) {
			if(ennemis_in_pos.size()==0)
				break;
			int key_enn = ennemis_in_pos.keySet().iterator().next();
			String ag_mort = combatDefenseurEnnemi(defenseurs_in_pos.get(i), ennemis_in_pos.get(key_enn));
			//System.out.println("Un "+ag_mort+" est mort"+" à la position "+pos);
			if(ag_mort.equals("Defenseur")) {
				liste_agents_Defenseur.get(i).feedback(-1);
				defenseurs_in_pos.remove(i);
			}else {
				ennemis_in_pos.remove(key_enn);
				//liste_agents_Defenseur.get(i).Ajout_Tuer();
				liste_agents_Defenseur.get(i).feedback(+1);
			}
		}
	}
	for (int i : ennemis_in_pos.keySet()) {
		liste_agents_Ennemi.get(i).setPosition("Centre");
	}
	
	

  }

  // combat de corps à corps entre deux agents
  public String combatDefenseurEnnemi(Agent joueur, Agent ennemi) {
    while (joueur.getVie() > 0 && ennemi.getVie() > 0) {
    	String choix_enn = ennemi.coupdepointOucoupdepied(liste_action);
    	String choix_def = joueur.coupdepointOucoupdepied(liste_action);
    	if(choix_def.equals(choix_enn)) {
    		ennemi.setVie(-1);
    		joueur.AddCoup_reussi();
    	}else {
    		joueur.setVie(-1);
    	}
    }
    
    if(joueur.getVie()==0)
    	return "Defenseur";

    return "Ennemi";
  }
  
  //Recuperer la liste des agents se trouvant à une position
  public HashMap<Integer, Agent> getAgentInPosition(HashMap<Integer, Agent> les_agents, String pos) {
	HashMap<Integer, Agent> listeToReturn = new HashMap<Integer, Agent>();
	for (int i : les_agents.keySet()) {
      if (les_agents.get(i).getPosition().equals(pos) && les_agents.get(i).getVie() > 0) {
        listeToReturn.put(i, les_agents.get(i));
      }
    }
    return listeToReturn;
  }
  
  //Cacul de la contribution d'un agent
  public double calculContribution(int id_agent, int _nbrJoueur ) {
	  int contrib = (liste_agents_Defenseur.get(id_agent).getCoup_reussi()/nombre_de_vie);
	  BigDecimal vrai_contrib = new BigDecimal(calculateFactorial(_nbrJoueur-1).multiply(BigInteger.valueOf(contrib)) );
	 	  for (List<Integer> liste_i : la_liste_de_coallition) {
		  if (!liste_i.contains(id_agent)) {
			  
			  int number = _nbrJoueur-liste_i.size()-1;

			  BigInteger partie1 = calculateFactorial(liste_i.size()).multiply(calculateFactorial(number));
			  int partie2 = gainCoallition(liste_i, liste_agents_Defenseur.get(id_agent).getCoup_reussi()) - gainCoallition(liste_i, 0);
			  BigDecimal v = (new BigDecimal(""+partie1.multiply(BigInteger.valueOf(partie2))));
			  vrai_contrib = vrai_contrib.add(v);
		}
	 
	  }
	  BigDecimal unsurN=  new BigDecimal(calculateFactorial(_nbrJoueur));
	  
	  BigDecimal cont = vrai_contrib.divide(unsurN, 2, RoundingMode.HALF_UP);
	  double val = cont.doubleValue();
	  return val;
  }
  
  //calcul du gain de la coalition
  public int gainCoallition(List<Integer> liste_id_agents, int coup_agent) {
	  
	  int coupresussie = coup_agent;
	  for (int j = 0; j < liste_id_agents.size(); j++) {
		  coupresussie += liste_agents_Defenseur.get(liste_id_agents.get(j)).getCoup_reussi();
	  }
	  //System.out.println(liste_id_agents+" "+coupresussie +"-"+ coupresussie/nombre_de_vie);
	  return coupresussie/nombre_de_vie;
  }
  
  //Function pour avoir le factoriel d'un nombre
  public BigInteger calculateFactorial(int n) {
      BigInteger result = BigInteger.ONE;
      for (int i = 1; i <= n; i++) {
          result = result.multiply(BigInteger.valueOf(i));
      }
      return result;
  }
  
  //Avoir la liste de toutes les coalitions
  public static void GetAllCoalitions(Set<Integer> set, List<List<Integer>> allCoalitions) {
      List<Integer> agentList = new ArrayList<>();
      for (int i : set) {
          agentList.add(i);
      }
      
      for (int longueur = 1; longueur <= set.size(); longueur++) {
          combinaisonsDeLongueurN(agentList, longueur, new ArrayList<>(), 0, allCoalitions);
      }
  }
  
  //Fonctions secondaire de construction de la coallition
  public static void combinaisonsDeLongueurN(List<Integer> list, int longueur, List<Integer> combinaison, int debut, List<List<Integer>> toFill) {
      if (longueur == 0) {
          // Ajouter la copie de la combinaison à la liste
          toFill.add(new ArrayList<>(combinaison));
          return;
      }

      for (int i = debut; i < list.size(); i++) {
          combinaison.add(list.get(i));
          combinaisonsDeLongueurN(list, longueur - 1, combinaison, i + 1, toFill);
          combinaison.remove(combinaison.size() - 1);
      }
  	}
  	
  //Afectation des contributions et des gains
  public void affectationGain(int ennemi_mort) {
  		for (int i : liste_agents_Defenseur.keySet()) {
  			double contrib = calculContribution(i, nbr_joueur);
  			liste_agents_Defenseur.get(i).setContribution(contrib);
  			double gain = 0;
  			if(ennemi_mort>0)
  				gain = (contrib*montant)/ennemi_mort;
  			liste_agents_Defenseur.get(i).setGainEnDollars(gain);
  		}
  	}
  	
  //Est dans le coeur ou pas
  public List<Integer> EstUnCoeurOuNon(int ennemi_mort) {
  		List<Integer> liste_vide = new ArrayList<Integer>();
  		for (List<Integer> liste_i : la_liste_de_coallition) {
  			int gsomme_gain_seul =0; int somme_gain_groupe =0;
  			for (int i = 0; i < liste_i.size(); i++) {
				/*if(liste_agents_Defenseur.get(i).getGainEnDollars() < (liste_agents_Defenseur.get(i).getTotalMort()*montant)/ennemi_mort) {
					return i;
				}*/
				somme_gain_groupe += liste_agents_Defenseur.get(i).getGainEnDollars();
				gsomme_gain_seul += ((liste_agents_Defenseur.get(i).getCoup_reussi()/nombre_de_vie)*montant)/ennemi_mort;
			}
  			if(somme_gain_groupe < gsomme_gain_seul) {
  				System.out.println("Le partage des gain selon shapley value n'est pas dans le coeur :");
  	  			System.out.println("-Par ce que la coalition :"+liste_i+" obtient "+gsomme_gain_seul+"$ en combatant tout seul par rapport à "+somme_gain_groupe+" en s'alliant aux groupe !");
  	  			liste_vide.addAll(liste_i);
  	  			return liste_i;
  			}
  				
  		}
  		System.out.println("Le partage des gain selon shapley value est dans le coeur !");
  		System.out.println("-Il n'y a aucun Agents et aucune sous coallition qui gagne plus en jouant seul qu'en jouant en groupe");
  		return liste_vide;
  	}
  
  //Calcul du nombre d'ennemi morts
  public int calculEnnemiMort() {
  		int ennemi_mort = 0;
		for (int i : liste_agents_Defenseur.keySet()) {
			ennemi_mort += liste_agents_Defenseur.get(i).getCoup_reussi();
		}
		return ennemi_mort/nombre_de_vie;
  	}
  
  //Affichage d'infos du jeu
  public void AffichageInfoJeu() {
  		System.out.println("Informations sur la simulation jeu");
  		System.out.println("-----------------------");
  		System.out.println("Nombre de défenseurs\t=> "+nbr_joueur+"\nNombre d'ennemis\t=> "+nbr_ennemi+"\nNombre de vies par agent \t=>"+nombre_de_vie);
  		System.out.println("Liste des position\t=> "+liste_de_position);
  		System.out.println("Gain à ce partager à la fin du jeu\t=> "+montant+ " $");
  		System.out.println();
  	    creationAgent(nbr_ennemi, nbr_joueur);
  	}
  	

}