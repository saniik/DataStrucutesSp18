package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 */
public class Tree {

    /**
     * Root node
     */
    TagNode root = null;

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
     * <p>
     * The root of the tree that is built is referenced by the root field of this object.
     */
    public void build() {
        root = recursiveBuildSubtree();
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
        TagNode current;
        TagNode temp;
        current = recursiveBold(root);

        if (current == null) {
            return;
        }

        current = current.firstChild;

        for (int integer = 1; integer < row; integer++) {
            current = current.sibling;
        }
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
        if ((tag.equals("em") || tag.equals("b") || tag.equals("p"))) {
            recursiveRemoveSimple(root, tag);
        }
        if ((tag.equals("ul") || tag.equals("ol"))) {
            recursiveRemoveComplicated(root, tag);
        }
    }

    /**
     * Adds a tag around all occurrences of a word in the DOM tree.
     *
     * @param word Word around which tag is to be added
     * @param tag  Tag to be added
     */
    public void addTag(String word, String tag) {
        if (tag.equals("b") || tag.equals("em")) {
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
        for (TagNode ptr = root; ptr != null; ptr = ptr.sibling) {
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
     */
    public void print() {
        print(root, 1);
    }

    private void print(TagNode root, int level) {
        for (TagNode ptr = root; ptr != null; ptr = ptr.sibling) {
            for (int i = 0; i < level - 1; i++) {
                System.out.print("      ");
            }
            if (root != this.root) {
                System.out.print("|---- ");
            } else {
                System.out.print("      ");
            }
            System.out.println(ptr.tag);
            if (ptr.firstChild != null) {
                print(ptr.firstChild, level + 1);
            }
        }
    }

   //Helper methods

    private TagNode recursiveBuildSubtree() {
        if (!sc.hasNextLine()) return null;

        String line = sc.nextLine();
        int length = line.length();
        boolean hasChild = false;

        if (line.charAt(0) == '<') {
            line = line.substring(1, length - 1);

            if (line.charAt(0) == '/') {
                return null;
            } else {
                hasChild = true;
            }
        }

        TagNode subRoot = new TagNode(line, null, null);

        if (hasChild) {
            subRoot.firstChild = recursiveBuildSubtree();
        }

        subRoot.sibling = recursiveBuildSubtree();
        return subRoot;
    }

    private void recursiveReplace(TagNode subRoot, String oldTag, String newTag) {
        if (subRoot == null) {
            return;
        }
        if (subRoot.tag.equals(oldTag)) {
            subRoot.tag = newTag;
        }

        recursiveReplace(subRoot.firstChild, oldTag, newTag);
        recursiveReplace(subRoot.sibling, oldTag, newTag);
    }

    private TagNode recursiveBold(TagNode subRoot) {
        if (subRoot == null) {
            return null;
        }

        TagNode finalRoot;
        String tag = subRoot.tag;

        if (tag.equals("table")) {
            return subRoot;
        }

        finalRoot = recursiveBold(subRoot.firstChild);

        if (finalRoot == null) {
            finalRoot = recursiveBold(subRoot.sibling);
        }
        return finalRoot;
    }

    private void recursiveAdd(TagNode subRoot, String text, String tag) {
        if(subRoot == null) {
            return;
        }

        recursiveAdd(subRoot.firstChild, text, tag);
        recursiveAdd(subRoot.sibling, text, tag);

        if (subRoot.firstChild == null) {
            while (subRoot.tag.toLowerCase().contains(text)) {
                String[] string = subRoot.tag.split(" ");
                Boolean includes = false;
                String word = "";
                StringBuilder builder = new StringBuilder(subRoot.tag.length());
                int counter = 1;
                for (String split : string) {
                    if (split.toLowerCase().matches(text + "[.,?!:;]?")) {
                        includes = true;
                        word = split;
                        for (int integer = 1; integer < string.length; integer++) {
                            builder.append(string[integer]).append(" ");
                            counter++;
                        }
                        break;
                    }
                }
                if (!includes){
                    return;
                }

                if (counter == 1) {
                    subRoot.firstChild = new TagNode(word, null, null);
                    subRoot.tag = tag;
                } else {
                    TagNode taggedWordNode = new TagNode(word, null, null);
                    TagNode newTag = new TagNode(tag, taggedWordNode, subRoot.sibling);
                    subRoot.sibling = newTag;
                    subRoot.tag = subRoot.tag.replaceFirst(" " + word, "");
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
                TagNode temp;

                for (temp = tempRoot.firstChild; temp.sibling != null; temp = temp.sibling) {
                    temp.sibling = tempRoot.sibling;
                    tempRoot.sibling = tempRoot.firstChild.sibling;
                }
            }

            tempRoot.firstChild = tempRoot.firstChild.firstChild;
        }
        recursiveRemoveSimple(tempRoot.firstChild, tag);
        recursiveRemoveSimple(tempRoot.sibling, tag);
    }

    private void recursiveRemoveComplicated(TagNode tempRoot, String tag) { // ol ul
        if (tempRoot == null) {
            return;
        }
        if (tempRoot.tag.equals(tag) && tempRoot.firstChild != null) {
            tempRoot.tag = "p";
            TagNode temp;

            for (temp = tempRoot.firstChild; temp.sibling != null; temp = temp.sibling) {
                temp.tag = "p";
            }

            temp.tag = "p";
            temp.sibling = tempRoot.sibling;
            tempRoot.sibling = tempRoot.firstChild.sibling;
            tempRoot.firstChild = tempRoot.firstChild.firstChild;
        }
        recursiveRemoveComplicated(tempRoot.firstChild, tag);
        recursiveRemoveComplicated(tempRoot.sibling, tag);
    }

}


