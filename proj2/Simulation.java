/*DEBUGGED VERSION*/
package cmsc433;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


/**
 * Simulation is the main class used to run the simulation. You may
 * add any fields (static or instance) or any methods you wish.
 */
public class Simulation {
	// List to track simulation events during simulation
	public static List<SimulationEvent> events;
	
	
	/**
	 * Used by other classes in the simulation to log events
	 * 
	 * @param event
	 */
	public static void logEvent(SimulationEvent event) {
		events.add(event);
		System.out.println(event);
	}



	/**
	 * Function responsible for performing the simulation. Returns a List of
	 * SimulationEvent objects, constructed any way you see fit. This List will
	 * be validated by a call to Validate.validateSimulation. This method is
	 * called from Simulation.main(). We should be able to test your code by
	 * only calling runSimulation.
	 * 
	 * Parameters:
	 * 
	 * @param numCustomers the number of customers wanting to enter the restaurant
	 * @param numCooks the number of cooks in the simulation
	 * @param numTables the number of tables in the restaurant
	 * @param machineCount the count of all machine types in the restaurant
	 * @param randomOrders a flag say whether or not to give each customer a random
	 *        order
	 */
	public static List<SimulationEvent> runSimulation(
		int numCustomers, int numCooks,
		int numTables, int machineCount,
		boolean randomOrders) {

		// This method's signature MUST NOT CHANGE.

		/*
		 * We are providing this events list object for you.
		 * It is the ONLY PLACE where a concurrent collection object is
		 * allowed to be used.
		 */

		events = Collections.synchronizedList(new ArrayList<SimulationEvent>());

		// Start the simulation

		
		logEvent(SimulationEvent.startSimulation(numCustomers,
			numCooks,
			numTables,
			machineCount));

		Rat resturaunt = new Rat(numCustomers,
			numCooks,
			numTables,
			machineCount, randomOrders);
		// Shut down machines
	

		logEvent(SimulationEvent.endSimulation());

		return events;
	}

	/**
	 * Entry point for the simulation.
	 *
	 * @param args the command-line arguments for the simulation. There
	 *        should be exactly four arguments: the first is the number of
	 *        customers, the second is the number of cooks, the third is the number
	 *        of tables in the restaurant, and the fourth is the number of items
	 *        each set of machines can make at the same time.
	 */
	public static void main(String args[]) throws InterruptedException {
		// Command line argument parser

// @formatter:off
/*
 * 		if (args.length != 4) {
 * 			System.err.println("usage: java Simulation <#customers> <#cooks> <#tables> <count> <randomorders");
 * 			System.exit(1);
 * 		}
 * 		int numCustomers = new Integer(args[0]).intValue();
 * 		int numCooks = new Integer(args[1]).intValue();
 * 		int numTables = new Integer(args[2]).intValue();
 * 		int machineCount = new Integer(args[3]).intValue();
 * 		boolean randomOrders = new Boolean(args[4]);
 */
// @formatter: on
		
		// Parameters to the simulation
		int numCustomers = 100;
		int numCooks = 5;
		int numTables = 5;
		int machineCount = 1;
		boolean randomOrders = false;

		/* Run the simulation and then feed the result into the method to validate simulation. */
		System.out.println("Did it work? " +
			Validate.validateSimulation(
				runSimulation(
					numCustomers, numCooks,
					numTables, machineCount,
					randomOrders)));
		
	}
}





/* (._.)*/
