package cmsc433;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Customer and process them.
 */
public class Cook implements Runnable {
	private final String name;
	private ArrayList< Machines> machines;
	private  HashMap < Integer,  HashMap <Food, LinkedList<Thread>>> cookMachines;

	/**
	 * You can feel free modify this constructor. It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful.
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name, ArrayList<Machines> machines) {
		this.name = name;
		this.machines = machines;
		this.cookMachines = new HashMap<Integer, HashMap<Food, LinkedList<Thread>>>();
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows. The cook tries to retrieve
	 * orders placed by Customers. For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine type, by calling makeFood(). Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified. The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			while (true) {
				Integer orderNum = 0;
				synchronized(Rat.instance.getCurrentOrders()) {
				while (!Rat.instance.activeCustOrders()) {
					Rat.instance.getCurrentOrders().wait();
				}
				orderNum = Rat.instance.getCustomerOrder();
				}
				List<Food>	order = Rat.instance.getOrder(orderNum);
				cookOrder(orderNum, order);

 // REMOVE THIS
			}
		} catch (InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}


public void cookOrder(int orderNum, List<Food> order)throws InterruptedException {
	synchronized (Rat.instance.getLock(orderNum)) {
		Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, order, orderNum));
		cookMachines.put(orderNum, new HashMap<Food, LinkedList<Thread>>());

		int fryCount=0;
		int pizzaCount=0;
		int subCount=0;
		int sodaCount=0;
		
		for (Food f : order) {
			if (f.equals(FoodType.fries)) {
				fryCount++;
			}
			if (f.equals(FoodType.pizza)) {
				pizzaCount++;
			}
			if (f.equals(FoodType.subs)) {
				subCount++;
			}
			if (f.equals(FoodType.soda)) {
				sodaCount++;
			}
		}
		Food fud = null;
		
		for (int i = 0; i < 4; i++) {
			if (i == 0) {
				fud = FoodType.fries;
			}
			else if (i == 1) {
				fud = FoodType.pizza;
			}
			else if (i == 2) {
				fud = FoodType.subs;
			}
			else if (i == 3) {
				fud = FoodType.soda;
			}
			int count = 0;
			if (fud == FoodType.fries) {
				count = fryCount;
			}
			if (fud == FoodType.pizza) {
				count = pizzaCount;
			}
			if (fud == FoodType.subs) {
				count = subCount;
			}
			if (fud == FoodType.soda) {
				count = sodaCount;
			}
			cookMachines.get(orderNum).put(fud, new LinkedList<Thread>());
			Rat.instance.addToCooks(this, orderNum);
			for (int j = 0; j < count; j++) {
				Machines Mymachine = machines.get(i);
				Simulation.logEvent(SimulationEvent.cookStartedFood(this, fud, orderNum));
				Thread thread = Mymachine.makeFood(fud);
				cookMachines.get(orderNum).get(fud).add(thread);
			}
		}
		
		for (Food food : cookMachines.get(orderNum).keySet()) {
			for (Thread thread : cookMachines.get(orderNum).get(food)) {
				thread.join();
				Simulation.logEvent(SimulationEvent.cookFinishedFood(this, food, orderNum));
			}
		}
		
		Rat.instance.addToFinished(this, orderNum);
		Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, orderNum));
		
	}
	
	}
	
}
