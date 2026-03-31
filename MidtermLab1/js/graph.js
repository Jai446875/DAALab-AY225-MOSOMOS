/**
 * Graph Data Structure and Dijkstra's Shortest Path Algorithm
 * 
 * Time Complexity: O((V + E) log V) with min-heap priority queue
 * Space Complexity: O(V) for distance array and visited set
 */

// MinHeap is used as a priority queue in Dijkstra's algorithm
// It always gives the node with the smallest cost next (min-priority)
class MinHeap {
    constructor() {
        // Internal array that stores heap elements as { node, priority } objects
        this.heap = [];
    }

    // Insert a new node with a given priority into the heap
    // After inserting at the end, bubble it up to restore heap order
    push(node, priority) {
        this.heap.push({ node, priority });
        this.bubbleUp(this.heap.length - 1); // Fix heap order from the new element upward
    }

    // Remove and return the element with the lowest priority (the root)
    pop() {
        if (this.heap.length === 0) return null; // Empty heap — nothing to return
        if (this.heap.length === 1) return this.heap.pop(); // Only one element — just remove it

        const min = this.heap[0]; // Save the root (minimum) to return later

        // Move the last element to the root position, then shrink the array
        this.heap[0] = this.heap.pop();

        // Restore heap order by sinking the new root down to its correct position
        this.bubbleDown(0);
        return min;
    }

    // Move element at index idx upward until heap order is restored
    // A child must always have priority >= its parent (min-heap property)
    bubbleUp(idx) {
        while (idx > 0) {
            const parentIdx = Math.floor((idx - 1) / 2); // Parent index in a binary heap
            if (this.heap[idx].priority >= this.heap[parentIdx].priority) break; // Already in order — stop

            // Swap child and parent because child has lower priority (higher importance)
            [this.heap[idx], this.heap[parentIdx]] = [this.heap[parentIdx], this.heap[idx]];
            idx = parentIdx; // Move up and continue checking
        }
    }

    // Move element at index idx downward until heap order is restored
    // Swaps with the smaller child to maintain min-heap property
    bubbleDown(idx) {
        while (true) {
            let smallest = idx;              // Assume current node is the smallest
            const leftChild = 2 * idx + 1;  // Left child index in a binary heap
            const rightChild = 2 * idx + 2; // Right child index in a binary heap

            // If left child exists and is smaller than current smallest, update
            if (leftChild < this.heap.length && 
                this.heap[leftChild].priority < this.heap[smallest].priority) {
                smallest = leftChild;
            }

            // If right child exists and is smaller than current smallest, update
            if (rightChild < this.heap.length && 
                this.heap[rightChild].priority < this.heap[smallest].priority) {
                smallest = rightChild;
            }

            if (smallest === idx) break; // Current node is already smallest — heap is valid, stop

            // Swap current node with the smallest child and continue sinking
            [this.heap[idx], this.heap[smallest]] = [this.heap[smallest], this.heap[idx]];
            idx = smallest;
        }
    }

    // Returns true if the heap has no elements
    isEmpty() {
        return this.heap.length === 0;
    }
}

// Graph represents a weighted directed graph using an adjacency list
// Each edge stores three separate weights: distance, time, and fuel
class Graph {
    constructor() {
        // adjacencyList maps each node to an array of its outgoing edges
        this.adjacencyList = new Map();

        // nodes is a Set of all unique node IDs encountered via addEdge()
        this.nodes = new Set();
    }

    /**
     * Add an edge to the graph
     * @param {number} from - Source node
     * @param {number} to - Destination node
     * @param {object} weights - Object containing distance, time, fuel
     */
    addEdge(from, to, weights) {
        // Register both endpoints as known nodes
        this.nodes.add(from);
        this.nodes.add(to);

        // Create an empty adjacency list entry for this source node if it doesn't exist yet
        if (!this.adjacencyList.has(from)) {
            this.adjacencyList.set(from, []);
        }

        // Append the edge — the neighbor object stores the destination and all three weights
        this.adjacencyList.get(from).push({
            node: to,
            distance: weights.distance,
            time: weights.time,
            fuel: weights.fuel
        });
    }

    /**
     * Get all nodes in the graph
     * @returns {Array} Array of node identifiers
     */
    getNodes() {
        // Convert the Set to an array and sort numerically so output is consistent
        return Array.from(this.nodes).sort((a, b) => a - b);
    }

    /**
     * Dijkstra's shortest path algorithm
     * @param {number} source - Starting node
     * @param {string} metric - Weight metric to use ('distance', 'time', or 'fuel')
     * @returns {object} Object containing distances and previous nodes for path reconstruction
     */
    dijkstra(source, metric) {
        const distances = new Map(); // Shortest known cost from source to each node
        const previous = new Map();  // Tracks the node we came from on the shortest path (for reconstruction)
        const visited = new Set();   // Nodes whose shortest path has been finalized
        const pq = new MinHeap();    // Priority queue — always processes the cheapest unvisited node next

        // Initialize all nodes with infinite cost and no predecessor
        for (const node of this.nodes) {
            distances.set(node, Infinity);
            previous.set(node, null);
        }
        // Source node costs 0 to reach from itself
        distances.set(source, 0);
        pq.push(source, 0); // Seed the queue with the source node

        while (!pq.isEmpty()) {
            const { node: current } = pq.pop(); // Get the unvisited node with the lowest cost

            if (visited.has(current)) continue; // Already finalized — skip (stale entry in the heap)
            visited.add(current);               // Mark as finalized

            // Check neighbors
            const neighbors = this.adjacencyList.get(current) || []; // Default to [] if no outgoing edges
            for (const neighbor of neighbors) {
                if (visited.has(neighbor.node)) continue; // Skip already-finalized neighbors

                // Use the selected metric (distance / time / fuel) as the edge weight
                const weight = neighbor[metric];
                const newDistance = distances.get(current) + weight; // Tentative cost via current node

                // If this path is cheaper than the previously known cost, update and re-queue
                if (newDistance < distances.get(neighbor.node)) {
                    distances.set(neighbor.node, newDistance);
                    previous.set(neighbor.node, current); // Record how we got here
                    pq.push(neighbor.node, newDistance);  // Push updated cost into the priority queue
                }
            }
        }

        return { distances, previous };
    }

    /**
     * Reconstruct the shortest path from source to target
     * @param {Map} previous - Previous nodes map from Dijkstra
     * @param {number} source - Starting node
     * @param {number} target - Destination node
     * @returns {Array|null} Path as array of nodes, or null if no path exists
     */
    reconstructPath(previous, source, target) {
        const path = [];
        let current = target;

        // Walk backwards from target to source using the previous[] map
        while (current !== null) {
            path.unshift(current); // Prepend so the final array reads source → ... → target
            current = previous.get(current);
        }

        // If path[0] is not the source, the target was unreachable (the walk never reached source)
        if (path[0] !== source) {
            return null;
        }

        return path;
    }

    /**
     * Find shortest paths from source to all other nodes
     * @param {number} source - Starting node
     * @param {string} metric - Weight metric to use ('distance', 'time', or 'fuel')
     * @returns {Array} Array of path objects with destination, path, and cost
     */
    findAllShortestPaths(source, metric) {
        // Run Dijkstra once from the source to get costs and predecessors for all nodes
        const { distances, previous } = this.dijkstra(source, metric);
        const results = [];

        for (const target of this.nodes) {
            if (target === source) continue; // Skip self — no path needed from a node to itself

            const path = this.reconstructPath(previous, source, target);
            const cost = distances.get(target);

            results.push({
                destination: target,
                path: path,
                cost: cost === Infinity ? null : cost, // Represent unreachable nodes as null cost
                reachable: path !== null && cost !== Infinity // true only if a valid path exists
            });
        }

        // Sort results by destination node ID for consistent display order
        results.sort((a, b) => a.destination - b.destination);

        return results;
    }

    /**
     * Compute shortest paths from all nodes to all other nodes for a given metric
     * @param {string} metric - Weight metric to use ('distance', 'time', or 'fuel')
     * @returns {Map} Map of source node -> array of path results
     */
    computeAllPairsShortestPaths(metric) {
        const allPaths = new Map(); // Stores results keyed by source node

        // Run findAllShortestPaths once per source node (one full Dijkstra per source)
        for (const source of this.nodes) {
            const paths = this.findAllShortestPaths(source, metric);
            allPaths.set(source, paths);
        }

        return allPaths;
    }
}

// Export the Graph class for use in Node.js environments
// In a browser context (no module system), this block is safely skipped
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { Graph };
}