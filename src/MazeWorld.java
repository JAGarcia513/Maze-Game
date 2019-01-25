
// Comments: 176-7; starting at 219 with determinePath ending at 240

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class EdgeComparator implements Comparator<Edge> {

  @Override
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }
}

class Vertex {
  // Position of the Vertex
  Posn pos;

  // Vertex's above, below, and to the sides
  Vertex up;
  Vertex down;
  Vertex left;
  Vertex right;
  Vertex previous;

  // Edges from this vertex
  ArrayList<Edge> outEdges;

  // Does this vertex have a path going up?
  boolean hasUp;
  boolean hasDown;
  boolean hasLeft;
  boolean hasRight;

  boolean alreadySeen;
  boolean onCorrectPath;

  Vertex(int x, int y) {
    this.pos = new Posn(x, y);
    this.hasUp = true;
    this.hasDown = true;
    this.hasLeft = true;
    this.hasRight = true;
    this.outEdges = new ArrayList<Edge>();
    this.alreadySeen = false;
    this.onCorrectPath = false;
  }

  /*-
   * Fields:
   * ... this.pos ... -- Posn
   * ... this.up ... -- Vertex
   * ... this.down ... -- Vertex
   * ... this.left ... -- Vertex
   * ... this.right ... -- Vertex
   * ... this.outEdges ... -- ArrayList<Edge>
   * ... this.hasUp ... -- boolean
   * ... this.hasDonw ... -- boolean
   * ... this.hasRight ... -- boolean
   * ... this.hasLeft ... -- boolean
   * Methods:
   * ... this.drawVertex(int) ... -- WorldImage
   * ... this.checkPaths() ... -- void
   * ... this.equals(Vertex v) ... --- boolean
   */

  // Draws vertex
  public WorldImage drawVertex(int vertexSize) {
    Color c = Color.LIGHT_GRAY;

    if (this.alreadySeen) {
      c = Color.CYAN;
    }
    if (this.onCorrectPath) {
      c = Color.BLUE;
    }
    return new RectangleImage(vertexSize, vertexSize, OutlineMode.SOLID, c);
  }

  // Sets the hasUp and hasLeft fields for the given vertex
  // EFFECT: Sets the hasUp and hasLeft fields for the given vertex
  public void checkPaths() {
    for (Edge e : this.outEdges) {
      if (e.to.equals(this.right)) {
        this.hasUp = false;
      }

      if (e.to.equals(this.down)) {
        this.hasLeft = false;
      }
    }
  }
}

// Represents connections between the vertices
class Edge {
  Vertex to;
  Vertex from;
  int weight;

  Edge(int weight, Vertex to, Vertex from) {
    this.weight = weight;
    this.to = to;
    this.from = from;
  }

  /*-
   * Fields:
   * ... this.to ... -- Vertex
   * ... this.from ... -- Vertex
   * ... this.weight ... -- int
   * Methods:
   * ... this.compareTo(Edge) ... -- boolean
   * ... this.drawHorizontalWall(int) ... -- WorldImage
   * ... this.drawVerticalWall(int) ... -- WorldImage
   */

  boolean compareTo(Edge e) {
    return this.weight < e.weight;
  }

  // Draws a horizontal wall on the canvas
  WorldImage drawHorizontalWall(int size) {
    return new RectangleImage(size, 3, OutlineMode.SOLID, Color.BLACK);
  }

  // Draws a Vertical wall on the canvas
  WorldImage drawVerticalWall(int size) {
    return new RectangleImage(3, size, OutlineMode.SOLID, Color.BLACK);
  }
}

class Player {
  int x;
  int y;
  Color col;
  Vertex cur;

  Player(int x, int y, Color col) {
    this.x = x;
    this.y = y;
    this.col = col;
  }

  /*-
   * Fields
   * ... this.x ... -- int
   * ... this.y ... -- int
   * ... this.col ... -- Color
   * ... this.cur ... -- Vertex
   * Methods:
   * ... this.drawPlayer(int) ... -- WorldImage
   */

  WorldImage drawPlayer(int size) {
    return new RectangleImage(size, size, OutlineMode.SOLID, this.col);
  }
}

// Represents out world
class MazeWorld extends World {
  // Represents the grid we use
  ArrayList<ArrayList<Vertex>> maze;
  ArrayList<Vertex> alreadySeen;
  ArrayList<Vertex> workList;
  ArrayList<Vertex> curSearchPath;
  ArrayList<Edge> initialEdges;
  ArrayList<Edge> tree;
  ArrayList<Vertex> correctPath;

  // Represents the map of our tree
  HashMap<Vertex, Vertex> map;
  HashMap<Vertex, Vertex> path = new HashMap<Vertex, Vertex>();

  // Represents the last cell of the maze
  Vertex end;

  // Represents the player
  Player player;
  Random rand = new Random();
  int vertexSize;
  int mazeWidth;
  int mazeHeight;
  boolean depthFirst;
  boolean breadthFirst;
  boolean reachedEnd;
  boolean found;

  MazeWorld(int mazeWidth, int mazeHeight) {
    this.mazeWidth = mazeWidth;
    this.mazeHeight = mazeHeight;
    this.vertexSize = 10;
    this.depthFirst = false;
    this.breadthFirst = false;
    this.found = false;
  }

  /*- 
   * Fields:
   * ... this.maze ... -- ArrayList<ArrayList<Vertex>>
   * ... this.map ... -- HashMap<Vertex, Vertex>
   * ... this.initialEdges ... -- ArrayList<Edge>
   * ... this.tree ... -- ArrayList<Edge>
   * ... this.player ... -- Player
   * ... this.rand ... -- Random
   * ... this.vertexSize ... -- int
   * ... this.mazeWidth ... -- int
   * ... this.mazeHeight ... -- int
   * Methods
   * ... this.initMaze() ... -- void
   * ... this.generateAdjacent() ... -- void
   * ... this.addBorders() ... -- void
   * ... this.buildWalls() ... -- void
   * ... this.union(Vertex, Vertex) ... -- void
   * ... this.unionFind(Vertex) ... -- Vertex
   * ... this.initMap() ... -- void
   * ... this.mazeScene() ... -- WorldImage
   * ... this.worldEnds() ... -- WorldEnd
   * ... this.determinePaths() ... -- void
   * ... this.onTick() ... -- void
   * ... this.onKeyEvent(String) ... -- void
   */

  // EFFECT: Initializes maze data structure with Vertices and
  void initMaze() {
    map = new HashMap<Vertex, Vertex>();
    maze = new ArrayList<ArrayList<Vertex>>();
    initialEdges = new ArrayList<Edge>();
    tree = new ArrayList<Edge>();
    correctPath = new ArrayList<Vertex>();
    curSearchPath = new ArrayList<Vertex>();
    workList = new ArrayList<Vertex>();
    alreadySeen = new ArrayList<Vertex>();
    this.reachedEnd = false;

    for (int i = 0; i < mazeHeight; i++) {
      ArrayList<Vertex> row = new ArrayList<Vertex>();
      for (int j = 0; j < mazeWidth; j++) {
        row.add(new Vertex(j, i));
      }
      this.maze.add(row);
    }
    this.player = new Player(0, 0, Color.GREEN);
    this.generateAdjacent();
    this.initMap();
    this.buildWalls();
    this.workList.add(this.maze.get(0).get(0));
    this.end = this.maze.get(this.maze.size() - 2).get(this.maze.get(0).size() - 2);
  }

  // EFFECT: Intializes down and right edges between vertices. As well as
  // intiaziles the up and left fields for the other vertex
  void generateAdjacent() {
    for (int i = 0; i < mazeHeight; i++) {
      for (int j = 0; j < mazeWidth; j++) {
        Vertex v = this.maze.get(i).get(j);
        if (i < mazeHeight - 1) {
          v.down = this.maze.get(i + 1).get(j);
          this.maze.get(i + 1).get(j).up = v;
          Edge e = new Edge(rand.nextInt(50), v.down, v);
          this.initialEdges.add(e);
        }
        if (j < mazeWidth - 1) {
          v.right = this.maze.get(i).get(j + 1);
          this.maze.get(i).get(j + 1).left = v;
          Edge e = new Edge(rand.nextInt(50), v.right, v);
          this.initialEdges.add(e);
        }
      }
    }
    Collections.sort(initialEdges, new EdgeComparator());
  }

  // EFFECT: Calls checkPaths on all vertex's in the maze, then sets the hasRight
  // and hasDown
  // fields accordingly
  void determinePaths() {
    for (int i = 0; i < mazeHeight; i++) {
      ArrayList<Vertex> row = this.maze.get(i);
      for (int j = 0; j < mazeWidth; j++) {
        Vertex v = row.get(j);
        if (j < mazeWidth - 1) {
          v.checkPaths();
        }
        if (i < mazeHeight - 1) {
          v.checkPaths();
        }
      }
    }

    for (int i = 0; i < mazeHeight; i++) {
      ArrayList<Vertex> row = this.maze.get(i);
      for (int j = 0; j < mazeWidth; j++) {
        Vertex v = row.get(j);
        if (j > 0) {
          if (!v.hasLeft) {
            row.get(j - 1).hasRight = false;
          }
        }
        if (i > 0) {
          if (!v.hasUp) {
            maze.get(i - 1).get(j).hasDown = false;
          }
        }
      }
    }
  }

  // EFFECT: Adds the borders of the entire maze to the tree field, and to the
  // outedges of their
  // corresponding vertex
  void addBorders() {
    for (Edge e : initialEdges) {
      Vertex to = e.to;
      Vertex from = e.from;
      if (e.to.pos.x == 0 && e.from.pos.x == 0) {
        this.tree.add(e);
        this.union(to, from, map);
        from.outEdges.add(e);
      }
      if (e.to.pos.y == 0 && e.from.pos.y == 0) {
        this.tree.add(e);
        this.union(to, from, map);
        from.outEdges.add(e);
      }
      if (e.to.pos.x == mazeWidth - 1 && e.from.pos.x == mazeWidth - 1) {
        this.tree.add(e);
        this.union(to, from, map);
        from.outEdges.add(e);
      }
      if (e.to.pos.y == mazeHeight - 1 && e.from.pos.y == mazeHeight - 1) {
        this.tree.add(e);
        this.union(to, from, map);
        from.outEdges.add(e);
      }
    }
  }

  // EFFECT: Applies Kriskals algorithm to the list of edges, generating a tree
  void buildWalls() {
    int i = 0;
    this.addBorders();
    while (i < (this.mazeHeight * this.mazeWidth) - 1) {
      Edge origin = this.initialEdges.get(i);
      Vertex v1 = origin.to;
      Vertex v2 = origin.from;
      if (!this.unionFind(v1, map).equals(this.unionFind(v2, map))) {
        tree.add(origin);
        this.union(v1, v2, map);
        v2.outEdges.add(origin);
        v1.outEdges.add(new Edge(origin.weight, v2, v1));
      }
      i++;
    }
    this.determinePaths();
  }

  // EFFECT: Sets v1's representative to v2's representative in the map field
  void union(Vertex v1, Vertex v2, HashMap<Vertex, Vertex> map) {
    map.put(this.unionFind(v1, map), this.unionFind(v2, map));
  }

  // Returns the vertex associated with the given vertex
  Vertex unionFind(Vertex v1, HashMap<Vertex, Vertex> map) {
    if (map.get(v1).equals(v1)) {
      return v1;
    } else {
      return this.unionFind(map.get(v1), map);
    }
  }

  // EFFECT: Initializes the hashmap
  void initMap() {
    for (int i = 0; i < this.mazeHeight; i++) {
      ArrayList<Vertex> row = this.maze.get(i);
      for (int j = 0; j < this.mazeWidth; j++) {
        this.map.put(row.get(j), row.get(j));
      }
    }
  }

  // EFFECT: On key presses moves the player to a spot if they are capable.
  @Override
  public void onKeyEvent(String s) {
    Vertex current = this.maze.get(player.y).get(player.x);
    if (s.equals("s") && current.hasDown) {
      current.alreadySeen = true;

      this.player.y += 1;
      current.down.previous = current;

    }
    if (s.equals("w") && current.hasUp) {
      current.alreadySeen = true;

      this.player.y -= 1;
      current.up.previous = current;

    }
    if (s.equals("a") && current.hasLeft) {
      current.alreadySeen = true;

      this.player.x -= 1;
      current.left.previous = current;

    }
    if (s.equals("d") && current.hasRight) {
      current.alreadySeen = true;

      this.player.x += 1;
      current.right.previous = current;

    }

    if (s.equals("n")) {
      breadthFirst = true;
    }
    if (s.equals("m")) {
      depthFirst = true;
    }

    // If the player hits enter, reset maze.
    if (s.equals("enter")) {
      this.initMaze();
    }
  }

  // Builds the scene, drawing each vertex in the maze field, and the edges
  // between vertices
  @Override
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene((mazeWidth - 1) * vertexSize, (mazeHeight - 1) * vertexSize);
    int offset = vertexSize / 2;

    // Draw all vertices/cells
    for (ArrayList<Vertex> row : this.maze) {
      for (Vertex v : row) {
        ws.placeImageXY(v.drawVertex(vertexSize).movePinhole(-offset, -offset),
            (v.pos.x) * vertexSize, (v.pos.y) * vertexSize);
      }
    }

    // Draw Player and last vertex
    ws.placeImageXY(this.player.drawPlayer(vertexSize).movePinhole(-offset, -offset),
        player.x * vertexSize, player.y * vertexSize);
    ws.placeImageXY(
        new RectangleImage(this.vertexSize, this.vertexSize, OutlineMode.SOLID, Color.RED)
            .movePinhole(-offset, -offset),
        maze.get(mazeHeight - 2).get(mazeWidth - 2).pos.x * vertexSize,
        maze.get(mazeHeight - 2).get(mazeWidth - 2).pos.y * vertexSize);

    // Draw edges between vertices
    for (Edge e : this.tree) {
      Vertex v1 = e.from;
      Vertex v2 = e.to;
      Posn pos1 = v1.pos;
      if (v1.pos.x == v2.pos.x) {
        ws.placeImageXY(e.drawVerticalWall(vertexSize).movePinhole(0, -this.vertexSize / 2),
            pos1.x * this.vertexSize, pos1.y * this.vertexSize);
      } else {
        ws.placeImageXY(e.drawHorizontalWall(vertexSize).movePinhole(-this.vertexSize / 2, 0),
            pos1.x * this.vertexSize, pos1.y * this.vertexSize);
      }
    }

    return ws;
  }

  // Determines when the game should end.
  @Override
  public WorldEnd worldEnds() {
    WorldScene ws = this.getEmptyScene();
    ws.placeImageXY(new TextImage("Maze Complete", Color.BLACK), this.mazeWidth * vertexSize,
        this.mazeHeight * vertexSize);
    boolean end = false;
    if (this.player.x == this.end.pos.x && this.player.y == this.end.pos.y) {
      end = true;
      return new WorldEnd(end, ws);
    }
    return new WorldEnd(end, this.makeScene());
  }

  // Generates the solution for the maze
  void generateSolution() {
    Vertex v = this.end;
    while (!this.maze.get(0).get(0).equals(v)) {
      this.correctPath.add(v);
      v.onCorrectPath = true;
      v = v.previous;
    }
  }

  // EFFECT: Performs depth and breadth first searches if they are enabled.
  @Override
  public void onTick() {
    if (depthFirst && workList.size() > 0 && !this.found) {
      Vertex check = workList.remove(0);
      if (check.equals(end)) {
        curSearchPath.add(check);
        this.generateSolution();
        found = true;
      } else if (curSearchPath.contains(check)) {
        // Do not add anything to the stack
      } else {
        if (check.hasDown && !check.down.alreadySeen) {
          workList.add(0, check.down);
          check.down.previous = check;
        }
        if (check.hasUp && !check.up.alreadySeen) {
          workList.add(0, check.up);
          check.up.previous = check;
        }
        if (check.hasLeft && !check.left.alreadySeen) {
          workList.add(0, check.left);
          check.left.previous = check;
        }
        if (check.hasRight && !check.right.alreadySeen) {
          workList.add(0, check.right);
          check.right.previous = check;
        }
        curSearchPath.add(check);
      }
      check.alreadySeen = true;
    }

    if (breadthFirst && workList.size() > 0) {
      Vertex check = workList.remove(0);
      if (check.equals(end)) {
        curSearchPath.add(check);
        this.generateSolution();
        found = true;
      } else if (curSearchPath.contains(check)) {
        // Do not add anything to the queue
      } else {
        if (check.hasDown && !check.down.alreadySeen) {
          workList.add(check.down);
          check.down.previous = check;
        }
        if (check.hasUp && !check.up.alreadySeen) {
          workList.add(check.up);
          check.up.previous = check;
        }
        if (check.hasLeft && !check.left.alreadySeen) {
          workList.add(check.left);
          check.left.previous = check;
        }
        if (check.hasRight && !check.right.alreadySeen) {
          workList.add(check.right);
          check.right.previous = check;
        }
        curSearchPath.add(check);
      }
      check.alreadySeen = true;
    }
  }
}

class ExamplesMazeWorld {
  MazeWorld mw;
  int width;
  int height;
  ArrayList<Edge> edges;
  Vertex v;
  Vertex v1;
  Edge e;
  Edge e1;
  Edge e2;
  Player p;

  void initData() {
    edges = new ArrayList<Edge>();
    width = 10;
    height = 10;
    e = new Edge(10, v1, v);
    e1 = new Edge(15, v1, v);
    e2 = new Edge(20, v1, v);
    v = new Vertex(0, 0);
    v1 = new Vertex(1, 0);
    mw = new MazeWorld(width, height);
    p = new Player(0, 0, Color.GREEN);
  }

  void testUnionFind(Tester t) {
    initData();
    mw.initMaze();
    mw.initMap();
    t.checkExpect(mw.unionFind(mw.maze.get(0).get(0), mw.map), mw.maze.get(0).get(0));
    t.checkExpect(mw.unionFind(mw.maze.get(1).get(0), mw.map), mw.maze.get(1).get(0));
    t.checkExpect(mw.unionFind(mw.maze.get(0).get(1), mw.map), mw.maze.get(0).get(1));
  }

  void testDeterminePaths(Tester t) {
    initData();
    mw.initMaze();
    t.checkExpect(mw.maze.get(0).get(0).hasUp, false);
    t.checkExpect(mw.maze.get(0).get(0).hasLeft, false);
    t.checkExpect(mw.maze.get(0).get(0).hasRight, mw.maze.get(0).get(1).hasLeft);
    t.checkExpect(mw.maze.get(0).get(0).hasDown, mw.maze.get(1).get(0).hasUp);
  }

  void testGenerateAdjacent(Tester t) {
    initData();
    mw.initMaze();
    mw.generateAdjacent();
    t.checkExpect(mw.maze.get(0).get(0).right, mw.maze.get(0).get(1));
    t.checkExpect(mw.maze.get(0).get(0).up, null);
    t.checkExpect(mw.maze.get(0).get(0).down, mw.maze.get(1).get(0));
    t.checkExpect(mw.maze.get(height - 1).get(width - 1).down, null);
  }

  void testOnKeyEvent(Tester t) {
    initData();
    mw.initMaze();
    mw.maze.get(0).get(0).hasRight = true;
    mw.onKeyEvent("d");
    t.checkExpect(mw.player.x, 1);
    mw.maze.get(0).get(1).hasRight = true;
    mw.onKeyEvent("d");
    t.checkExpect(mw.player.x, 2);
    mw.maze.get(0).get(2).hasRight = true;
    mw.onKeyEvent("d");
    t.checkExpect(mw.player.x, 3);
  }

  void testCheckPaths(Tester t) {
    initData();
    mw.initMaze();

    mw.maze.get(0).get(0).checkPaths();
    t.checkExpect(mw.maze.get(0).get(0).hasUp, false);
    t.checkExpect(mw.maze.get(0).get(0).hasLeft, false);
    t.checkExpect(mw.maze.get(0).get(0).hasRight, mw.maze.get(0).get(1).hasLeft);
    t.checkExpect(mw.maze.get(0).get(0).hasDown, mw.maze.get(1).get(0).hasUp);
  }

  void testInitMap(Tester t) {
    initData();
    mw.initMaze();
    mw.initMap();
    t.checkExpect(mw.map.get(mw.maze.get(0).get(0)), mw.maze.get(0).get(0));
    t.checkExpect(mw.map.get(mw.maze.get(1).get(0)), mw.maze.get(1).get(0));
    t.checkExpect(mw.map.get(mw.maze.get(0).get(1)), mw.maze.get(0).get(1));
    t.checkExpect(mw.map.get(mw.maze.get(1).get(1)), mw.maze.get(1).get(1));
  }

  void testMazeWorld(Tester t) {
    initData();
    mw.initMaze();
    mw.bigBang(1000, 700, 0.5);
  }

  void testDrawHorizontalWall(Tester t) {
    initData();
    t.checkExpect(e.drawHorizontalWall(10),
        new RectangleImage(10, 3, OutlineMode.SOLID, Color.BLACK));
    t.checkExpect(e.drawHorizontalWall(11),
        new RectangleImage(11, 3, OutlineMode.SOLID, Color.BLACK));
    t.checkExpect(e.drawHorizontalWall(12),
        new RectangleImage(12, 3, OutlineMode.SOLID, Color.BLACK));
  }

  void testDrawVerticalWall(Tester t) {
    initData();
    t.checkExpect(e.drawVerticalWall(10),
        new RectangleImage(3, 10, OutlineMode.SOLID, Color.BLACK));
    t.checkExpect(e.drawVerticalWall(20),
        new RectangleImage(3, 20, OutlineMode.SOLID, Color.BLACK));
    t.checkExpect(e.drawVerticalWall(30),
        new RectangleImage(3, 30, OutlineMode.SOLID, Color.BLACK));
  }

  void testCompareTo(Tester t) {
    initData();
    t.checkExpect(e.compareTo(e1), true);
    t.checkExpect(e1.compareTo(e), false);
    t.checkExpect(e.compareTo(e2), true);
    t.checkExpect(e2.compareTo(e), false);
    t.checkExpect(e1.compareTo(e2), true);
  }

  void testDrawPlayer(Tester t) {
    initData();
    t.checkExpect(p.drawPlayer(10), new RectangleImage(10, 10, OutlineMode.SOLID, p.col));
    t.checkExpect(p.drawPlayer(20), new RectangleImage(20, 20, OutlineMode.SOLID, p.col));
    t.checkExpect(p.drawPlayer(30), new RectangleImage(30, 30, OutlineMode.SOLID, p.col));
  }

  void addBorders(Tester t) {
    initData();
    mw.initMaze();
    mw.initialEdges.set(0, e);
    mw.addBorders();
    t.checkExpect(mw.tree.get(mw.tree.size() - 1), e);
  }
  
  void testGenerateSolution(Tester t) {
    initData();
    mw.initMaze();
    t.checkExpect(mw.maze.get(0).get(0).previous, null);
    t.checkExpect(mw.maze.get(0).get(0).onCorrectPath, false);
    t.checkExpect(mw.maze.get(mw.mazeHeight - 1).get(mw.mazeWidth - 1).previous, null);
  }

  void testUnion(Tester t) {
    initData();
    mw.initMaze();
    mw.initMap();
    mw.union(mw.maze.get(0).get(0), mw.maze.get(0).get(1), mw.map);
    t.checkExpect(mw.map.get(mw.maze.get(0).get(0)), mw.maze.get(0).get(1));
    mw.union(mw.maze.get(0).get(0), mw.maze.get(0).get(2), mw.map);
    t.checkExpect(mw.map.get(mw.maze.get(0).get(2)), mw.maze.get(0).get(2));
    mw.union(mw.maze.get(1).get(0), mw.maze.get(0).get(2), mw.map);
    t.checkExpect(mw.map.get(mw.maze.get(1).get(0)), mw.maze.get(0).get(2));
    mw.union(mw.maze.get(2).get(0), mw.maze.get(0).get(2), mw.map);
    t.checkExpect(mw.map.get(mw.maze.get(2).get(0)), mw.maze.get(0).get(2));
  }
}