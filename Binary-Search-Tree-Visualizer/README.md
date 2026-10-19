# Binary Search Tree (BST) Visualizer — Java AWT

A Java-based visualization of Binary Search Tree (BST) operations using AWT. This project demonstrates how nodes are inserted, searched, and deleted while the tree structure updates dynamically.

## Overview

A Binary Search Tree is a tree-based data structure where values smaller than a node are stored in its left subtree, and values greater than a node are stored in its right subtree.

This project provides a graphical way to understand BST operations and follow how the tree changes during execution.

## Features

- **Insertion:** Insert values and visualize the updated tree.
- **Searching:** Search for a value and follow the search process.
- **Deletion:** Delete a node and visualize the resulting tree.
- **Dynamic Visualization:** Display the tree structure graphically.
- **Operation Feedback:** Show status messages during operations.
- **Java AWT Interface:** Use a graphical interface to explore BST behavior.

## Technologies Used

- Java
- Abstract Window Toolkit (AWT)
- Binary Search Tree
- Data Structures and Algorithms

## Time Complexity

For a typical, unbalanced Binary Search Tree:

| Operation | Average Case | Worst Case |
|---|---|---|
| Insertion | O(log n) | O(n) |
| Searching | O(log n) | O(n) |
| Deletion | O(log n) | O(n) |

Here, `n` represents the number of nodes in the tree.

The worst case occurs when the tree becomes highly skewed. These are the theoretical complexities of BST operations, not necessarily measurements displayed by the visualizer.

## How to Run

1. Install the Java Development Kit (JDK).
2. Clone or download this repository.
3. Open the Java source file in your preferred IDE.
4. Compile and run the program using the actual source filename and class name.

Example, if the filename and class name are both `BSTVisualizer`:

```bash
javac BSTVisualizer.java
java BSTVisualizer
```

## Learning Objectives

- Understand the structure and properties of a Binary Search Tree.
- Visualize insertion, searching, and deletion.
- Explore how tree shape affects operation efficiency.
- Practice Java GUI development using AWT.

## Project Status

A learning project focused on visualizing fundamental Binary Search Tree operations in Java.

