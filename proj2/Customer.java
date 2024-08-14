package cmsc433;

import java.util.List;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order. When running, an
 * customer attempts to enter the Ratsie's (only successful if the
 * Ratsie's has a free table), place its order, and then leave the
 * Ratsie's when the order is complete.
 */
public class Customer implements Runnable {
	// JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;
	private static int count = 0;
	/**
	 * You can feel free modify this constructor. It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order) {
		this.name = name;
		this.order = order;
		this.orderNum = count++;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	/**
	 * This method defines what an Customer does: The customer attempts to
	 * enter the Ratsie's (only successful when the Ratsie's has a
	 * free table), place its order, and then leave the Ratsie's
	 * when the order is complete.
	 */
	public void run() {
		// YOUR CODE GOES HERE...
		Simulation.logEvent( SimulationEvent.customerStarting(this));
		Rat.instance.enterRat(this);			
		Simulation.logEvent(SimulationEvent.customerEnteredRatsies(this));
		Rat.instance.placeOrder(this, order, orderNum);

		Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order, orderNum));	
		

		synchronized(Rat.instance.getLock(orderNum)) {
			while (Rat.instance.isCooking(orderNum)) {
				try {
					Rat.instance.getLock(orderNum).wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, order, orderNum));
		Simulation.logEvent(SimulationEvent.customerLeavingRatsies(this));
		Rat.instance.leaveRat(this);






	}

}
