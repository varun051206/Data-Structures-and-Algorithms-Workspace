import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
//25,20,36,10,22,30,40,5,12,28,38,48,5,12,28,38,48,1,8,15,45,40
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.swing.Timer;

public class insertion extends Frame {
    private static final int NODE_DIAMETER = 42;
    private static final int LEVEL_GAP = 76;
    private static final int ANIMATION_DELAY = 400;

    private final TreeCanvas treeCanvas = new TreeCanvas();
    private final TextField valueField = new TextField(12);
    private final Label statusLabel = new Label("Enter one or more values, then click Insert to build the tree.");
    private final Button insertButton = new Button("Insert");
    private final Button searchButton = new Button("Search");
    private final Button deleteButton = new Button("Delete");
    private final Queue<Integer> insertQueue = new ArrayDeque<Integer>();
    private final Timer animationTimer;

    private Node root;
    private Node currentNode;
    private Node parentNode;
    private Node highlightedNode;
    private Node deletionTarget;
    private int pendingValue;
    private Operation operation = Operation.NONE;
    private boolean findingSuccessor;

    public insertion() {
        super("Binary Search Tree Visualizer");
        setLayout(new BorderLayout(8, 8));

        Panel controls = new Panel();
        controls.add(new Label("Value(s):"));
        controls.add(valueField);
        controls.add(insertButton);
        controls.add(searchButton);
        controls.add(deleteButton);

        add(controls, BorderLayout.NORTH);
        add(treeCanvas, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        animationTimer = new Timer(ANIMATION_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                advanceOperation();
            }
        });

        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                beginInsert();
            }
        });
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                beginSearch();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                beginDelete();
            }
        });
        valueField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                beginInsert();
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                animationTimer.stop();
                dispose();
            }
        });

        setSize(950, 650);
        setLocationRelativeTo(null);
    }

    private void beginInsert() {
        if (operation != Operation.NONE) {
            return;
        }

        String input = valueField.getText().trim();
        if (input.length() == 0) {
            statusLabel.setText("Enter one or more integer values to insert.");
            return;
        }

        String[] values = input.split("[,\\s]+");
        List<Integer> parsedValues = new ArrayList<Integer>();
        try {
            for (String value : values) {
                parsedValues.add(Integer.valueOf(value));
            }
        } catch (NumberFormatException exception) {
            statusLabel.setText("Use integers separated by commas or spaces.");
            return;
        }

        insertQueue.addAll(parsedValues);
        valueField.setText("");
        startNextInsert();
    }

    private void startNextInsert() {
        if (insertQueue.isEmpty()) {
            finishOperation();
            return;
        }

        pendingValue = insertQueue.remove();
        operation = Operation.INSERT;
        currentNode = root;
        parentNode = null;
        highlightedNode = null;
        statusLabel.setText("Inserting " + pendingValue + ": starting at the root.");
        treeCanvas.repaint();
        animationTimer.start();
    }

    private void beginSearch() {
        Integer value = readSingleValue("search");
        if (value == null || operation != Operation.NONE) {
            return;
        }

        pendingValue = value.intValue();
        operation = Operation.SEARCH;
        currentNode = root;
        parentNode = null;
        highlightedNode = null;
        statusLabel.setText("Searching for " + pendingValue + ": starting at the root.");
        treeCanvas.repaint();
        animationTimer.start();
    }

    private void beginDelete() {
        Integer value = readSingleValue("delete");
        if (value == null || operation != Operation.NONE) {
            return;
        }

        pendingValue = value.intValue();
        operation = Operation.DELETE;
        currentNode = root;
        parentNode = null;
        deletionTarget = null;
        findingSuccessor = false;
        highlightedNode = null;
        statusLabel.setText("Deleting " + pendingValue + ": searching from the root.");
        treeCanvas.repaint();
        animationTimer.start();
    }

    private Integer readSingleValue(String action) {
        if (operation != Operation.NONE) {
            return null;
        }

        String input = valueField.getText().trim();
        try {
            if (input.length() == 0 || input.matches(".*[,\\s].*")) {
                throw new NumberFormatException();
            }
            return Integer.valueOf(input);
        } catch (NumberFormatException exception) {
            statusLabel.setText("Enter one integer value to " + action + ".");
            return null;
        }
    }

    private void advanceOperation() {
        if (operation == Operation.INSERT) {
            advanceInsert();
        } else if (operation == Operation.SEARCH) {
            advanceSearch();
        } else if (operation == Operation.DELETE) {
            advanceDelete();
        }
        treeCanvas.repaint();
    }

    private void advanceInsert() {
        if (currentNode == null) {
            Node inserted = new Node(pendingValue);
            if (parentNode == null) {
                root = inserted;
            } else if (pendingValue < parentNode.value) {
                parentNode.left = inserted;
            } else {
                parentNode.right = inserted;
            }
            highlightedNode = inserted;
            statusLabel.setText("Inserted " + pendingValue + " in its correct position.");
            startNextInsertAfterCurrentStep();
            return;
        }

        highlightedNode = currentNode;
        if (pendingValue == currentNode.value) {
            statusLabel.setText(pendingValue + " is already in the tree; duplicates are not inserted.");
            startNextInsertAfterCurrentStep();
        } else {
            String direction = pendingValue < currentNode.value ? "left" : "right";
            statusLabel.setText(pendingValue + " is " + direction + " of " + currentNode.value + "; compare there next.");
            parentNode = currentNode;
            currentNode = pendingValue < currentNode.value ? currentNode.left : currentNode.right;
        }
    }

    private void startNextInsertAfterCurrentStep() {
        if (insertQueue.isEmpty()) {
            animationTimer.stop();
            operation = Operation.NONE;
        } else {
            pendingValue = insertQueue.remove();
            operation = Operation.INSERT;
            currentNode = root;
            parentNode = null;
        }
    }

    private void advanceSearch() {
        if (currentNode == null) {
            statusLabel.setText(pendingValue + " was not found in the tree.");
            finishOperation();
            return;
        }

        highlightedNode = currentNode;
        if (pendingValue == currentNode.value) {
            statusLabel.setText("Found " + pendingValue + "!");
            finishOperation();
            return;
        }

        String direction = pendingValue < currentNode.value ? "left" : "right";
        statusLabel.setText(pendingValue + " is " + direction + " of " + currentNode.value + "; moving " + direction + ".");
        currentNode = pendingValue < currentNode.value ? currentNode.left : currentNode.right;
    }

    private void advanceDelete() {
        if (findingSuccessor) {
            highlightedNode = currentNode;
            if (currentNode.left != null) {
                statusLabel.setText("Looking for the in-order successor: move left from " + currentNode.value + ".");
                currentNode = currentNode.left;
                return;
            }

            int successorValue = currentNode.value;
            deletionTarget.right = deleteNode(deletionTarget.right, successorValue);
            deletionTarget.value = successorValue;
            highlightedNode = deletionTarget;
            statusLabel.setText("Replaced " + pendingValue + " with its in-order successor " + successorValue + ".");
            finishOperation();
            return;
        }

        if (currentNode == null) {
            statusLabel.setText(pendingValue + " was not found; nothing was deleted.");
            finishOperation();
            return;
        }

        highlightedNode = currentNode;
        if (pendingValue == currentNode.value) {
            if (currentNode.left != null && currentNode.right != null) {
                deletionTarget = currentNode;
                currentNode = currentNode.right;
                findingSuccessor = true;
                statusLabel.setText("Found " + pendingValue + ". Find its in-order successor in the right subtree.");
            } else {
                root = deleteNode(root, pendingValue);
                statusLabel.setText("Deleted " + pendingValue + ".");
                finishOperation();
            }
            return;
        }

        String direction = pendingValue < currentNode.value ? "left" : "right";
        statusLabel.setText(pendingValue + " is " + direction + " of " + currentNode.value + "; moving " + direction + ".");
        parentNode = currentNode;
        currentNode = pendingValue < currentNode.value ? currentNode.left : currentNode.right;
    }

    private Node deleteNode(Node node, int value) {
        if (node == null) {
            return null;
        }
        if (value < node.value) {
            node.left = deleteNode(node.left, value);
        } else if (value > node.value) {
            node.right = deleteNode(node.right, value);
        } else if (node.left == null) {
            return node.right;
        } else if (node.right == null) {
            return node.left;
        } else {
            Node successor = node.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            node.value = successor.value;
            node.right = deleteNode(node.right, successor.value);
        }
        return node;
    }

    private void finishOperation() {
        animationTimer.stop();
        operation = Operation.NONE;
        currentNode = null;
        parentNode = null;
        deletionTarget = null;
        findingSuccessor = false;
        treeCanvas.repaint();
    }

    private enum Operation {
        NONE, INSERT, SEARCH, DELETE
    }

    private static class Node {
        private int value;
        private int x;
        private int y;
        private int depth;
        private Node left;
        private Node right;

        private Node(int value) {
            this.value = value;
        }
    }

    private class TreeCanvas extends Canvas {
        private TreeCanvas() {
            setBackground(Color.WHITE);
        }

        public Dimension getPreferredSize() {
            return new Dimension(900, 540);
        }

        public void paint(Graphics graphics) {
            super.paint(graphics);
            Graphics2D g = (Graphics2D) graphics;
            if (root == null) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("SansSerif", Font.PLAIN, 16));
                g.drawString("Your tree is empty. Enter values above and click Insert.", 24, 36);
                return;
            }

            List<Node> nodes = new ArrayList<Node>();
            collectInOrder(root, nodes, 0);
            int width = getWidth();
            int spacing = Math.max(NODE_DIAMETER + 8, width / (nodes.size() + 1));
            for (int index = 0; index < nodes.size(); index++) {
                Node node = nodes.get(index);
                node.x = spacing * (index + 1);
                node.y = 38 + node.depth * LEVEL_GAP;
            }

            g.setColor(new Color(100, 100, 100));
            drawEdges(g, root);
            for (Node node : nodes) {
                drawNode(g, node);
            }
        }

        private void collectInOrder(Node node, List<Node> nodes, int depth) {
            if (node == null) {
                return;
            }
            node.depth = depth;
            collectInOrder(node.left, nodes, depth + 1);
            nodes.add(node);
            collectInOrder(node.right, nodes, depth + 1);
        }

        private void drawEdges(Graphics2D g, Node node) {
            if (node == null) {
                return;
            }
            if (node.left != null) {
                g.drawLine(node.x, node.y, node.left.x, node.left.y);
            }
            if (node.right != null) {
                g.drawLine(node.x, node.y, node.right.x, node.right.y);
            }
            drawEdges(g, node.left);
            drawEdges(g, node.right);
        }

        private void drawNode(Graphics2D g, Node node) {
            int left = node.x - NODE_DIAMETER / 2;
            int top = node.y - NODE_DIAMETER / 2;
            if (node == highlightedNode) {
                g.setColor(new Color(255, 204, 102));
            } else {
                g.setColor(new Color(143, 203, 247));
            }
            g.fillOval(left, top, NODE_DIAMETER, NODE_DIAMETER);
            g.setColor(new Color(45, 75, 100));
            g.drawOval(left, top, NODE_DIAMETER, NODE_DIAMETER);
            String text = String.valueOf(node.value);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, node.x - textWidth / 2, node.y + 5);
        }
    }

    public static void main(String[] args) {
        insertion window = new insertion();
        window.setVisible(true);
    }
}
