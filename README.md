# SokoBot (Java)

## Overview
SokoBot is an automated solver for the classic Japanese puzzle game **Sokoban** (“warehouse keeper”). Built in Java, the application models the game as a **state-space search problem** and uses an **informed search agent** to navigate a grid-based warehouse.

The objective is to compute the most efficient sequence of moves that pushes all crates onto their designated target squares while avoiding immovable **deadlock states**.

## Features

- **Informed Search Implementation**  
  Uses an informed search algorithm to determine the optimal solution path with the lowest cumulative cost.

- **Deadlock Detection**  
  Prunes the search space by detecting irreversible crate positions (e.g., corners or wall locks without targets).

- **State-Space Management**  
  Efficient representation of unique game states using player and crate coordinates.

- **Heuristic Evaluation**  
  Estimates remaining cost by calculating distances between crates and target locations to prioritize promising paths.

- **Automated Pathfinding**  
  Outputs a sequence of moves (`u`, `d`, `l`, `r`) that can be executed to solve a given Sokoban map.

- **Visited State Tracking**  
  Uses hashing to prevent revisiting previously explored configurations, avoiding infinite loops and redundant computation.

## Technical Specifications

- **Language**: Java  
- **Algorithm**: A* Search Algorithm  
- **Data Structures**:
  - Priority Queues (for F-value ordering)
  - HashSets (for visited state tracking)
  - 2D Arrays (for map representation)
- **Environment**: Compatible with standard Java Runtime Environments (JRE)

## Project Context
This project was developed as **Major Course Output 1 (MCO1)** for the course  
**CSINTSY - Introduction to Intelligent Systems** at De La Salle University. 

The project adheres to strict academic requirements for state representation, successor generation, and heuristic correctness.

## Design Highlights

- **State Object Design**  
  Encapsulates player position, crate positions, and the path taken to reach the current state.

- **Heuristic Optimization**  
  Implements a custom heuristic function that computes the minimum distance from crates to goal tiles, significantly reducing node expansion.

- **Successor Function**  
  Determines valid moves by evaluating walls, crate positions, and empty spaces in the grid.

- **Deadlock Logic**  
  The `isDeadEnd` method prevents exploration of states where crates are pushed into non-goal corners or against walls, ensuring solver efficiency.

## Code Scope Disclaimer
This repository contains **only my original implementation** of the
`SokoBot` solver logic.

The remaining game engine, map parser, and execution framework were
provided as part of an academic assignment and are excluded to respect
course policies.

