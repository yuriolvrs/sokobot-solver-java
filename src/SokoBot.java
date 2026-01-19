/*
 * SokoBot.java
 *
 * This file contains my implementation of the Sokoban-solving algorithm.
 * The surrounding game engine and puzzle framework were provided
 * as part of an academic assignment and are not included in this repository.
 */

package solver;

import java.util.*;

public class SokoBot {
  private static final char WALL = '#';
  private static final char PLAYER = '@';
  private static final char CRATE = '$';
  private static final char TARGET = '.';

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    // Initialize the default solution
    String solution = "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
    
    // Find target positions on the map
    Set<Point> targetPositions = findTargets(mapData);
    
    // Initialize the starting state of the level
    State startState = initializeStartState(itemsData, targetPositions);
    
    // Create a set to keep track of visited states
    HashSet<String> visitedStates = new HashSet<String>();

    // Create a priority queue of states to explore by their F Value
    PriorityQueue<State> queue = new PriorityQueue<>(Comparator.comparingInt(s -> s.getFValue()));
    queue.add(startState);

    // Explore states until the queue is empty 
    while (!queue.isEmpty()) {
        // Pop the current state
        State currentState = queue.poll();
        
        // Check if the current state is a goal state
        if (isGoalState(currentState, targetPositions)) {
          // Construct the solution and exit the loop
          solution = constructSolution(currentState);
          break;
        }

        // Find the valid successor states of the current states
        List<State> successorStates = findSuccessorStates(currentState, mapData, itemsData, targetPositions);

        // Check if successor states have been visited. If not, add them to the queue
        for (State successor : successorStates) {
          if (!visitedStates.contains(successor.toString())) {
            visitedStates.add(successor.toString());
            queue.add(successor);
          }
        }
      }

    return solution;
  }

  // Find the positions of the target locations on the map
  private Set<Point> findTargets(char[][] mapData) {
    Set<Point> targetPositions = new HashSet<>();

    for (int y = 0; y < mapData.length; y++) {
      for (int x = 0; x < mapData[0].length; x++) {
        char mapChar = mapData[y][x];

        if (mapChar == TARGET) {
          targetPositions.add(new Point(x, y));
        }
      }
    }

    return targetPositions;
  }

  // Initialize the starting state of the level
  private State initializeStartState(char[][] itemsData, Set<Point> targetPositions) {
    Set<Point> cratePositions = new HashSet<>();
    Point playerPosition = null;
    Set<Point> hitGoals = new HashSet<>();

    for (int y = 0; y < itemsData.length; y++) {
      for (int x = 0; x < itemsData[0].length; x++) {
        char itemsChar = itemsData[y][x];

        if (itemsChar == PLAYER) {
          playerPosition = new Point(x, y);
        }

        if (itemsChar == CRATE) {
          cratePositions.add(new Point(x, y));

          if (targetPositions.contains(new Point(x, y))) {
            hitGoals.add(new Point(x, y));
          }
        }
      }
    }

    int heuristic = calculateHeuristic(playerPosition.x, playerPosition.y, cratePositions, targetPositions);

    return new State(playerPosition.x, playerPosition.y, cratePositions, 0, heuristic, null, hitGoals);
  }

  // Calculate the heuristic value based on the total Manhattan distance from each crate to its nearest target
  // and the minimum distance from the player to the nearest crate
  private int calculateHeuristic(int playerX, int playerY, Set<Point> cratePositions, Set<Point> targetPositions) {
    int totalDistance = 0;

    // Calculate the total Manhattan distance from each crate to its nearest target.
    for (Point crate : cratePositions) {
        int minDistance = Integer.MAX_VALUE;
        for (Point target : targetPositions) {
            int distance = Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y);
            minDistance = Math.min(minDistance, distance);
        }
        totalDistance += minDistance;
    }

    // Calculate the minimum distance from the player to any crate.
    int playerToCrateDistance = Integer.MAX_VALUE;
    for (Point crate : cratePositions) {
        int distance = Math.abs(playerX - crate.x) + Math.abs(playerY - crate.y);
        playerToCrateDistance = Math.min(playerToCrateDistance, distance);
    }

    return totalDistance + playerToCrateDistance;
  }

  // Check if the given state is a goal state where all crates are in their target positions.
  private boolean isGoalState(State state, Set<Point> targetPositions) {
    for (Point crate : state.cratePositions) {
      if (!targetPositions.contains(crate)) {
        return false;
      }
    }
    return true;
  }

  // Construct a solution string by backtracking from the goal state to the starting state
  private String constructSolution(State goalState) {
    List<Character> moves = new ArrayList<>();

    State currentState = goalState;

    // Backtrack from the goal state to the initial state to record the sequence of moves
    while (currentState.parent != null) {
      char move = checkLastMove(currentState.parent, currentState);
      moves.add(move);

      currentState = currentState.parent;
    }

    // Reverse the list of moves to get them in the correct order
    Collections.reverse(moves);

    // Convert the list of moves to a string
    StringBuilder solution = new StringBuilder();
    for (char move : moves) {
      solution.append(move);
    }
    return solution.toString();
  }

  // Determines the direction of the move made from the parent state to the current state
  private char checkLastMove(State parentState, State currentState) {
    int diffX = currentState.playerX - parentState.playerX;
    int diffY = currentState.playerY - parentState.playerY;

    if (diffX == 1) {
      return 'r';
    } else if (diffX == -1) {
      return 'l';
    } else if (diffY == 1) {
      return 'd';
    } else if (diffY == -1) {
      return 'u';
    }

    return ' ';
  }

  // Find valid successor states for the given current state by exploring possible moves
  private List<State> findSuccessorStates(State currentState, char[][] mapData, char[][] itemsData, Set<Point> targetPositions) {
    List<State> successorStates = new ArrayList<>();
    int[] dx = {1, -1, 0, 0};
    int[] dy = {0, 0, 1, -1};

    // Loop through each direction
    for (int direction = 0; direction < 4; direction++) {
      int newPlayerX = currentState.playerX + dx[direction];
      int newPlayerY = currentState.playerY + dy[direction];

      // Check if the move is valid
      if (!isValidMove(mapData, newPlayerX, newPlayerY)) {
        continue;
      }

      int newCost = currentState.gValue + 1;

      // If the new player position contains a crate
      if (currentState.cratePositions.contains(new Point(newPlayerX, newPlayerY))) {
        int newCrateX = newPlayerX + dx[direction];
        int newCrateY = newPlayerY + dy[direction];

        // If the crate can be moved to the new position
        if (!currentState.cratePositions.contains(new Point(newCrateX, newCrateY))) {
          if (isValidMove(mapData, newCrateX, newCrateY)) {
            // Create a new set of crate positions after moving the crate
            Set<Point> newCratePositions = new HashSet<>(currentState.cratePositions);
            newCratePositions.remove(new Point(newPlayerX, newPlayerY));
            newCratePositions.add(new Point(newCrateX, newCrateY));

            // Check if the new position is not a dead end
            if (!isDeadEnd(newCrateX, newCrateY, mapData, newCratePositions)) {
              Set<Point> newHitGoals = new HashSet<>(currentState.hitGoals);

              // If the new crate position is a target, add it to hitGoals
              if (targetPositions.contains(new Point(newCrateX, newCrateY))) {
                newHitGoals.add(new Point(newCrateX, newCrateY));
              }

              // If the new player position is a target, remove it from hitGoals
              if (targetPositions.contains(new Point(newPlayerX, newPlayerY))) {
                newHitGoals.remove(new Point(newPlayerX, newPlayerY));
              }

              int newHeuristic = calculateHeuristic(newCrateX, newCrateY, newCratePositions, targetPositions);
              
              // Create a new state and add it to successorStates
              State newState = new State(newPlayerX, newPlayerY, newCratePositions, newCost, newHeuristic, currentState, newHitGoals);
              successorStates.add(newState);
            }
          }
        }
      } else {
        int newHeuristic = calculateHeuristic(newPlayerX, newPlayerY, currentState.cratePositions, targetPositions);
        
        // Create a new state and add it to successorStates
        State newState = new State(newPlayerX, newPlayerY, currentState.cratePositions, newCost, newHeuristic, currentState, currentState.hitGoals);
        successorStates.add(newState);
      }
    }
    return successorStates;
  }

  // Check if a move is valid based on map boundaries and walls
  private boolean isValidMove(char[][] mapData, int x, int y) {
    return x >= 0 && x < mapData[0].length && y >= 0 && y < mapData.length && mapData[y][x] != WALL;
  }

  // Check if a position is a dead end
  private boolean isDeadEnd(int crateX, int crateY, char[][] mapData, Set<Point> cratePositions) {
    int width = mapData[0].length;
    int height = mapData.length;

    if (mapData[crateY][crateX] == TARGET)
        return false;

    boolean hasOpenPath = false;

    // Check adjacent positions for open paths
    for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
            int x = crateX + dx;
            int y = crateY + dy;

            if (x >= 0 && x < width && y >= 0 && y < height) {
                char mapChar = mapData[y][x];

                if (mapChar == WALL || cratePositions.contains(new Point(x, y))) {
                    continue;
                } else {
                    // If an open position is found, set hasOpenPath to true and continue searching
                    hasOpenPath = true;
                }
            }
        }
    }

    // If any open positions were found, it's not a dead end; return false.
    if (hasOpenPath) {
        return false;
    } else {
        // If all adjacent positions are blocked, it's a dead end; return true.
        return true;
    }
  }
}
