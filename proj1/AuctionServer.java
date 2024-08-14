package cmsc433.p1;

/**
 *  @author Michael Woods
 */

/*final final final final submission*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AuctionServer
{
	/**
	 * Singleton: the following code makes the server a Singleton. You should
	 * not edit the code in the following noted section.
	 *
	 * For test purposes, we made the constructor protected.
	 */

	/* Singleton: Begin code that you SHOULD NOT CHANGE! */
	protected AuctionServer()
	{
	}

	private static AuctionServer instance = new AuctionServer();

	public static AuctionServer getInstance()
	{
		return instance;
	}

	/* Singleton: End code that you SHOULD NOT CHANGE! */





	/* Statistic variables and server constants: Begin code you should likely leave alone. */


	/**
	 * Server statistic variables and access methods:
	 */
	private int soldItemsCount = 0;
	private int revenue = 0;
	private int uncollectedRevenue = 0;
	public int blacklistCash = 0;
	public int extraCash = 0;
	public int soldItemsCount() {
		synchronized (instanceLock) {
			return this.soldItemsCount;
		}
	}

	public int revenue()
	{
		synchronized (instanceLock) {
			return this.revenue;
		}
	}

	public int uncollectedRevenue () {
		synchronized (instanceLock) {
			return this.uncollectedRevenue;
		}
	}



	/**
	 * Server restriction constants:
	 */
	public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
	public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given time.
	public static final int serverCapacity = 80; // The maximum number of active items at a given time.

	/* Statistic variables and server constants: End code you should likely leave alone. */



	/**
	 * Some variables we think will be of potential use as you implement the server...
	 */

	// List of items currently up for bidding (will eventually remove things that have expired).
	private List<Item> itemsUpForBidding = new ArrayList<Item>();


	// The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
	private int lastListingID = -1;

	// List of item IDs and actual items.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

	// List of itemIDs and the highest bid for each item.  This is a running list with everything ever bid upon.
	private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

	// List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
	private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>();

	// List of Bidders who have been permanently banned because they failed to pay the amount they promised for an item.
	private HashSet<String> blacklist = new HashSet<String>();
	private HashSet<String> whiteList = new HashSet<String>();
	// List of sellers and how many items they have currently up for bidding.
	private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

	// List of buyers and how many items on which they are currently bidding.
	private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();

	// List of itemIDs that have been paid for. This is a running list including everything ever paid for.
	private HashSet<Integer> itemsSold = new HashSet<Integer> ();

	// Object used for instance synchronization if you need to do it at some point
	// since as a good practice we don't use synchronized (this) if we are doing internal
	// synchronization.
	//
	private Object instanceLock = new Object();









	/*
	 *  The code from this point forward can and should be changed to correctly and safely
	 *  implement the methods as needed to create a working multi-threaded server for the
	 *  system.  If you need to add Object instances here to use for locking, place a comment
	 *  with them saying what they represent.  Note that if they just represent one structure
	 *  then you should probably be using that structure's intrinsic lock.
	 */


	/**
	 * Attempt to submit an <code>Item</code> to the auction
	 * @param sellerName Name of the <code>Seller</code>
	 * @param itemName Name of the <code>Item</code>
	 * @param lowestBiddingPrice Opening price
	 * @param biddingDurationMs Bidding duration in milliseconds
	 * @return A positive, unique listing ID if the <code>Item</code> listed successfully, otherwise -1
	 */

	public int submitItem(String sellerName, String itemName, int lowestBiddingPrice, int biddingDurationMs)
	{   	 //System.out.println(sellerName);

		//System.out.println("SUBMITTTING ITEM");

		/*
		 * TODO: make sure no 2 items have the same listing ID as another item
		 * if seller has already used up quota, return -1
		 * if server has reached maxServercapacity, return -1
		 */

		//System.out.println("ENTEREDING SUBMIT ITEM");
		synchronized(instanceLock) {
			//System.out.println("---------------------"+itemsUpForBidding + "-----------------");
			if (itemsUpForBidding.size() < serverCapacity && (itemsPerSeller.containsKey(sellerName) == false ||
					itemsPerSeller.get(sellerName) < maxSellerItems)) {

				lastListingID++;
				Item newItem = new Item(sellerName, itemName, lastListingID,  lowestBiddingPrice, biddingDurationMs);



				itemsUpForBidding.add(newItem);
				highestBids.put(lastListingID, lowestBiddingPrice);
				itemsAndIDs.put(lastListingID, newItem);

				if (itemsPerSeller.get(sellerName) == null) {
					itemsPerSeller.put(sellerName, 1);
				}
				else {
					itemsPerSeller.put(sellerName, itemsPerSeller.get(sellerName) + 1);
				}
				//System.out.println("**************************" + itemsUpForBidding + "*****************************");

				return lastListingID;
			}

			return -1;

		}
	}




	/**
	 * Get all <code>Items</code> active in the auction
	 * @return A copy of the <code>List</code> of <code>Items</code>
	 */
	public List<Item> getItems()
	{    
		synchronized(instanceLock) {
			List<Item> check = new ArrayList<Item>();
			for (Item item : itemsUpForBidding) {
				if (item.biddingOpen() && itemsSold.contains(item.listingID()) == false) {
					check.add(item);
				}
			}

			return check;
		}
	}

	public List<String> getBlacklist(){
		synchronized(instanceLock) {
			List<String> check = new ArrayList<String>();
			for (String item : blacklist) {
				check.add(item);
			}

			return check;
		}
	}

	/**
	 * Attempt to submit a bid for an <code>Item</code>
	 * @param bidderName Name of the <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @param biddingAmount Total amount to bid
	 * @return True if successfully bid, false otherwise
	 */



	public boolean submitBid(int listingID, String bidderName, int biddingAmount) {
		//System.out.println("-------------------SUBMIT BID----------------");
		//System.out.println("biddername" + bidderName);

		synchronized(instanceLock) {
			//System.out.println(bidderName);
			Item item = itemsAndIDs.get(listingID);

			//System.out.println(item);

			System.out.println("ZERO" + item != null && item .biddingOpen() && (itemsUpForBidding.contains(item)));
			System.out.println("FIRST" + (itemsPerBuyer.containsKey(bidderName) == false
			    || itemsPerBuyer.get(bidderName) < maxBidCount));
			System.out.println("2" +  (!blacklist.contains(bidderName) && (highestBidders.get(listingID) != bidderName)));
			System.out.println("3" + (highestBidders.containsKey(listingID) == false || highestBidders.get(listingID) != bidderName));
			System.out.println("4" + (highestBids.containsKey(listingID) && highestBids.get(listingID) <= biddingAmount));
			
			
			
			if (item != null && item .biddingOpen() && (itemsUpForBidding.contains(item))
					&& (itemsPerBuyer.containsKey(bidderName) == false
					|| itemsPerBuyer.get(bidderName) < maxBidCount) &&     
					!blacklist.contains(bidderName) && 
					(highestBidders.containsKey(listingID) == false || highestBidders.get(listingID) != bidderName)
					&& (highestBids.containsKey(listingID) && highestBids.get(listingID) <= biddingAmount)) {
		
				int lowestPrice = item.lowestBiddingPrice();
				String prevBidder = highestBidders.get(listingID);
				/*No one cast a bid*/
		   		
				
				if (prevBidder == null && lowestPrice == item.lowestBiddingPrice()) {
		   			 
		   			 if (biddingAmount >= lowestPrice) {
		   				 highestBids.put(listingID, biddingAmount);
		   				 highestBidders.put(listingID, bidderName);
		   			 }
		   			 if (itemsPerBuyer.get(bidderName) != null) {
		   				 itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) + 1);
		   			 }
		   			 else {
		   				 itemsPerBuyer.put(bidderName, 1);
		   			 }
		   			 
		   		 }
		   		 /*someone cast a bid*/
		   		 else {
		   			 
		   			
		   			 if (prevBidder != null && lowestPrice < biddingAmount) {
		   				 highestBids.put(listingID, biddingAmount );
		   				 highestBidders.put(listingID, bidderName);
		   				 if (itemsPerBuyer.get(bidderName) != null) {
		   					 itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) + 1);
		   				 }
		   				 else {
		   					 itemsPerBuyer.put(bidderName, 1);
		   				 }
		   				 
		   				 /*START OF PREVBIDDER STUFF*/
		   				 
		   				 if (itemsPerBuyer.get(prevBidder) != null && itemsPerBuyer.get(prevBidder) > 1) {
		   					 itemsPerBuyer.put(prevBidder, itemsPerBuyer.get(prevBidder) - 1);
		   					 
		   				 }
		   				 else if (itemsPerBuyer.get(prevBidder) == 1) {
		   					 itemsPerBuyer.remove(prevBidder);
		   					 
		   				 }
		   				 else {
		   					 return false;
		   				 }
		   				
		   			 }
		   			 else {
		   			 return false;
		   			 }
		   			 }
		   		 return true;
		   		 }
		   		 return false;

		}
	}
			

	/**
	 * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
	 * @param bidderName Name of <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return 0 (success) if bid is over and this <code>Bidder</code> has won<br>
	 * 1 (open) if this <code>Item</code> is still up for auction<br>
	 * 2 (failed) If this <code>Bidder</code> did not win or the <code>Item</code> does not exist
	 */
	public int checkBidStatus(int listingID, String bidderName)
	{
		//System.out.println("Checking bid status");
		final int SUCCESS = 0, OPEN = 1, FAILURE = 2;
		synchronized(instanceLock) {    
			Item item = itemsAndIDs.get(listingID);
			
	
			if (item != null && highestBidders.get(listingID) != null) {
				String bidCheck = highestBidders.get(listingID);
				if (bidCheck.equals( bidderName)  && !(itemsAndIDs.get(listingID).biddingOpen()))  {

					String sellerName = itemsAndIDs.get(listingID).seller();
					int numItems = itemsPerSeller.get(sellerName);
					if (numItems == 1) {
						itemsPerSeller.remove(sellerName);
					}
					else {
						itemsPerSeller.put(sellerName, numItems- 1 );
					}
					uncollectedRevenue += highestBids.get(listingID);
					itemsUpForBidding.remove(item);
					return SUCCESS;
				}
				/*If the bidcheck is not equal to the name, we will return failure*/
				else if (!bidCheck.equals(bidderName)) {
					return FAILURE;
				}
				/*if the bidhcekc is open*/
				else if (bidCheck.equals(bidderName)&& itemsAndIDs.get(listingID).biddingOpen()) {

					return OPEN;
				}
				else
					return FAILURE;
			}
			return FAILURE;    
		}
	}

	/**
	 * Check the current bid for an <code>Item</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return The highest bid so far or the opening price if there is no bid on the <code>Item</code>,
	 * or -1 if no <code>Item</code> with the given listingID exists
	 */
	public int itemPrice(int listingID)
	{
		synchronized(instanceLock) {
			if( itemsAndIDs.get(listingID) != null) {
				//System.out.println("ENTERED item Price");
				return highestBids.get(listingID);
			}
			else
				//System.out.println("ENTERED item Price");
				return -1;
		}


	}

	/**
	 * Check whether an <code>Item</code> has a bid on it
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return True if there is no bid or the <code>Item</code> does not exist, false otherwise
	 */
	public boolean itemUnbid(int listingID)
	{
		synchronized(instanceLock) {
			if (itemsAndIDs.get(listingID)== null && !highestBidders.containsKey(listingID)) {
				return true;
			}
			else {
				return false;
			}
		}

	}

	/**
	 * Pay for an <code>Item</code> that has already been won.
	 * @param bidderName Name of <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @param amount The amount the <code>Bidder</code> is paying for the item
	 * @return The name of the <code>Item</code> won, or null if the <code>Item</code> was not won by the <code>Bidder</code> or if the <code>Item</code> did not exist
	 * @throws InsufficientFundsException If the <code>Bidder</code> did not pay at least the final selling price for the <code>Item</code>
	 */
	public String payForItem (int listingID, String bidderName, int amount) throws InsufficientFundsException {
		synchronized(instanceLock) {
			//System.out.println("---------------------PAY FOR ITEM-----------------------");

			Item item = itemsAndIDs.get(listingID);


			if (item != null && !itemsUpForBidding.contains(item) && !itemsSold.contains(listingID) &&
					(highestBids.get(item.listingID()) != item.lowestBiddingPrice()) && highestBidders.get(listingID).equals(bidderName)) {
				if (amount >= highestBids.get(listingID)) {

					whiteList.add(bidderName);
					revenue+= amount;
					uncollectedRevenue -= amount;
					soldItemsCount++;
					itemsSold.add(listingID);	
					if (itemsPerBuyer.get(bidderName) != 1) {
					itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) - 1);
					}
					else {
						itemsPerBuyer.remove(bidderName);
					}
					return itemsAndIDs.get(listingID).name();
				}
				/*TODO: need to go over this*/
				/*CLOSED*/   							 
				else if (!itemsUpForBidding.contains(item) && highestBids.get(item.listingID()) != item.lowestBiddingPrice() && amount < highestBids.get(item.listingID())) {
					//System.out.println("ENTTTTTTTTTTTTTTTTTTTTTTTTERED");
					blacklist.add(bidderName);

					blacklistCash += amount+1;
				
					for (int id : highestBidders.keySet())  {
						if (highestBidders.get(id).equals(bidderName)){
							//reset to lowest bidding price
							highestBids.put(id, itemsAndIDs.get(id).lowestBiddingPrice());
							//gt rid of highestBidder
							highestBidders.remove(id);
						}
						itemsPerBuyer.remove(bidderName);

						throw new InsufficientFundsException();


					}
				}


			}//end of method
			return null;
		} // end of synchro

	}//end of class
}
