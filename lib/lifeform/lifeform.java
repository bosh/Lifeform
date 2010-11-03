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
	
	String current_behavior;
	Map behaviors = new HashMap();

	ArrayList types = new ArrayList();

	ArrayList conditions = new Arraylist(); //Any currently-applied effects such as burning

	public java.applet.Applet get_applet() { //May want to work specifically with game and thus use platform
		return this.applet;
	}

	public void set_name(String name) { this.name = name; }
	public String name() { return name; } //Returns full name
	public abstract String short_name() {} //Returns a shorter name
	public abstract String render_name() {}; //Returns the name to display when rendered

	public void set_state(String state) {
		this.state = state;
	}

	public String get_state() {
		return state;
	}

	public boolean is_alive() {
		return is_state("Alive");
	}

	public boolean is_dead() {
		return is_state("Dead");
	}

	public boolean is_spawned() {
		return (spawned == true);
	}

	public boolean is_not_state(String st) {
		return (!is_state(st));
	}

	public boolean is_state(String st) {
		return (get_state() == st);
	}

	public boolean spawn() { //Returns false if was already spawned
		if (spawned == false) {
			String on_spawn = get_interaction("on_spawn");
			if (on_spawn != null) {
				activate_interaction(on_spawn);
			}
			state = "Alive";
			spawned = true;
			render();
			return true;
		} else {
			return false;
		}
	}

	public void despawn() { //This set might be better as behaviors or interactions
		if (spawned == true) {
			spawned = false;
			remove();
		}
	}

	public abstract void remove() {} //Pop out removal
	public abstract void destroy() {}; //Death via game mechanics

	//Statistics are static values set at create/spawn time that change rarely
	public void set_statistic(String stat, int value) { //needs to have the ability to save any type of statistic
		statistics.put(stat, value);
	}

	public boolean increment_statistic(String stat, int amount) { //Returns false if over max, sets value to max
		//do max checking
		set_statistic(stat, get_raw_statistic + amount);
		return true;
	}

	public boolean decrement_statistic(String stat, int amount) { //Returns false if under min, sets value to min
		//do min checking
		set_statistic(stat, get_raw_statistic - amount);
		return true;
	}

	public int get_statistic(String stat) { //needs to be as type-inspecific as the above
		//get modifiers here
		int value = get_raw_statistic(stat);
		//return multiplicativemods*(additivemodifiers + value)
		return value;
	}

	int get_raw_statistic(String stat) {
		return (int) statistics.get(stat);
	}

	//Resources are values that should have min and max, and can change on the fly or on tick
	public void set_resource(String res, int value) {}; //^ statistics
	public void empty_resource(String res){}; //Drain/set to 0
	public void fill_resource(String res){}; //Set to max, if known, else 100
	public boolean decrement_resource(String res, int amount, boolean force){}; //returns false if couldn't decrement that much
	public boolean increment_resource(String res, int amount, boolean force){}; //returns true if over maximum, force causes over max, else rounds to maximum
	public int get_resource(String res) {}; //^set

	//Actions are the set of things the lifeform is doing or is going to do
	public void queue_actions(int count) {}; //Enqueues count number of action time (sum of actions' times will be > count)
	public String current_action() {}; //Eventually becomes an Action type/interface (Action.execute with arguments like target)
	public void stop_current_action(boolean force) {}; //Drops the current, force drops current and any actions of the same type in a row

	//Interactions are the possible things to enqueue to actions. interactions may directly enqueue or may create action objects that contain values for the specific instance of the action to carry out
	public void set_interaction(String name, String interaction) {}; //String interaction => Interaction interaction, another interface
	public boolean has_interaction(String inter) {};
	public String get_interaction(String inter) {};	//Interactions as an interface that has Interaction.interact() or something
	String[] get_listening_interactions() {}; //Returns the interactions worth calling back on
	public void activate_interaction(String inter) {};
	public void deactivate_interaction(String inter) {};

	//Behaviors are higher level AI patterns that determine how to enqueue actions
	public void add_behavior(String name, String behavior) {}; //String behavior => Behavior behavior
	public boolean has_behavior(String behavior) {};
	public String get_behavior(String behavior) {};
	public void activate_behavior(String behavior) {};
	public void deactivate_behavior(String behavior) {};
	public void set_current_behavior(String behavior) {}; //Sets
	public void randomize_behavior();
	public void stop_behavior(); //Dumps current behavior and replaces with null or default

	//Types ... might as well be booleans in statistics, to be honest
	public void add_type(String name) {};
	public void remove_type(String name) {};
	public void replace_type(String remove, String add) {};
	public boolean is_type(String type) {}; //after implementing CreatureType, turn this into CreatureType type
	public boolean is_not_type(String type) {}; //^

	//Conditions should implement an interface that is apply(), remove(), recover(), tick() (or something)
	public boolean has_condition(String condition) {}; //conditions are like Burning
	public boolean does_not_have_condition(String condition) {}; //implement ConditionalEffect and replace String with that
	
	public void tick() {}; //runs a step worth of the creature whenever the app determines appropriate
	public void render() {}; //until I implement renderable, which is an interface that includes public void render()
}