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

		root = recursiveBuild();

	}

	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		recursiveReplace(root, oldTag, newTag);
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
		current = recursiveBold(root);
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

		
	
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		if((tag.equals("p") || tag.equals("em") || tag.equals("b"))){
			recursiveRemoveSimple(root, tag);
		}
		if((tag.equals("ol") || tag.equals("ul"))){
			recursiveRemoveComplicated(root, tag);
		}
	}

	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		if (tag.equals("em") || tag.equals("b")) {
			recursiveAdd(root, word.toLowerCase(), tag);
		}
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
			}
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

	/////////////HELPER METHODS (MINE)////////////

	private TagNode recursiveBuild() {
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
		if (child == true) {
			temp.firstChild = recursiveBuild();
		}
		temp.sibling = recursiveBuild();
		return temp;
	}

	private void recursiveReplace(TagNode tempRoot, String old, String newer) {
		TagNode current = tempRoot;
		if (current == null) {
			return;
		}
		if (current.tag.equals(old)) {
			current.tag = newer;
		}
		recursiveReplace(tempRoot.firstChild, old, newer);
		recursiveReplace(tempRoot.sibling, old, newer);
	}

	private TagNode recursiveBold(TagNode current) {
		if (current == null) {
			return null;
		}
		TagNode tempNode = null;
		String temp = current.tag;

		if (temp.equals("table")) {
			tempNode = current;
			return tempNode;
		}

		if (tempNode == null) {
			tempNode = recursiveBold(current.firstChild);
		}

		if (tempNode == null) {
			tempNode = recursiveBold(current.sibling);
		}
		return tempNode;
	}

	private void recursiveAdd(TagNode tempRoot, String word, String tag) {
		if(tempRoot == null) {
			return;
		}

		recursiveAdd(tempRoot.firstChild, word, tag);
		recursiveAdd(tempRoot.sibling, word, tag);

		if (tempRoot.firstChild == null) {
			while (tempRoot.tag.toLowerCase().contains(word)) {
				String[] splits = tempRoot.tag.split(" ");
				Boolean match = false;
				String taggedWord = "";
				StringBuilder taggerString = new StringBuilder(tempRoot.tag.length());
				int counter = 0;
				for (int words = 0; words < splits.length; words++) {
					if (splits[words].toLowerCase().matches(word+"[.,?!:;]?")) {
						match = true;
						taggedWord = splits[words];
						for (int integer = words + 1; integer < splits.length; integer++) {
							taggerString.append(splits[integer]+" ");
						}
						break;
					}
				}
				if (!match){
					return;
				}
				String finalString = taggerString.toString().trim();
				if(counter == 0) {
					tempRoot.firstChild = new TagNode(taggedWord, null, null);
					tempRoot.tag = tag;
					if (!finalString.equals("")) {
						tempRoot.sibling = new TagNode(finalString, null, tempRoot.sibling);
						tempRoot = tempRoot.sibling;
					}
				} else {
					TagNode taggedWordNode = new TagNode(taggedWord, null, null);
					TagNode newTag = new TagNode(tag, taggedWordNode, tempRoot.sibling);
					tempRoot.sibling = newTag;
					tempRoot.tag = tempRoot.tag.replaceFirst(" " + taggedWord, "");
					if (!finalString.equals("")) {
						tempRoot.tag = tempRoot.tag.replace(finalString, "");
						newTag.sibling = new TagNode(finalString, null, newTag.sibling);
						tempRoot = newTag.sibling;
					}
				}
			}
		}
	}

	private void recursiveRemoveSimple(TagNode tempRoot, String tag) { // em p b
		if (tempRoot == null) {
			return;
		}

		if (tempRoot.tag.equals(tag) && tempRoot.firstChild != null) {
			tempRoot.tag = tempRoot.firstChild.tag;
			if (tempRoot.firstChild.sibling != null) {
				TagNode traverseTag = null;
				for (traverseTag = tempRoot.firstChild; traverseTag.sibling != null; traverseTag = traverseTag.sibling) {
					traverseTag.sibling = tempRoot.sibling;
					tempRoot.sibling = tempRoot.firstChild.sibling;
				}
			}
			tempRoot.firstChild = tempRoot.firstChild.firstChild;
		}
		recursiveRemoveSimple(tempRoot.firstChild, tag);
		recursiveRemoveSimple(tempRoot.sibling, tag);
	}
	private void recursiveRemoveComplicated(TagNode tempRoot, String tag) { // ol ul
		if (tempRoot == null) return;
		if (tempRoot.tag.equals(tag) && tempRoot.firstChild != null) {
			tempRoot.tag = "p";
			TagNode traverseTag = null;
			for (traverseTag = tempRoot.firstChild; traverseTag.sibling != null; traverseTag = traverseTag.sibling) {
				traverseTag.tag = "p";
			}
			traverseTag.tag = "p";
			traverseTag.sibling = tempRoot.sibling;
			tempRoot.sibling = tempRoot.firstChild.sibling;
			tempRoot.firstChild = tempRoot.firstChild.firstChild;
		}

		recursiveRemoveComplicated(tempRoot.firstChild, tag);
		recursiveRemoveComplicated(tempRoot.sibling, tag);
	}

}


