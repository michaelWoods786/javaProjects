package cmsc433;

/**
 * Machines are used to make different kinds of Food. Each Machine type makes
 * just one kind of Food. Each machine type has a count: the set of machines of
 * that type can make that many food items in parallel. If the machines are
 * asked to produce a food item beyond its count, the requester blocks. Each
 * food item takes at least item.cookTime10S seconds to produce. In this
 * simulation, use Thread.sleep(item.cookTime10S) to simulate the actual cooking
 * time.
 */
public class Machines {

	public enum MachineType {
		sodaMachines, fryers, grillPresses, ovens
	};

	// Converts Machines instances into strings based on MachineType.
	public String toString() {
		switch (machineType) {
			case sodaMachines:
				return "Soda Machines";
			case fryers:
				return "Fryers";
			case grillPresses:
				return "Grill Presses";
			case ovens:
				return "Ovens";
			default:
				return "INVALID MACHINE TYPE";
		}
	}

	public final MachineType machineType;
	public final Food machineFoodType;
	public int available;
	public int countIn;
	// YOUR CODE GOES HERE...



	/**
	 * The constructor takes at least the name of the machines, the Food item they
	 * make, and their count. You may extend it with other arguments, if you wish.
	 * Notice that the constructor currently does nothing with the count; you must
	 * add code to make use of this field (and do whatever initialization etc. you
	 * need).
	 */
	public Machines(MachineType machineType, Food foodIn, int countIn) {
		this.machineType = machineType;
		this.machineFoodType = foodIn;
		Simulation.logEvent(SimulationEvent.machinesStarting(this, foodIn, countIn));
		this.available = countIn;
		this.countIn = countIn;



	}
	private boolean avail() {
		synchronized (this) {
			return available != 0;
		}
	}

	/**
	 * This method is called by a Cook in order to make the Machines' food item. You
	 * can extend this method however you like, e.g., you can have it take extra
	 * parameters or return something other than Object. You will need to implement
	 * some means to notify the calling Cook when the food item is finished.
	 */
	public Thread makeFood(Food f1) throws InterruptedException {
		// YOUR CODE GOES HERE...
		Thread t1 =  new Thread(new CookAnItem(f1, this));
		t1.start();


		return t1;
	}

	// THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {
		Food food;
		 Machines machines;
	
		public CookAnItem(Food f1, Machines m1) {
			this.food = f1;
			this.machines = m1;
		}
		public void run() {
			synchronized(machines) {
				while (!machines.avail()) {
					try {
						machines.wait();
					}
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Simulation.logEvent(SimulationEvent.machinesCookingFood(machines, food));
				available-=1;
			}
			try {
				Thread.sleep(this.food.cookTime10S);
				// REMOVE THIS
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			synchronized(machines) {
				Simulation.logEvent(SimulationEvent.machinesDoneFood(machines, food));
				available+=1;
				machines.notifyAll();
			}
		}
	}
}
