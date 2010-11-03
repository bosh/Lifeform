package lifeform;

import java.awt.*;
import java.util.*; //HashMap, Queue

/* Name, Resources, Actions, Interactions, Behaviors, Types, Statistics, Conditions */

public abstract class Lifeform {
	java.applet.Applet applet;
	String name;

	String state;
	boolean spawned = false;

	Map statistics = new HashMap(); //Of key: string, value: int
	Map resources = new HashMap(); //Of key: string, value: Resource

	Queue actions = new LinkedList(); //The current action and any that are queued to happen
	Map interactions = new HashMap();

	String currentBehavior;
	Map behaviors = new HashMap();

	ArrayList types = new ArrayList();

	ArrayList conditions = new Arraylist(); //Any currently-applied effects such as burning

	public java.applet.Applet getApplet() { //May want to work specifically with game and thus use platform
		return this.applet;
	}

	public void setName(String name) { this.name = name; }
	public String name() { return name; } //Returns full name
	// public abstract String shortName() {} //Returns a shorter name
	// public abstract String renderName() {}; //Returns the name to display when rendered

	public void setState(String state) { this.state = state; }
	public String getState() { return state; }
	public boolean isAlive() { return isState("Alive"); }
	public boolean isDead() { return isState("Dead"); }
	public boolean isSpawned() { return (spawned == true); }
	public boolean isNotState(String st) { return (!isState(st)); }
	public boolean isState(String st) { return (getState() == st); }

	public boolean spawn() { //Returns false if was already spawned
		if (spawned == false) {
			String on_spawn = getInteraction("on_spawn");
			if (on_spawn != null) {
				activateInteraction(on_spawn);
			}
			state = "Alive";
			spawned = true;
			render();
			return true;
		} else {
			return false;
		}
	}

	public abstract void remove() {} //Pop out removal
	
	public void despawn() { //This set might be better as behaviors or interactions
		if (spawned == true) {
			setState("Unspawned");
			spawned = false;
			remove();
		}
	}

	public void destroy() { //Death via game mechanics
		setState("Dead");
		if(interactions.containsKey("on_death")) {
			interactions.get("on_death"); //and run it
		}
		remove();
	}

	//Statistics are static values set at create/spawn time that change rarely
	public void setStatistic(String stat, int value) { statistics.put(stat, value); } //needs to have the ability to save any type of statistic

	public boolean incrementStatistic(String stat, int amount) { //Returns false if over max, sets value to max
		int new_amount = getRawStatistic() + amount;
		//do max checking
		setStatistic(stat, new_amount);
		return true;
	}

	public boolean decrementStatistic(String stat, int amount) { //Returns false if under min, sets value to min
		int new_amount = getRawStatistic() - amount;
		//do min checking
		setStatistic(stat, new_amount);
		return true;
	}

	public int getStatistic(String stat) { //needs to be as type-inspecific as the above
		//get modifiers here
		int value = getRawStatistic(stat);
		//return multiplicativemods*(additivemodifiers + value)
		return value;
	}

	private int getRawStatistic(String stat) { return (int) statistics.get(stat); }

	//Resources are values that should have min and max, and can change on the fly or on tick
	public void setResource(String res, int value) { resources.put(res, value); }

	public void emptyResource(String res){ //Drain/set to 0
		setResource(res, 0); //should be resource.min
	}

	public void fillResource(String res){ //Set to max, if known, else 100
		setResource(res, 100); //should be resource.max
	}

	public boolean decrementResource(String res, int amount, boolean force){ //returns false if couldn't decrement that much
		int resource = getResource(res);
		//do not decrement if the result would be below 0 and return false
		setResource(resource - amount);
		return true;
	}

	public boolean incrementResource(String res, int amount, boolean force){ //returns true if over maximum, force causes over max, else rounds to maximum
		int resource = getResource(res);
		setResource(res, resource + amount);
		return false;
	}

	public int getResource(String res) { resources.get(res); }

	//Actions are the set of things the lifeform is doing or is going to do
	public void queueActions(int duration) { //Enqueues count number of action time (sum of actions' times will be > count)
		behavior = getCurrentBehavior();
		//then actions.add() each of behavior.generate_action_pattern(duration) array;
	}
	
	//Eventually becomes an Action type/interface (Action.execute with arguments like target)
	public String currentAction() { return actions.peek(); }

	public void stopCurrentAction(boolean force) { //Drops the current, force drops current and any actions of the same type in a row
		//if (actions.peek() != null) { actions.peek().stop_execution(); }
		actions.poll();
	}

	//Interactions are the possible things to enqueue to actions. interactions may directly enqueue or may create action objects that contain values for the specific instance of the action to carry out

	//String interaction => Interaction interaction, another interface
	public void setInteraction(String name, String interaction) { interactions.put(name, interaction); }

	//Interactions as an interface that has Interaction.interact() or something
	public String getInteraction(String inter) { return interactions.get(inter); }

	private ArrayList getListeningInteractions() { //Returns the interactions worth calling back on
		ArrayList listeners = new ArrayList();
		//for each interaction, if it's a callback type, add it to the list
		return listeners;
	}

	//Behaviors are higher level AI patterns that determine how to enqueue actions
	public void addBehavior(String name, String behavior) { behaviors.put(name, behavior); }
	public String getBehavior(String behavior) { return behaviors.get(behavior); }
	public void setCurrentBehavior(String behavior) { currentBehavior = behavior; }
	public String getCurrentBehavior() { return currentBehavior; }

	public boolean activateBehavior(String behavior) { //Returns false if the behavior is not present
		//TODO: allow behaviors to change state from nonselectable to selectable sets
		return true;
	}

	public boolean deactivateBehavior(String behavior) { //Returns false if the behavior is not present
		//TODO: see above
		return true;
	}

	public void randomizeBehavior() {
		String behavior = "";
		//TODO: make this a random behavior from the possible set
	}

	public void stopBehavior() { //Dumps current behavior and replaces with null or default
		String default_behavior = null;
		//if there's a default, choose that instead
		setCurrentBehavior(default_behavior);
	}

	//Types ... might as well be booleans in statistics, to be honest
	public void addType(String name) { types.add(name); }
	public void removeType(String name) { types.remove(types.indexOf(name)); }
	public boolean isType(String type) { return (types.indexOf(type) != -1); }
	public boolean isNotType(String type) { return (!isType(type)); }

	//Conditions should implement an interface that is apply(), remove(), recover(), tick() (or something)
	public boolean hasCondition(String condition) { return (conditions.indexOf(condition) != -1) }
	public boolean doesNotHaveCondition(String condition) { return (!hasCondition(condition)); }

	public abstract void tick() {}; //runs a step worth of the creature whenever the app determines appropriate
	public abstract void render() {}; //until I implement renderable, which is an interface that includes public void render()
}
