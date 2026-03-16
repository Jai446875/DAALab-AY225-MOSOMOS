Overview
A single-file interactive HTML application that visualizes a network of nodes (cities) in Calabarzon and finds the shortest path between any two nodes using Dijkstra's Algorithm.

Features

Interactive Node Map — 8 nodes, 12 unique undirected edges drawn on a canvas
Shortest Path Finder — uses Dijkstra's Algorithm to find the optimal route
Results Panel — shows the full path, total distance (km), time (min), fuel (L), and node count
Hover Tooltips — hover over any node or edge to see its details
Draggable Nodes — drag any node to reposition it on the map
Edge Highlighting — the shortest path is highlighted in orange on the graph

Nodes
IMUS, BACOOR, DASMA, KAWIT, INDANG, SILANG, GENTRI, NOVELETA
Algorithm
Dijkstra's Algorithm — finds the shortest path by distance between any two selected nodes.
Usage

Select a From Node (origin)
Select a To Node (destination)
Click Calculate Path
The optimal route appears in the sidebar and is highlighted on the map

Report
1. Introduction
This assignment required creating a program that visualizes a network of nodes representing cities in Calabarzon and determines the shortest path between any two nodes based on distance, travel time, and fuel consumption. The program was implemented as a standalone HTML file with embedded CSS and JavaScript, requiring no external frameworks or installation.

2. Dataset
The input data consists of 14 directed connections between 8 cities. Each connection includes three weight attributes: Distance (km), Time (mins), and Fuel (Liters).
From NodeTo NodeDistance (km)Time (mins)Fuel (Liters)IMUSBACOOR10151.2BACOORDASMA12251.5DASMAKAWIT12251.5KAWITINDANG12251.2INDANGSILANG14251.5SILANGGENTRI10251.3GENTRINOVELETA10251.5NOVELETAIMUS10151.2BACOORSILANG10251.3DASMASILANG12251.5SILANGBACOOR10251.3NOVELETABACOOR10151.2SILANGKAWIT14251.2IMUSNOVELETA10151.2
After removing bidirectional duplicates (e.g. BACOOR↔SILANG appears twice in opposite directions), the graph has 8 unique nodes and 12 unique undirected edges.

3. Approach
Part 1 — Node Map
The node map was built using the HTML5 Canvas API:

Nodes were extracted from the edge data using a Set to ensure uniqueness.
Edges were stored as objects with from, to, distance, time, and fuel properties.
Node positions were assigned as relative ratios (0–1) of the canvas size, placing cities in an approximate geographic layout.
Duplicate bidirectional pairs were removed using a canonical key ([A,B].sort().join('|')) so each pair renders as one undirected line.
Nodes render as glowing circles with full name labels. Edge labels show distance in km.
Nodes are draggable. Hovering a node shows total connections; hovering an edge shows all three weight values.

Part 2 — Shortest Path
The graph is treated as undirected — both directions of travel are available on every edge. The adjacency list was built by adding both the forward and reverse direction of each edge. The user selects an origin and destination node and clicks Calculate Path. The algorithm outputs the optimal route with totals for distance, time, and fuel, and highlights the path on the canvas.

4. Algorithm Used — Dijkstra's Algorithm
Dijkstra's Algorithm was chosen because:

All edge weights are positive (distance, time, and fuel are all > 0)
The graph is small (8 nodes, 12 edges) so performance is not a concern
It guarantees the globally optimal shortest path from source to destination

How it works:

Set all node distances to Infinity except the start node (distance = 0)
Use a priority queue (array sorted by cost) to always process the lowest-cost unvisited node next
For each neighbor, compute the tentative cost — if it improves the known distance, update it and record the previous node
Stop once the destination node is popped from the queue
Reconstruct the path by walking backwards through the prev map from destination to source

The result includes the full ordered path plus the sum of distance, time, and fuel across all edges used.

5. Challenges Faced
Directed vs. Undirected Graph
The original table lists some routes in both directions as separate rows. Initially the graph was directed, which caused Dijkstra to miss valid shorter paths. The fix was to make the adjacency list bidirectional and deduplicate edges visually so the canvas did not draw overlapping lines.
Node Label Clipping
Longer names like NOVELETA and BACOOR were initially truncated because the node radius was too small. This was fixed by increasing the radius to 36px and scaling the font size based on name length so all names fit on a single line inside the circle.
Edge Overlap with Bidirectional Pairs
When directed, pairs like BACOOR↔SILANG were drawn as two curved arcs. After switching to undirected, the drawing was simplified to a single straight line per pair, removing visual clutter.
Inaccurate Node Count in Results
The results panel initially showed a "Hops" metric counting edges instead of nodes. This was corrected to display path.length (total nodes visited) instead of path.length - 1.
Canvas Responsiveness
Node positions were initially stored as absolute pixel coordinates, causing them to stay in place when the browser was resized. This was fixed by storing positions as relative ratios and recomputing pixel positions on every canvas resize event.

6. Conclusion
The program successfully fulfills both parts of the assignment. The node map provides a clear interactive visualization of the Calabarzon route network with all 8 nodes and 12 unique connections. The shortest path finder uses Dijkstra's Algorithm to compute the optimal route between any two selected nodes, displaying the full path and all relevant metrics. The program is delivered as a single self-contained HTML file that runs directly in any modern browser without any installation or external dependencies.