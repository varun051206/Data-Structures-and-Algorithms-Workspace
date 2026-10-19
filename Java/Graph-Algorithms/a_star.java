import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class a_star extends Frame {
    private static final int SIZE = 36;
    private static final int CELL_SIZE = 18;
    private static final int GRID_X = 24;
    private static final int GRID_Y = 64;

    // Each array cell is a graph vertex; edges connect its walkable neighbors.
    private final int[][] graph = new int[SIZE][SIZE];
    private final int[][] cost = new int[SIZE][SIZE];
    private final Point[][] parent = new Point[SIZE][SIZE];
    private final boolean[][] explored = new boolean[SIZE][SIZE];
    private final boolean[][] shownExplored = new boolean[SIZE][SIZE];
    private final boolean[][] shownPath = new boolean[SIZE][SIZE];
    private final List<Point> exploreOrder = new ArrayList<>();
    private final List<Point> path = new ArrayList<>();
    private final Board board = new Board();

    private Point start;
    private Point goal;
    private volatile boolean animating;
    private volatile String status = "Click a cell to choose the start node.";

    public a_star() {
        super("A* Pathfinding - 36 x 36 Grid");
        setResizable(false);
        add(board);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });

        makeWalls();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void makeWalls() {
        for (int x = 3; x < 31; x++) {
            if (x != 12) graph[8][x] = -1;
        }
        for (int x = 5; x < 35; x++) {
            if (x != 25) graph[17][x] = -1;
        }
        for (int x = 1; x < 31; x++) {
            if (x != 8) graph[26][x] = -1;
        }
    }

    private void findPath() {
        for (boolean[] row : explored) Arrays.fill(row, false);
        for (boolean[] row : shownExplored) Arrays.fill(row, false);
        for (boolean[] row : shownPath) Arrays.fill(row, false);
        for (Point[] row : parent) Arrays.fill(row, null);
        exploreOrder.clear();
        path.clear();
        for (int[] row : cost) Arrays.fill(row, Integer.MAX_VALUE);

        PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.comparingInt((Node node) -> node.score)
                        .thenComparingInt(node -> node.heuristic));
        cost[start.y][start.x] = 0;
        open.add(new Node(start.x, start.y, 0, distance(start.x, start.y, goal)));

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        while (!open.isEmpty()) {
            Node current = open.remove();
            if (explored[current.y][current.x]) continue;
            explored[current.y][current.x] = true;
            exploreOrder.add(new Point(current.x, current.y));

            if (current.x == goal.x && current.y == goal.y) break;

            for (int i = 0; i < dx.length; i++) {
                int x = current.x + dx[i];
                int y = current.y + dy[i];
                if (x < 0 || x >= SIZE || y < 0 || y >= SIZE
                        || graph[y][x] == -1 || explored[y][x]) continue;

                int nextCost = cost[current.y][current.x] + 1;
                if (nextCost < cost[y][x]) {
                    cost[y][x] = nextCost;
                    parent[y][x] = new Point(current.x, current.y);
                    int heuristic = distance(x, y, goal);
                    open.add(new Node(x, y, nextCost, heuristic));
                }
            }
        }

        if (!explored[goal.y][goal.x]) {
            status = "No path found.";
            return;
        }

        for (Point point = goal; point != null; point = parent[point.y][point.x]) {
            path.add(0, point);
        }
    }

    private int distance(int x, int y, Point goal) {
        return Math.abs(goal.x - x) + Math.abs(goal.y - y);
    }

    private void animate() {
        Thread animation = new Thread(() -> {
            try {
                for (Point point : exploreOrder) {
                    shownExplored[point.y][point.x] = true;
                    board.repaintCell(point.x, point.y);
                    Thread.sleep(80);
                }
                for (Point point : path) {
                    shownPath[point.y][point.x] = true;
                    board.repaintCell(point.x, point.y);
                    Thread.sleep(200);
                }
                status = "Shortest path: " + (path.size() - 1)
                        + " steps   |   Explored: " + exploreOrder.size()
                        + " nodes   |   Click to choose a new start.";
                animating = false;
                board.repaint();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                animating = false;
            }
        }, "a-star-animation");
        animation.setDaemon(true);
        animation.start();
    }

    private static class Node {
        final int x;
        final int y;
        final int score;
        final int heuristic;

        Node(int x, int y, int cost, int heuristic) {
            this.x = x;
            this.y = y;
            this.score = cost + heuristic;
            this.heuristic = heuristic;
        }
    }

    private class Board extends Canvas {
        Board() {
            setPreferredSize(new Dimension(696, 762));
            setBackground(new Color(245, 247, 250));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    selectCell(event.getX(), event.getY());
                }
            });
        }

        private void selectCell(int mouseX, int mouseY) {
            if (animating) return;

            int x = (mouseX - GRID_X) / CELL_SIZE;
            int y = (mouseY - GRID_Y) / CELL_SIZE;
            if (mouseX < GRID_X || mouseY < GRID_Y
                    || x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
            if (graph[y][x] == -1) {
                status = "That cell is a wall. Choose a walkable cell.";
                repaint();
                return;
            }

            if (start == null || goal != null) {
                start = new Point(x, y);
                goal = null;
                for (boolean[] row : shownExplored) Arrays.fill(row, false);
                for (boolean[] row : shownPath) Arrays.fill(row, false);
                status = "Start selected. Click a cell to choose the destination.";
            } else {
                goal = new Point(x, y);
                status = "Searching...  Blue = explored, yellow = shortest path.";
                animating = true;
                findPath();
                if (path.isEmpty()) {
                    status = "No path found. Click a cell to choose a new start.";
                    animating = false;
                } else {
                    animate();
                }
            }
            repaint();
        }

        @Override
        public void paint(Graphics graphics) {
            graphics.setColor(new Color(31, 41, 55));
            graphics.setFont(new Font("SansSerif", Font.BOLD, 18));
            graphics.drawString("A* Search  |  36 x 36 Grid", GRID_X, 28);
            graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
            graphics.setColor(new Color(75, 85, 99));
            graphics.drawString(status, GRID_X, 48);

            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    int px = GRID_X + x * CELL_SIZE;
                    int py = GRID_Y + y * CELL_SIZE;
                    Color color = Color.WHITE;
                    if (graph[y][x] == -1) color = new Color(55, 65, 81);
                    else if (shownPath[y][x]) color = new Color(250, 204, 21);
                    else if (shownExplored[y][x]) color = new Color(147, 197, 253);
                    if (start != null && start.x == x && start.y == y) {
                        color = new Color(34, 197, 94);
                    }
                    if (goal != null && goal.x == x && goal.y == y) {
                        color = new Color(239, 68, 68);
                    }

                    graphics.setColor(color);
                    graphics.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                    graphics.setColor(new Color(209, 213, 219));
                    graphics.drawRect(px, py, CELL_SIZE, CELL_SIZE);
                }
            }

            graphics.setColor(new Color(34, 197, 94));
            graphics.fillRect(GRID_X, GRID_Y + SIZE * CELL_SIZE + 12, 12, 12);
            graphics.setColor(new Color(31, 41, 55));
            graphics.drawString("Start", GRID_X + 18, GRID_Y + SIZE * CELL_SIZE + 23);
            graphics.setColor(new Color(239, 68, 68));
            graphics.fillRect(GRID_X + 75, GRID_Y + SIZE * CELL_SIZE + 12, 12, 12);
            graphics.setColor(new Color(31, 41, 55));
            graphics.drawString("Destination", GRID_X + 93, GRID_Y + SIZE * CELL_SIZE + 23);
            graphics.setColor(new Color(147, 197, 253));
            graphics.fillRect(GRID_X + 190, GRID_Y + SIZE * CELL_SIZE + 12, 12, 12);
            graphics.setColor(new Color(31, 41, 55));
            graphics.drawString("Visited", GRID_X + 208, GRID_Y + SIZE * CELL_SIZE + 23);
            graphics.setColor(new Color(250, 204, 21));
            graphics.fillRect(GRID_X + 270, GRID_Y + SIZE * CELL_SIZE + 12, 12, 12);
            graphics.setColor(new Color(31, 41, 55));
            graphics.drawString("Path", GRID_X + 288, GRID_Y + SIZE * CELL_SIZE + 23);
            graphics.setColor(new Color(75, 85, 99));
            graphics.drawString("Dark cells are walls. Click after the animation to try again.",
                    GRID_X, GRID_Y + SIZE * CELL_SIZE + 43);
        }

        void repaintCell(int x, int y) {
            repaint(GRID_X + x * CELL_SIZE, GRID_Y + y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE + 1);
        }
    }

    public static void main(String[] args) {
        new a_star();
    }
}
