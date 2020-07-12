/**
 * CS564 Group 17 Rosalie Cai, Ruiqi Hu
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


/**
 * B+Tree Structure Key - StudentId Leaf Node should contain [ key,recordId ]
 */
class BTree {
  FileWriter csvWriter;

  /**
   * Pointer to the root node.
   */
  private BTreeNode root;
  /**
   * Number of key-value pairs allowed in the tree/the minimum degree of B+Tree
   **/
  private int t;

  BTree(int t) {
    this.root = null;
    this.t = t;
  }

  /**
   * Search for an existing student given a studentId, return recordId if found. Otherwise, print
   * out a message that the given studentId has not been found in the table.
   * 
   * @param studentId
   * @return recordId
   */
  long search(long studentId) {
    return searchTree(root, studentId);
  }

  /**
   * helper method for search(long studentId)
   * 
   * @param currNode
   * @param studentId
   * @return recordID, or -1 for failure
   */
  long searchTree(BTreeNode currNode, long studentId) {
    // if value not found, or if empty tree
    if (currNode == null) {
      System.out.print("The given studentId, " + studentId + " has not been found in the table");
      return -1;
    }

    // search for matching key
    // if leaf
    if (currNode.leaf) {
      for (int i = 0; i < currNode.keys.length; i++) {
        // if studentId found
        if (currNode.keys[i] == studentId) {
          return currNode.values[i];
        }
      }
      // if not found
      System.out.print("The given studentId, " + studentId + " has not been found in the table");
      return -1;
    }
    // if internal node
    else {
      // if ID is smaller than the smallest key, go left
      if (studentId < currNode.keys[0]) {
        return searchTree(currNode.children[0], studentId);
      }
      // if ID is larger than the smallest key, go right

      // else if (studentId >= currNode.keys[currNode.n - 1]) {
      // return searchTree(currNode.C[currNode.n], studentId);
      // }
      // // if ID is somewhere in the middle
      else {
        for (int i = 1; i < currNode.keys.length; i++) {
          // if ID is somewhere in the middle
          if (currNode.keys[i] != 0 && studentId < currNode.keys[i]) {
            return searchTree(currNode.children[i], studentId);
          }
          // if all keys are searched
          if (currNode.keys[i] == 0) {
            return searchTree(currNode.children[i], studentId);
          }
        }
      }

    }

    return -1;


  }


  /**
   * insert a new student with a new studentId. Use a random generator to generate recordId for this
   * student. Update both the B+ tree index and the Student table.
   * 
   * @param student
   * @return
   */
  BTree insert(Student student) {
    // check input
    if (student.studentId == 0) {
      System.out.print("You should not have 0 for student ID");
      return this;
    }

    /**
     * TODO: Implement this function to insert in the B+Tree. Also, implement in student.csv after
     * inserting in B+Tree.
     */
    if (root == null) {
      root = new BTreeNode(t, true);// set up the tree
      root.keys[0] = student.studentId;// set up key
      root.values[0] = student.recordId;// set up key-value pair
    } else {
      insertTree(root, student, new BTreeNode(t, false));
    }

    // write to
    try {
      csvWriter = new FileWriter(new File("Student.csv"));
      //add student infomation to csv
      csvWriter.append(student.studentId + "," + student.studentName + "," + student.major + ","
          + student.level + "," + student.age + "," + student.recordId);
      csvWriter.flush();
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return this;
  }

  /**
   * Helper method for insert
   * 
   * @param currNode
   * @param student
   * @param newChlide
   */
  void insertTree(BTreeNode currNode, Student student, BTreeNode newChild) {
    // if not leaf
    // search for matching place to insert
    if (!currNode.leaf) {
      insertTree(currNode.children[getIndex(currNode.keys, student.studentId)], student, newChild);
      // usual case; didn’t split child
      if (newChild == null) {
        return;
      }
      // we split child, must insert *newchildentry in currNode
      else {
        // if we have space to accept the newChild
        if (hasSpace(currNode.keys)) {
          // insert child
          insertChild(currNode.children, getIndex(currNode.keys, newChild.keys[0]) + 1, newChild);
          // insert key
          insertValue(currNode.keys, getIndex(currNode.keys, newChild.keys[0]), newChild.keys[0]);

          newChild = null;
          return;
        }
        // if we do ont have space
        else {

          // keep propogating
          newChild = splitInternal(currNode, newChild);

          // if current node is root, create a new node
          if (currNode == root) {
            root = new BTreeNode(t, false);
            root.children[0] = currNode;
            root.children[1] = newChild;
            root.keys[0] = newChild.keys[0];
          }
        }
      }
    }
    // if current node is a leaf
    else {
      // if current node has space to put the key-value
      if (hasSpace(currNode.keys)) {
        // insert values
        insertValue(currNode.values, getIndex(currNode.keys, student.studentId), student.recordId);
        insertValue(currNode.keys, getIndex(currNode.keys, student.studentId), student.studentId);

        // increment key-value pair count
        currNode.n++;

        newChild = null;
        return;
      }
      // if splitting is needed
      else {
        // keep propogating
        newChild = splitLeaf(currNode, student);

      }

    }
  }



  /**
   * delete an existing student given a studentId. Return true if deletion is complete successfully.
   * Return false otherwise.
   * 
   * @param studentId
   * @return Return true if deletion is complete successfully. Return false otherwise.
   */
  boolean delete(long studentId) {
    /**
     * TODO: Implement this function to delete in the B+Tree and student table. Return true if the
     * student is deleted successfully otherwise, return false.
     */

    if (search(studentId) == -1) {
      return false;
    }
    remove(null, root, studentId, new BTreeNode(t, true));
    return true;
  }


  void remove(BTreeNode parentNode, BTreeNode currNode, long studentId, BTreeNode oldChild) {
    //non-leaf node
    if (!currNode.leaf) {
        for (int i = 0; i < currNode.keys.length; i++) {
          //recursive remove
            if (studentId != currNode.keys[i]) {
              if (studentId < currNode.keys[i])
                remove(currNode, currNode.C[i], studentId, oldChild);
              else if (studentId > currNode.keys[currNode.n - 1]) {
                remove(currNode, currNode.C[currNode.n], studentId, oldChild);
              }
            }
        }
            //usual case, child not deleted. oldChild is null
            if(oldChild == null) {
                return;
            }
            else {
                // remove oldChild from currNode
                currNode.next = null;
                // currNode has studentId to spare
                if(currNode.keys != null) {
                    oldChild = null;
                    return;
                }else {
                    BTreeNode Sibling = parentNode.next;
                    // S has extra entries
                    if(Sibling.keys != null) {
                        //redistribute evenly between N and S through parent
                        
                        oldChild = null;
                        return;
                    } else {
                        // merge N and S
                        
                        oldChild = &(currNode);
                        //pull splitting key from parent down into node on left
                            BTreeNode left = parentNode.C[parentNode.C.length-1];
                            for(int j = 0; j < currNode.keys.length; j++) {
                                insertTree(left, currNode.s, left.next);
                            }
                            for(int j = 0; j < currNode.next.keys.length; j++) {
                                insertTree(left.next, currNode.s, left.next);
                            }
                            parentNode.next = null;
                          //move all entries from M to node on left
                          //discard empty node M, return;
                            if(parentNode.keys == null) {
                                root = left;
                                return;
                            }
                        }
                        
                }
                
                
            }
        }
    // is a leaf node, L
    if(currNode.leaf) {
        // L has entries to spare
        if(currNode.keys != null) {
            //remove entry
            currNode.next = null;
            oldChild = null;
            return;
        }else {
            BTreeNode Sibling = parentNode.next;
            // S has extra entries
            if(Sibling.keys != null) {
                //redistribute evenly between N and S through parent
               
                // find entry in parent for node on right
                BTreeNode right = parentNode.C[parentNode.n -2];
 
                for(int i = 0; i < currNode.keys.length; i++) {
                    insertTree(currNode, currNode.s, currNode.next);
                }
                for(int i = 0; i < currNode.C.length; i++) {
                    insertTree(currNode.C, currNode.s, currNode.next);
                }
                for(int i = 0; i < right.C.length; i++) {
                    insertTree(right.C, currNode.s, right.next);
                }
                parentNode.next = null;
                if(parentNode.keys == null) {
                    root = right;
                    return;
                }
            }
        }
    }
    return;
}

  List<Long> print() {

    List<Long> listOfRecordID = new ArrayList<>();

    /**
     * TODO: Implement this function to print the B+Tree. Return a list of recordIDs from left to
     * right of leaf nodes.
     *
     */
    if (root != null) {
      // height
      int h = root.n;
      for (int i = 0; i >= 1; i--) {

        printNode(root, listOfRecordID, i);
      }
    }
    return listOfRecordID;
  }

  /**
   * Helper method for print
   * 
   * @param listOfRecordID
   * @return listOfRecordID
   */
  void printNode(BTreeNode currNode, List<Long> listOfRecordID, int level) {
    if (currNode == null) {
      return;
    }
    if (level == 1) {
      for (int i = 0; i < currNode.keys.length; i++) {
        listOfRecordID.add(currNode.s[i].recordId);
      }
    } else if (level > 1) {
      printNode(currNode.left, listOfRecordID, level);
      printNode(currNode.right, listOfRecordID, level);
    }
  }



  // ------------------------------helper methods-----------------------------------------------
  // -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------

  /**
   * count the number of valid elements in the list
   * 
   * @param list
   * @return the number of valid elements
   */
  int elementNum(long[] list) {
    int count = 0;
    for (int i = 0; i < list.length; i++) {
      if (list[i] != 0)
        count++;
    }
    return count;
  }


  /**
   * get the appropriote index
   * 
   * @param list
   * @param element
   * @return the appropriote index
   */
  int getIndex(long[] list, long element) {
    int index = 0;
    // list is empty, insert at 0
    if (list[0] == 0) {
      return index;
    }
    // if greater than all elements
    if (list[elementNum(list) - 1] < element) {
      return elementNum(list);
    }
    // find index within array
    for (; index < elementNum(list) - 1; index++) {
      if (list[index] < element && element < list[index + 1]) {
        return ++index;
      }
    }
    return index;
  }

  /**
   * check if there is sapce left for a list
   * 
   * @param list
   * @return whether there is space or not
   */
  boolean hasSpace(long[] list) {
    return elementNum(list) < list.length;
  }

  /**
   * shift the values in the array and insert the value
   * 
   * @param list
   * @param index
   * @param value
   */
  void insertValue(long[] list, int index, long value) {
    for (int i = list.length - 1; i >= index; i++) {
      list[i + 1] = list[i];
    }
    list[index] = value;
  }

  /**
   * insert a tree node at some index
   * 
   * @param list
   * @param index
   * @param child
   */
  void insertChild(BTreeNode[] list, int index, BTreeNode child) {
    for (int i = list.length - 1; i >= index; i++) {
      list[i + 1] = list[i];
    }
    list[index] = child;
  }

  /**
   * Split an internal node
   * 
   * @param currNode
   * @param newChild
   * @return the newly produced internal node
   */
  BTreeNode splitInternal(BTreeNode currNode, BTreeNode newChild) {
    // the new node splitted that stores the larger keys and children
    BTreeNode newNode = new BTreeNode(t, false);
    // copy first
    newNode.keys = Arrays.copyOfRange(currNode.keys, t, currNode.keys.length);
    newNode.children = Arrays.copyOfRange(currNode.children, t + 1, currNode.children.length);
    // clears the second half of the original keys and children list
    Arrays.fill(currNode.keys, t, currNode.keys.length, 0);
    Arrays.fill(currNode.children, t, currNode.children.length, null);

    // if the newChild should be added to the newNode
    if (newChild.keys[0] > currNode.keys[t - 1]) {
      // insert child and value
      insertChild(newNode.children, getIndex(newNode.keys, newChild.keys[0]) + 1, newChild);
      insertValue(newNode.keys, getIndex(newNode.keys, newChild.keys[0]), newChild.keys[0]);
    }
    // insert into currNode
    else {
      // insert child and value
      insertChild(currNode.children, getIndex(currNode.keys, newChild.keys[0]) + 1, newChild);
      insertValue(currNode.keys, getIndex(currNode.keys, newChild.keys[0]), newChild.keys[0]);
    }

    return newNode;
  }

  /**
   * split a leaf node
   * 
   * @param currNode
   * @param newChild
   * @return the newly produced internal node
   */
  BTreeNode splitLeaf(BTreeNode currNode, Student student) {
    // the new node splitted that stores the larger keys and values
    BTreeNode newNode = new BTreeNode(t, true);

    // copy first
    newNode.keys = Arrays.copyOfRange(currNode.keys, t, currNode.keys.length);
    newNode.values = Arrays.copyOfRange(currNode.values, t + 1, currNode.keys.length);
    // clears the second half of the original keys and value list
    Arrays.fill(currNode.keys, t, currNode.keys.length, 0);
    Arrays.fill(currNode.values, t, currNode.keys.length, 0);

    // add the new entry
    // if the entry should be added to the newNode
    if (student.studentId > currNode.keys[t - 1]) {
      // insert values
      insertValue(newNode.values, getIndex(newNode.keys, student.studentId), student.recordId);
      insertValue(newNode.keys, getIndex(newNode.keys, student.studentId), student.studentId);

    }
    // insert into currNode
    else {
      // insert values
      insertValue(currNode.values, getIndex(currNode.keys, student.studentId), student.recordId);
      insertValue(currNode.keys, getIndex(currNode.keys, student.studentId), student.studentId);

    }

    // updates the key-value pair count
    currNode.n = elementNum(currNode.keys);
    newNode.n = elementNum(currNode.keys);

    // updates the sibling pointers
    newNode.next = currNode.next;
    currNode.next = newNode.next;

    return newNode;
  }
}


