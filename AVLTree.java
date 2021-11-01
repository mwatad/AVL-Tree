
/**
 *
 * AVLTree
 *
 * An implementation of an AVL Tree with distinct integer keys and info
 *
 * @author Muhammad Watad
 * @author Shadi Abu Shqara
 * 
 */
public class AVLTree {
	
	/**
	 * Constants:
	 * VIRTUAL: key for virtual node
	 * INSERT & DELETE: parameters for balance. They are used in order to know
	 * if we should apply 1 rotation or continue.
	 */
	private static final int VIRTUAL = -1;
	private static final int INSERT = 1;
	private static final int DELETE = 2;

	private IAVLNode root;
	private IAVLNode min;
	private IAVLNode max;

	/**
	 * public boolean empty()
	 *
	 * returns true if and only if the tree is empty
	 * 
	 * Time complexity: O(1)
	 */
	public boolean empty() {
		return root == null;
	}

	/**
	 * public String search(int k)
	 *
	 * returns the info of an item with key k if it exists in the tree otherwise,
	 * returns null
	 * 
	 * Time complexity: O(logn)
	 */
	public String search(int k) {
		return search(root, k);
	}

	/**
	 * private String search(IAVLNode node, int key)
	 * 
	 * This recursive function returns the info of an item with key k if it exists
	 * in the tree otherwise, returns null.
	 * 
	 * Time complexity: O(logn)
	 * 
	 */
	private String search(IAVLNode node, int key) {

		if (node == null)
			return null;

		if (key == node.getKey())
			return node.getValue();

		if (key > node.getKey())
			return search(node.getRight(), key);

		return search(node.getLeft(), key);
	}

	/**
	 * public int insert(int k, String i)
	 *
	 * inserts an item with key k and info i to the AVL tree. the tree must remain
	 * valid (keep its invariants). returns the number of rebalancing operations, or
	 * 0 if no rebalancing operations were necessary. returns -1 if an item with key
	 * k already exists in the tree.
	 * 
	 * Time complexity: O(logn)
	 */
	public int insert(int k, String i) {
		if (empty()) {
			root = new AVLNode(i, k);
			root.setLeft(new AVLNode(null, VIRTUAL));
			root.getLeft().setParent(root);
			root.setRight(new AVLNode(null, VIRTUAL));
			root.getRight().setParent(root);
			root.setParent(null);
			root.setSubtreeSize(1);
			root.setHeight(0);
			return 0;
		}

		// Find where the new node should be inserted
		IAVLNode ptr = root;
		boolean rightChild = false;
		while (ptr.isRealNode() && ptr.getKey() != k) {
			if (k > ptr.getKey()) {
				ptr = ptr.getRight();
				rightChild = true;
			} else if (k < ptr.getKey()) {
				ptr = ptr.getLeft();
				rightChild = false;
			}
		}

		// k already exists
		if (ptr.isRealNode())
			return -1;

		// Find the new node's parent (before rotating)
		IAVLNode parent = ptr.getParent();
		// insert (k, i) to the tree
		put(parent, rightChild, k, i);
		// balance tree. (use INSERT mode for balance)
		int numOfRotations = balance(parent, INSERT);
		// update max & min
		updateMax();
		updateMin();
		return numOfRotations;
	}
	
	
	/**
	 * private void put(IAVLNode parent, boolean rightChild, int k, String i)
	 * 
	 * This method creates a new node when k is the key and i is the info
	 * and connects the node to parent. If rightChild is true then the new
	 * node is connected as a right child of parent, otherwise as a left child.
	 * 
	 * @pre parent is a leaf
	 * 
	 * Time complexity: O(logn)
	 */
	private void put(IAVLNode parent, boolean rightChild, int k, String i) {

		// Create and initialize the new node
		AVLNode node = new AVLNode(i, k);
		node.setParent(parent);
		node.setSubtreeSize(1);
		node.setHeight(0);

		// Check where it should be connected
		if (rightChild) {
			node.setRight(parent.getRight());
			node.setLeft(new AVLNode(null, VIRTUAL));
			parent.setRight(node);
		} else {
			node.setLeft(parent.getLeft());
			node.setRight(new AVLNode(null, VIRTUAL));
			parent.setLeft(node);
		}

		// connect parents' pointers
		node.getRight().setParent(node);
		node.getLeft().setParent(node);

		// Buttom-up update of size and keys' sum. 
		updateSizesKeysSumButtomUp(parent);

	}
	
	

	/**
	 * private int balance(IAVLNode node, int mode)
	 * 
	 * This method balances the tree after insertion or deletion.
	 * For insertion use mode = INSERT
	 * For deletion use mode = DELETE
	 * node is the parent of the node which was inserted
	 * or the parent of the node which was truly deleted.
	 * 
	 * It returns the number of rotations that were applied when
	 * LL & RR are considered 1 rotation while LR & RL are two rotations.
	 * 
	 * Time complexity: O(logn)
	 */
	private int balance(IAVLNode node, int mode) {

		IAVLNode ptr = node;
		int rotations = 0;

		// Apply the balancing algorithm
		while (ptr != null) {

			int bf = ptr.calculateBF();
			int newHeight = ptr.calculateHeight();
			if (Math.abs(bf) < 2 && newHeight == ptr.getHeight())
				return rotations;
			if (Math.abs(bf) < 2 && newHeight != ptr.getHeight()) {
				ptr.setHeight(newHeight);
				ptr = ptr.getParent();
				continue;
			}

			ptr.setHeight(newHeight);
			
			// remember ptr's parent
			IAVLNode parent = ptr.getParent();
			//rotate
			rotations += rotate(ptr);

			if (mode == INSERT)
				return rotations;
			
			if (mode == DELETE) {
				ptr = parent;
				continue;
			}
		}

		return rotations;
	}

	/**
	 * private int rotate(IAVLNode node)
	 * 
	 * This method calls the appropriate rotate according to the BF values.
	 * It returns the number of rotations that were applied:
	 * 2 for RL & LR
	 * 1 for LL & RR
	 * 
	 * Time complexity: O(1)
	 */
	private int rotate(IAVLNode node) {

		int bf = node.calculateBF();

		if (bf == 2) {
			if (node.getLeft().calculateBF() == 1 || node.getLeft().calculateBF() == 0) {
				LL(node);
				return 1;
			}
			if (node.getLeft().calculateBF() == -1) {
				LR(node);
				return 2;
			}
		}

		if (bf == -2) {
			if (node.getRight().calculateBF() == 1) {
				RL(node);
				return 2;
			}
			if (node.getRight().calculateBF() == -1 || node.getRight().calculateBF() == 0) {
				RR(node);
				return 1;
			}
		}
		
		// should NOT reach this point
		return 0;
	}

	/**
	 * private void LL(IAVLNode node)
	 * 
	 * Apply Left-Left rotation
	 * 
	 * Time complexity: O(1)
	 */
	private void LL(IAVLNode node) {
		rightRotate(node);
	}
	
	/**
	 * private void RR(IAVLNode node)
	 * 
	 * Apply Right-Right rotation
	 * 
	 * Time complexity: O(1)
	 */
	private void RR(IAVLNode node) {
		leftRotate(node);
	}
	
	/**
	 * private void LR(IAVLNode node)
	 * 
	 * Apply Left-Right rotation
	 * 
	 * Time complexity: O(1)
	 */
	private void LR(IAVLNode node) {
		leftRotate(node.getLeft());
		rightRotate(node);
	}

	/**
	 * private void RL(IAVLNode node)
	 * 
	 * Apply Right-Left rotation
	 * 
	 * Time complexity: O(1)
	 */
	private void RL(IAVLNode node) {
		rightRotate(node.getRight());
		leftRotate(node);
	}
	
	
	/**
	 * private void rightRotate(IAVLNode a)
	 * 
	 * This method Applies right rotation as follows:
	 * 
	 *            a                  b
	 *           / \                / \
	 *          b   c      =>      d   a  
	 *         / \                    / \
	 *        d   e                  e   c
	 * 
	 * Time complexity: O(1)
	 */
	private void rightRotate(IAVLNode a) {

		IAVLNode b = a.getLeft();
		IAVLNode e = b.getRight();
		// if a is NOT the root then b should be connected to a's parent
		if (a != root) {
			if (isRight(a))
				a.getParent().setRight(b);
			else // a is a left child
				a.getParent().setLeft(b);
		}

		// connect nodes
		a.setLeft(e);
		b.setRight(a);
		b.setParent(a.getParent());
		a.setParent(b);
		e.setParent(a);

		// if a was the root then b should replace it
		if (root == a) {
			root = b;
			root.setParent(null);
		}

		// update high, size and keys' sum for a
		updateHeightSize(a);
		a.setKeysSum(a.calculateKeysSum());
		// update high, size and keys' sum for b
		updateHeightSize(b);
		b.setKeysSum(b.calculateKeysSum());

	}

	/**
	 * private void leftRotate(IAVLNode a)
	 * 
	 * This method Applies left rotation as follows:
	 * 
	 *            a					 b
	 *           / \			    / \
	 *          c   b      =>      a   e
	 *             / \			  / \
	 *            d   e          c   d
	 * 
	 * Time complexity: O(1)
	 */
	private void leftRotate(IAVLNode a) {

		IAVLNode b = a.getRight();
		IAVLNode bLeft = b.getLeft();
		// if a is NOT the root then b should be connected to a's parent
		if (a != root) {
			if (isRight(a))
				a.getParent().setRight(b);
			else // a is a left child
				a.getParent().setLeft(b);
		}

		// connect nodes
		a.setRight(bLeft);
		b.setLeft(a);
		b.setParent(a.getParent());
		a.setParent(b);
		bLeft.setParent(a);

		// if a was the root then b should replace it
		if (root == a) {
			root = b;
			root.setParent(null);
		}

		// update high, size and keys' sum for a
		updateHeightSize(a);
		a.setKeysSum(a.calculateKeysSum());
		// update high, size and keys' sum for b
		updateHeightSize(b);
		b.setKeysSum(b.calculateKeysSum());

	}
	
	
	/**
	 * public int delete(int k)
	 *
	 * deletes an item with key k from the binary tree, if it is there; the tree
	 * must remain valid (keep its invariants). returns the number of rebalancing
	 * operations, or 0 if no rebalancing operations were needed. returns -1 if an
	 * item with key k was not found in the tree.
	 * 
	 * Time complexity: O(logn)
	 */
	public int delete(int k) {
				
		if(empty())
			return -1;

		// Find the node to be deleted
		IAVLNode ptr = root;
		while (ptr.isRealNode() && ptr.getKey() != k) {
			if (k > ptr.getKey())
				ptr = ptr.getRight();
			else if (k < ptr.getKey())
				ptr = ptr.getLeft();

		}
		
		// k doesn't exist in the tree
		if (!ptr.isRealNode())
			return -1;

		// ptr points to the node to be deleted
		
		// If the root is a leaf and we are trying to delete it
		if (ptr == root && ptr.isLeaf()) {
			root = null;
			return 0;
		}
		
		// If we are trying to delete a node which has two children
		// then swap node with it's successor
		if (ptr.hasTwoChildren()) {
			IAVLNode y = ptr.findSuccessor();
			swapNodes(y, ptr);
			ptr = y;
		}
		
		// If we are trying to delete the root
		if(ptr == root) {
			root = ptr.getChild();
			root.setParent(null);
			updateMin();
			updateMax();
			return 0;
		}
		
		IAVLNode parent = ptr.getParent();
		// ptr now is a leaf or have only one children
		if (isRight(ptr)) { // ptr is a right child
			IAVLNode node = ptr.getChild();
			node.setParent(parent);
			parent.setRight(node);
			updateSizesKeysSumButtomUp(parent);
		}

		else { // ptr is a left child
			IAVLNode node = ptr.getChild();
			node.setParent(parent);
			parent.setLeft(node);
			updateSizesKeysSumButtomUp(parent);
		}
		
		// Apply rotations
		int numOfRotations = balance(parent, DELETE);
		
		// update max & min
		updateMax();
		updateMin();
		
		return numOfRotations;
		
	}
	
	

	/**
	 * private boolean isRight(IAVLNode node)
	 * 
	 * precondition: node is NOT the root
	 * 
	 * return value :
	 * true  : if node is a right child
	 * false : if node is a left child
	 * 
	 * Time complexity: O(1)
	 */
	private boolean isRight(IAVLNode node) {
		return node.getParent().getRight() == node;
	}
	
	/**
	 * private void updateHeightSize(IAVLNode node)
	 * 
	 * This method updates the height and size of a given node.
	 * 
	 * Time complexity: O(1)
	 */
	private void updateHeightSize(IAVLNode node) {
		node.setHeight(node.calculateHeight());
		node.setSubtreeSize(node.calculateSize());
	}

	
	/**
	 * private void updateSizesKeysSumButtomUp(IAVLNode node)
	 * 
	 * Buttom-up update of size and keys' sum starting from node.
	 * 
	 * Time complexity: O(logn) 
	 */
	private void updateSizesKeysSumButtomUp(IAVLNode node) {

		IAVLNode ptr = node;

		while (ptr != null) {
			ptr.setSubtreeSize(ptr.calculateSize());
			ptr.setKeysSum(ptr.calculateKeysSum());
			ptr = ptr.getParent();
		}

	}



	/**
	 * private void updateMin()
	 * 
	 * update min so it points to the node with the minimal key
	 * or null if the tree is empty.
	 * 
	 * Time complexity: O(logn)
	 */
	private void updateMin() {
		
		if (empty()) {
			min = null;
			return;
		}
		
		IAVLNode ptr = root;
		
		while(ptr.isRealNode())
			ptr = ptr.getLeft();
		
		min = ptr.getParent();
		
	}

	/**
	 * private void updateMax()
	 * 
	 * update max so it points to the node with the maximal key
	 * or null if the tree is empty.
	 * 
	 * Time complexity: O(logn)
	 */
	private void updateMax() {
		
		if (empty()) {
			max = null;
			return;
		}
		
		IAVLNode ptr = root;
		
		while(ptr.isRealNode())
			ptr = ptr.getRight();
		
		max = ptr.getParent();
		
	}

	/**
	 * private void swapNodes(IAVLNode y, IAVLNode ptr)
	 * 
	 * This method swaps nodes y & ptr
	 * y.key   <=>  ptr.key
	 * y.info  <=>  ptr.info
	 * 
	 * Time complexity: O(1)
	 */
	private void swapNodes(IAVLNode y, IAVLNode ptr) {
		
		int tmpKey = y.getKey();
		String tmpStr = y.getValue();
		
		y.setKey(ptr.getKey());
		y.setValue(ptr.getValue());
		
		ptr.setKey(tmpKey);
		ptr.setValue(tmpStr);

	}

	/**
	 * public String min()
	 *
	 * Returns the info of the item with the smallest key in the tree, or null if
	 * the tree is empty
	 * 
	 * Time complexity: O(1)
	 */
	public String min() {
		return empty() ? null : min.getValue();
	}

	/**
	 * public String max()
	 *
	 * Returns the info of the item with the largest key in the tree, or null if the
	 * tree is empty
	 * 
	 * Time complexity: O(1)
	 */
	public String max() {
		return empty() ? null : max.getValue();
	}

	/**
	 * public int[] keysToArray()
	 *
	 * Returns a sorted array which contains all keys in the tree, or an empty array
	 * if the tree is empty.
	 * 
	 * Time complexity: O(n)
	 */
	public int[] keysToArray() {
		
		if(empty())
			return new int[0];
		
		int[] res = new int[size()];
		buildKeys(root, res, 0);
		
		return res;
	}


	/**
	 * private void buildKeys(IAVLNode node, int[] arr, int i)
	 * 
	 * This recursive in-order traversal writes the keys of the sub-tree
	 * whose root is node to arr starting from index i.
	 * 
	 * Time complexity: O(n)
	 */
	private void buildKeys(IAVLNode node, int[] arr, int i) {
		
		if(!node.isRealNode())
			return;
		
		buildKeys(node.getLeft(), arr, i);
		arr[node.getLeft().getSubtreeSize() + i] = node.getKey();
		buildKeys(node.getRight(), arr, node.getLeft().getSubtreeSize() + i + 1);
		
	}

	/**
	 * public String[] infoToArray()
	 *
	 * Returns an array which contains all info in the tree, sorted by their
	 * respective keys, or an empty array if the tree is empty.
	 * 
	 * Time complexity: O(n)
	 */
	public String[] infoToArray() {
		
		if(empty())
			return new String[0];
		
		String[] res = new String[size()];
		buildInfo(root, res, 0);
		
		return res;
		
	}
	
	/**
	 * private void buildInfo(IAVLNode node, String[] arr, int i)
	 * 
	 * This recursive in-order traversal writes the values of the sub-tree
	 * whose root is node to arr starting from index i.
	 * 
	 * Time complexity: O(n)
	 */

	private void buildInfo(IAVLNode node, String[] arr, int i) {
		
		if(!node.isRealNode())
			return;
		
		buildInfo(node.getLeft(), arr, i);
		arr[node.getLeft().getSubtreeSize() + i] = node.getValue();
		buildInfo(node.getRight(), arr, node.getLeft().getSubtreeSize() + i + 1);
		
	}	// For testing only
	////


	/**
	 * public int size()
	 *
	 * Returns the number of nodes in the tree.
	 *
	 * precondition: none postcondition: none
	 * 
	 * Time complexity: O(1)
	 */
	public int size() {
		
		if(empty())
			return 0;
		
		return root.getSubtreeSize();
	}
	

	/**
	 * public int getRoot()
	 *
	 * Returns the root AVL node, or null if the tree is empty
	 *
	 * precondition: none postcondition: none
	 * 
	 * Time complexity: O(1)
	 */
	public IAVLNode getRoot() {
		return root;
	}

	/**
	 * public string select(int i)
	 *
	 * Returns the value of the i'th smallest key (return null if tree is empty)
	 * Example 1: select(1) returns the value of the node with minimal key Example
	 * 2: select(size()) returns the value of the node with maximal key Example 3:
	 * select(2) returns the value 2nd smallest minimal node, i.e the value of the
	 * node minimal node's successor
	 *
	 * precondition: size() >= i > 0 postcondition: none
	 * 
	 * Time complexity: O(logi)
	 */
	public String select(int i) {
		
		if(empty() || size() < i || i <= 0)
			return null;
		
		IAVLNode ptr = min;
		// Going up to the first node with size >= i
		while(ptr.getSubtreeSize() < i)
			ptr = ptr.getParent();
		
		IAVLNode res = select(ptr, i);
		return res.getValue();
		
	}
	
	
	/**
	 * private IAVLNode select(IAVLNode ptr, int i)
	 * 
	 * This method applies the select algorithm.
	 * It returns the i'th node of the tree starting from ptr.
	 * 
	 * Time complexity: O(logi)
	 */
	private IAVLNode select(IAVLNode ptr, int i) {
		
		int r = ptr.getLeft().getSubtreeSize() + 1;
		
		if(i == r)
			return ptr;
		
		if(i < r)
			return select(ptr.getLeft(), i);
		
		return select(ptr.getRight(), i-r);
		
	}

	/**
	 * public int less(int i)
	 *
	 * Returns the sum of all keys which are less or equal to i i is not
	 * neccessarily a key in the tree
	 *
	 * precondition: none postcondition: none
	 * 
	 * Time complexity: O(logn)
	 */
	public int less(int i) {
		
		if(empty())
			return 0;
		
		int sum = root.getKeysSum();
		IAVLNode ptr = root;
		while(ptr.getKey() != i && ptr.isRealNode()) {
			// do nothing if we go right
			if(i > ptr.getKey())
				ptr = ptr.getRight();
			// subtract key and sum of right tree if we go left
			else {
				sum = sum - ptr.getRight().getKeysSum() - ptr.getKey();
				ptr = ptr.getLeft();
			}
		}
		// if we found i in the tree then subtract the sum
		// of the right tree
		if(ptr.getKey() == i) {
			sum = sum - ptr.getRight().getKeysSum();
		}
		return sum;
		
	}
	

	/**
	 * public interface IAVLNode ! Do not delete or modify this - otherwise all
	 * tests will fail !
	 */
	public interface IAVLNode {

		public int getKey(); // returns node's key (for virtual node return -1)
			
		public void setKey(int key); // sets key
		
		public String getValue(); // returns node's value [info] (for virtual node return null)
		
		public void setValue(String value); // sets value

		public IAVLNode getLeft(); // returns left child (if there is no left child return null)
		
		public void setLeft(IAVLNode node); // sets left child
		
		public IAVLNode getRight(); // returns right child (if there is no right child return null)
		
		public void setRight(IAVLNode node); // sets right child
		
		public IAVLNode getParent(); // returns the parent (if there is no parent return null)
		
		public void setParent(IAVLNode node); // sets parent
		
		public int getHeight(); // Returns the height of the node (-1 for virtual nodes)
		
		public void setHeight(int height); // sets the height of the node
		
		public int calculateHeight(); // Calculate the height of a node
		
		public int calculateBF(); // Get balance factor of the node
		
		public int getKeysSum(); // returns key's sum of the tree starting from this
		
		public void setKeysSum(int keysSum); // sets keys' sum
		
		public int calculateKeysSum(); // calculates keys' sum
		
		public int getSubtreeSize(); // Returns the number of real nodes in this node's subtree (Should be
									 // implemented in O(1))
		
		public void setSubtreeSize(int size); // sets the number of real nodes in this node's subtree
		
		public int calculateSize(); // Calculate the size of a the subtree starting from the current node

		public IAVLNode findSuccessor(); // find the node's successor

		public IAVLNode getChild(); // gets the real child
		
		public boolean hasTwoChildren(); // check if node has two children

		public boolean isLeaf(); // Check if the node is a leaf

		public boolean isRealNode(); // Returns True if this is a non-virtual AVL node

	}

	/**
	 * public class AVLNode
	 *
	 * If you wish to implement classes other than AVLTree (for example AVLNode), do
	 * it in this file, not in another file. This class can and must be modified.
	 * (It must implement IAVLNode)
	 */
	public class AVLNode implements IAVLNode {

		private String info;
		private int key;
		private IAVLNode left;
		private IAVLNode right;
		private IAVLNode parent;
		private int size;
		private int height;
		private int keysSum;

		// Time complexity: O(1) for all of the methods
		
		public AVLNode(String info, int key) {
			this.info = info;
			this.key = key;
			this.keysSum = key;
			if (key == VIRTUAL) {
				this.height = -1;
				this.keysSum = 0;
			}
		}
		
		public int getKey() {
			return key;
		}

		public String getValue() {
			return info;
		}

		public void setLeft(IAVLNode node) {
			this.left = node;
		}

		public IAVLNode getLeft() {
			return left;
		}

		public void setRight(IAVLNode node) {
			this.right = node;
		}

		public IAVLNode getRight() {
			return right;
		}

		public void setParent(IAVLNode node) {
			this.parent = node;
		}

		public IAVLNode getParent() {
			return parent;
		}

		// Returns True if this is a non-virtual AVLNode
		public boolean isRealNode() {
			return key != VIRTUAL;
		}

		public void setSubtreeSize(int size) {
			this.size = size;
		}

		public int getSubtreeSize() {
			return size;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getHeight() {
			return height;
		}

		public int calculateBF() {

			if(!isRealNode())
				return 0;
			
			return left.getHeight() - right.getHeight();
		}

		public int calculateHeight() {
			int rHeight = right.getHeight();
			int lHeight = left.getHeight();
			return Math.max(rHeight, lHeight) + 1;
		}

		public int calculateSize() {
			int rSize = right.getSubtreeSize();
			int lSize = left.getSubtreeSize();
			return rSize + lSize + 1;
		}

		public boolean isLeaf() {
			return !right.isRealNode() && !left.isRealNode();
		}

		public IAVLNode findSuccessor() {

			IAVLNode ptr = right;

			while (ptr.isRealNode()) {
				ptr = ptr.getLeft();
			}

			return ptr.getParent();

		}

		public boolean hasTwoChildren() {
			return right.isRealNode() && left.isRealNode();
		}

		public IAVLNode getChild() {
			if (right.isRealNode())
				return right;
			return left;
		}

		public void setValue(String value) {
			info = value;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public void setKeysSum(int keysSum) {
			this.keysSum = keysSum;
		}

		public int calculateKeysSum() {
			return key + left.getKeysSum() + right.getKeysSum();
		}

		public int getKeysSum() {
			return keysSum;
		}

	}

}
