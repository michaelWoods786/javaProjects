package cmsc433.p3;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;

/**
 * This file needs to hold your solver to be tested. 
 * You can alter the class to extend any class that extends MazeSolver.
 * It must have a constructor that takes in a Maze.
 * It must have a solve() method that returns the datatype List<Direction>
 *   which will either be a reference to a list of steps to take or will
 *   be null if the maze cannot be solved.
 */
public class StudentMTMazeSolver extends SkippingMazeSolver
{
	public StudentMTMazeSolver(Maze maze)
	{
		super(maze);
	}
	private int numProcessors = Runtime.getRuntime().availableProcessors();
	private ExecutorService newFixedThreadPool;
	private CompletionService<List<Direction>>  service;
	public List<Direction> solve(){



		this.newFixedThreadPool = Executors.newFixedThreadPool(numProcessors);
		this.service = new ExecutorCompletionService<List<Direction>>(newFixedThreadPool);
		List<Callable<List<Direction>>> lst = new ArrayList<Callable<List<Direction>>>();
		Choice start;
		try{
			//returns list of available directions
			start = firstChoice(maze.getStart());

			//if the move size = 1, then we just move directly there
			//otherwise, it will return a new Choice, with Postion rom = pos, choices(Deque
			//directions) null, and from = null


			for (Direction d : start.choices){
				//Callable is a task that returns a a result and may throw an exception	


				lst.add(new  DepthFirst(follow(start.at,d),d ));

			}

			//when executor calls submit, it automatically will look for the call method
		}catch(SolutionFound e){
			System.out.println("a solution has been found");
		}

		List<Direction> path = null;

		for (Callable<List<Direction>> c: lst){
			this.service.submit(c);
		}
		try {
			for (int i = 0; i < lst.size(); i++) {

				Future<List<Direction>> f = this.service.take();
				List<Direction> check = f.get();
				if (check != null) {
					path = check;
					break;
				}
			} 

		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ExecutionException e) {
			e.printStackTrace();
		}
		

		newFixedThreadPool.shutdown();
		return path;
	}


	private class DepthFirst implements Callable <List<Direction>>{

		private Choice choiceStart;
		private Direction startDir;
		public DepthFirst(Choice ch, Direction dir){
			this.choiceStart = ch;
			this.startDir = dir;
		}


		public List<Direction> call(){
			LinkedList<Choice> choiceStack = new LinkedList<Choice>();
			Choice ch = null; 

			try 
			{   
				choiceStack.push(this.choiceStart);
				while (!choiceStack.isEmpty())
				{   
					ch = choiceStack.peek();
					if (ch.isDeadend())
					{   
						// backtrack.
						choiceStack.pop();
						if (!choiceStack.isEmpty()) choiceStack.peek().choices.pop();
						continue;
					}   
					choiceStack.push(follow(ch.at, ch.choices.peek()));
				}   
				// No solution found.
				return null;
			}   
			catch (SolutionFound e)
			{   
				Iterator<Choice> iter = choiceStack.iterator();
				LinkedList<Direction> solutionPath = new LinkedList<Direction>();
				while (iter.hasNext())
				{   
					ch = iter.next();
					solutionPath.push(ch.choices.peek());
				}   
				solutionPath.push(this.startDir);

				if (maze.display != null) maze.display.updateDisplay();
				return pathToFullPath(solutionPath);
			}   
		}   

	}
}






