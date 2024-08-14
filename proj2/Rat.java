package cmsc433;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Rat {
	public static List<SimulationEvent> events;
	public static Rat instance;
	public int numCustomers;
	public int numCooks;
	public int numTables;
	public int machineCount;
	public boolean random;
	public Object tableLock = new Object();
	public Object orderNumLock = new Object();
	public HashMap <Integer, Object> locks ;
	public ArrayList<Machines> machines;
	public HashMap <Integer, List<Food>> ordersNum ;
	public HashSet<Customer> tables;
	public HashSet<Integer> orders;
	public HashSet<Integer> currentOrders;
	public HashSet<Integer> cooksOrders;

	
	Rat (
			int numCustomers, int numCooks,
			int numTables, int machineCount,
			boolean randomOrders) {
			this.numCustomers = numCustomers;
			this.numCooks = numCooks;
			this.numTables = numTables;
			this.machineCount = machineCount;
			this.random = randomOrders;
			instance = this;
			// This method's signature MUST NOT CHANGE.

			/*
			 * We are providing this events list object for you.
			 * It is the ONLY PLACE where a concurrent collection object is
			 * allowed to be used.
			 */

			orders = new HashSet<Integer>();
			currentOrders = new HashSet<Integer>();
			ordersNum = new HashMap<Integer, List<Food>>();
			cooksOrders = new HashSet<Integer>();
			machines = new ArrayList<Machines>(machineCount);
			locks = new HashMap<Integer, Object>();
			tables = new HashSet<Customer>();
			
			machines.add(new Machines(Machines.MachineType.fryers, FoodType.fries, machineCount));
			machines.add((new Machines(Machines.MachineType.ovens, FoodType.pizza, machineCount)));
			machines.add((new Machines(Machines.MachineType.grillPresses, FoodType.subs, machineCount)));
			machines.add((new Machines(Machines.MachineType.sodaMachines, FoodType.soda, machineCount)));
			Thread[] cookThreads = new Thread[numCooks];

			

			// Build the customers.
			Thread[] customers = new Thread[numCustomers];
			LinkedList<Food> order;
			if (!randomOrders) {
				order = new LinkedList<Food>();
				order.add(FoodType.fries);
				order.add(FoodType.pizza);
				order.add(FoodType.subs);
				order.add(FoodType.soda);
				for (int i = 0; i < customers.length; i++) {
					customers[i] = new Thread(new Customer("Customer " + (i), order));
				}
			} else {
				for (int i = 0; i < customers.length; i++) {
					Random rnd = new Random();
					int friesCount = rnd.nextInt(4);
					int pizzaCount = rnd.nextInt(4);
					int subCount = rnd.nextInt(4);
					int sodaCount = rnd.nextInt(4);
					order = new LinkedList<Food>();
					for (int b = 0; b < friesCount; b++) {
						order.add(FoodType.fries);
					}
					for (int f = 0; f < pizzaCount; f++) {
						order.add(FoodType.pizza);
					}
					for (int f = 0; f < subCount; f++) {
						order.add(FoodType.subs);
					}
					for (int c = 0; c < sodaCount; c++) {
						order.add(FoodType.soda);
					}
					customers[i] = new Thread(
						new Customer("Customer " + (i), order));
				}
			}
			for (int i = 0; i < numCooks; i++) {
				cookThreads[i] = new Thread(new Cook("Cook " + i, machines));
			}

			/*
			 * Now "let the customers know the shop is open" by starting them running in
			 * their own thread.
			 */
			
			for (int i = 0; i < cookThreads.length; i++) {
				cookThreads[i].start();
				/*
				 * NOTE: Starting the customer does NOT mean they get to go right into the shop.
				 * There has to be a table for them. The Customer class' run method has many
				 * jobs to do - one of these is waiting for an available table...
				 */
			}
			
			for (int i = 0; i < customers.length; i++) {
				System.out.println("RUNNING");
				customers[i].start();
				/*
				 * NOTE: Starting the customer does NOT mean they get to go right into the shop.
				 * There has to be a table for them. The Customer class' run method has many
				 * jobs to do - one of these is waiting for an available table...
				 */
			}

		
			try {
				/*
				 * Wait for customers to finish
				 * -- you need to add some code here...
				 */

				for (int i = 0; i < customers.length; i++) {
					customers[i].join();
				}
				System.out.println("DONE JOINING");
	
				for (int i = 0; i < cookThreads.length; i++)
					cookThreads[i].interrupt();
				for (int i = 0; i < cookThreads.length; i++)
					cookThreads[i].join();

			} catch (InterruptedException e) {
				System.out.println("Simulation thread interrupted.");
			}
			Simulation.logEvent(SimulationEvent.machinesEnding(machines.get(0)));
			machines.remove(0);
			Simulation.logEvent(SimulationEvent.machinesEnding(machines.get(0)));
			machines.remove(0);
			Simulation.logEvent(SimulationEvent.machinesEnding(machines.get(0)));
			machines.remove(0);
			Simulation.logEvent(SimulationEvent.machinesEnding(machines.get(0)));
			machines.remove(0);
		
			
	}
	
	boolean placeOrder(Customer cust, List<Food> order, int orderNumber) {
		if (cust == null || order == null || currentOrders == null || ordersNum == null ) {
			return false;
		}
		synchronized(ordersNum) {
			ordersNum.put(orderNumber, order);
		}
		synchronized(locks) {
			locks.put(orderNumber, new Object());
		}
		synchronized(currentOrders) {
			currentOrders.add(orderNumber);
			synchronized(this) {
				this.notifyAll();
			}
			currentOrders.notifyAll();
		}
		return true;
	}
	void enterRat(Customer cust) {
		synchronized(tables) {
			while (numTables <= tables.size()) {
				try {
					tables.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			tables.add(cust);
		}
	}
	void leaveRat(Customer cust) {
		synchronized(tables) {
			tables.remove(cust);
			tables.notifyAll();
		}
	}
	Object getLock(int orderNumber) {
		synchronized (locks) {
			return locks.get(orderNumber);
		}
	}
	void addToCooks(Cook cook, int orderNum) {
		synchronized(getLock(orderNum)) {
			synchronized(cooksOrders) {
				cooksOrders.add(orderNum);
				getLock(orderNum).notifyAll();
			}
		}
	}
	void addToFinished(Cook cook, int orderNum) {
		synchronized(getLock(orderNum)) {
			synchronized(cooksOrders) {
				cooksOrders.remove(orderNum);
				synchronized (orders) {
					orders.add(orderNum);
				}
			}
		}
	}
	boolean activeCustOrders() {
		synchronized(currentOrders) {
			return !(currentOrders.isEmpty());
		}
	}
	//TODO: check this
	private boolean areAllOrdersFinished() {
				synchronized (orders) {
			return orders.size() == numCustomers;
		}
	}
	Integer getCustomerOrder() {
		synchronized(currentOrders) {
			while (currentOrders.isEmpty() && !areAllOrdersFinished()) {
				try {
					currentOrders.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (areAllOrdersFinished()) {
					return null;
				}
			}
			Iterator<Integer> it = currentOrders.iterator();
			Integer orderNumber = -1;
			while (it.hasNext()) {
				orderNumber = it.next();
				break;
			}
			currentOrders.remove(orderNumber);
			return orderNumber;
		}
		}
	boolean isCooking(int orderNum) {
		synchronized(getLock(orderNum)) {
			synchronized(orders) {
				return (orders.contains(orderNum) == false);
			}
		}
	}
	List<Food> getOrder(int orderNum){
		synchronized( ordersNum) {
			return ordersNum.get(orderNum);
		}
	}
	HashSet<Integer> getCurrentOrders(){
		synchronized(currentOrders){
		return currentOrders;	
		}
	}
	

}
