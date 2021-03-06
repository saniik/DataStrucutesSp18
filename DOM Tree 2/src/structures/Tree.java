package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 *
 */
public class Tree {

	/**
	 * Root node
	 */
	TagNode root=null;

	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;

	/**
	 * Initializes this tree object with scanner for input HTML file
	 *
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}

	/**
	 * Builds the DOM tree from input HTML file, through scanner passed
	 * in to the constructor and stored in the sc field of this object. 
	 *
	 * The root of the tree that is built is referenced by the root field of this object.
	 */
	public void build() {

		root = build2();

	}
	private TagNode build2() {
		int length;
		String line = null;
		boolean domlines = sc.hasNextLine();

		if (domlines == true) {
			line = sc.nextLine();
		} else {
			return null;
		}

		length = line.length();
		boolean child = false;

		if (line.charAt(0) == '<') {
			line = line.substring(1, length - 1);
			if (line.charAt(0) == '/') {
				return null;
			} else {
				child = true;
			}
		}

		TagNode temp = new TagNode (line, null, null);
		if(child == true) {
			temp.firstChild = build2();
		}
		temp.sibling = build2();
		return temp;
	}

	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 *
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		swapTag(root, oldTag, newTag);
	}

	private void swapTag(TagNode tempRoot, String old, String newer) {
		TagNode current = tempRoot;
		if (current == null) {
			return;
		}
		System.out.println("TempRoot: " + tempRoot);

		if (current.tag.equals(old)) {
			System.out.println("Replacing "+ tempRoot.tag + " with " + newer);
			current.tag = newer;

		}
		System.out.println("TempRoot sibling:" + tempRoot.sibling);
		System.out.println("TempRoot child: " + tempRoot.firstChild);
		swapTag(tempRoot.firstChild, old, newer);
		swapTag(tempRoot.sibling, old, newer);

	}

	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 *
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		TagNode current = new TagNode(null, null, null);
		TagNode temp;
		current = boldrow2(root);
		if (current == null) {
			System.out.println("No table");
			return;
		}
		current = current.firstChild;

		//rows of table
		for(int i = 1; i < row; i++) {
			current = current.sibling;
		}

		//columns of table
		for (temp = current.firstChild; temp != null; temp = temp.sibling) {
			temp.firstChild = new TagNode("b", temp.firstChild, null);

		}

	}

	private TagNode boldrow2(TagNode current) {
		if (current == null)
			return null;

		TagNode tempNode = null;
		String temp = current.tag;

		if(temp.equals("table")) {
			tempNode = current;
			return tempNode;
		}

		if(tempNode == null) {
			tempNode = boldrow2(current.firstChild);
		}

		if(tempNode == null) {
			tempNode = boldrow2(current.sibling);
		}
		return tempNode;
	}



	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 *
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		/** COMPLETE THIS METHOD **/
	}

	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 *
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		/** COMPLETE THIS METHOD **/
	}

	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 *
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}

	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");
			}
		}
	}

	/**
	 * Prints the DOM tree. 
	 *
	 */
	public void print() {
		print(root, 1);
	}

	private void print(TagNode root, int level) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			for (int i=0; i < level-1; i++) {
				System.out.print("      ");
			};
			if (root != this.root) {
				System.out.print("|---- ");
			} else {
				System.out.print("      ");
			}
			System.out.println(ptr.tag);
			if (ptr.firstChild != null) {
				print(ptr.firstChild, level+1);
			}
		}
	}
}
