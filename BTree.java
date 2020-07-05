/**
 * Do NOT modify. This is the class with the main function
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * B+Tree Structure Key - StudentId Leaf Node should contain [ key,recordId ]
 */
class BTree {

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
    /**
     * TODO: Implement this function to search in the B+Tree. Return recordID for the given
     * StudentID. Otherwise, print out a message that the given studentId has not been found in the
     * table and return -1.
     */
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
          return currNode.s[i].recordId;
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
        return searchTree(currNode.C[0], studentId);
      }
      // if ID is larger than the biggest key, go right
      else if (studentId >= currNode.keys[currNode.n - 1]) {
        return searchTree(currNode.C[currNode.n], studentId);
      }
      // if ID is somewhere in the middle
      else {
        for (int i = 0; i < currNode.n; i++) {
          if (studentId < currNode.keys[i]) {
            return searchTree(currNode.C[i], studentId);
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
    /**
     * TODO: Implement this function to insert in the B+Tree. Also, implement in student.csv after
     * inserting in B+Tree.
     */
    if (root == null) {
      root = new BTreeNode(t, true);// set up the tree
      root.keys[0] = (int) student.studentId;// set up key
      root.s[0] = student;// set up key-value pair
    } else {
      insertTree(root, student, new BTreeNode(t, true));
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
    // search for matching place
    // if not leaf
    if (!currNode.leaf) {
      for (int i = 0; i < currNode.keys.length; i++) {
        // insert
        if (student.studentId != currNode.keys[i]) {
          if (student.studentId < currNode.keys[i])
            insertTree(currNode.C[i], student, newChild);
          else if (student.studentId > currNode.keys[currNode.n - 1]) {
            insertTree(currNode.C[currNode.n], student, newChild);
          }
        }

        // usual case; didn’t split child
        if (newChild == null) {
          return;
        }
        // we split child, must insert *newchildentry in currNode
        else {
          // if currNode have space
          if (currNode.n < currNode.C.length) {
            if (student.studentId > currNode.keys[currNode.n - 1]) {
              // add key
              currNode.keys[currNode.n] = newChild.keys[0];
              currNode.n++;
              // add child
              currNode.C[currNode.n] = newChild;
            } else {
              for (int j = currNode.keys.length - 1; j > i; i++) {
                currNode.keys[j] = newChild.keys[j - 1];
                currNode.C[j + 1] = newChild.C[j];
              }
              currNode.keys[i + 1] = newChild.keys[0];
              currNode.n++;
              currNode.C[i + 2] = newChild;
            }

            newChild = null;
            return;
          }
          // no space
          else {
            newChild.leaf = false;
            int[] temp = Arrays.copyOf(currNode.keys, currNode.keys.length + 1);
            temp[temp.length - 1] = newChild.keys[0];
            // set up new keys array
            Arrays.sort(temp);


            BTreeNode[] temp2 = Arrays.copyOf(currNode.C, currNode.C.length + 1);
            temp2[temp2.length - 1] = newChild;
            // set up new student array
            Arrays.sort(temp2, new Comparator<BTreeNode>() {
              @Override
              public int compare(BTreeNode o1, BTreeNode o2) {

                return (int) (o1.keys[0] - o2.keys[0]);
              }
            });

            // update all arrays
            Arrays.fill(currNode.keys, 0);
            Arrays.fill(currNode.C, null);
            for (int j = 0; j < currNode.C.length; j++) {
              if (j < t) {
                currNode.keys[j] = temp[j];
                currNode.C[j] = temp2[j];
              }
              // TODO: which are inclusive?
              if (j >= t) {
                newChild.keys[j - t] = temp[j];
                newChild.C[j - t] = temp2[j];
                newChild.n++;
              }
              currNode.n = t;
              newChild.C[newChild.n] = temp2[temp2.length - 1];
            }

            if (currNode == root) {
              root = new BTreeNode(t, false);
              root.C[0] = currNode;
              currNode.leaf = true;
              root.C[1] = newChild;
              newChild.leaf = true;
              root.keys[0] = temp[t];

            }

          }
        }


      }
    }
    // if leaf
    else {
      for (int i = 0; i < currNode.n; i++) {
        if (student.studentId == currNode.keys[i]) {
          System.out.println("Student already in the system");
        }
        // if insertable
        if (currNode.n < currNode.C.length
            && (student.studentId < currNode.keys[i] || i == currNode.n)) {
          for (int j = currNode.keys.length - 1; j >= i; j++) {
            // move key and move student
            currNode.keys[j] = currNode.keys[j - 1];
            currNode.s[j] = currNode.s[j - 1];
          }
          // insert values
          currNode.keys[i] = (int) student.studentId;
          currNode.s[i] = student;

          // set newChild to null
          newChild = null;
          return;
        }
        // if full
        if (currNode.n == currNode.C.length
            && (student.studentId < currNode.keys[i] || i == currNode.n)) {

          Student[] temp = Arrays.copyOf(currNode.s, currNode.s.length + 1);
          temp[temp.length - 1] = student;

          // set up new student array
          Arrays.sort(temp, new Comparator<Student>() {
            @Override
            public int compare(Student o1, Student o2) {

              return (int) (o1.studentId - o2.studentId);
            }
          });
          currNode.s = Arrays.copyOfRange(temp, 0, t + 1);
          currNode.s = Arrays.copyOf(currNode.s, temp.length - 1);
          newChild.s = Arrays.copyOfRange(temp, t, temp.length);
          newChild.s = Arrays.copyOf(newChild.s, temp.length - 1);

          // set up key arrays in newchild and current child
          for (int j = 0; j < newChild.s.length; j++) {
            if (newChild.s[i] != null) {
              newChild.keys[i] = (int) newChild.s[i].studentId;
            } else {
              newChild.keys[i] = 0;
            }

            if (currNode.s[i] != null) {
              currNode.keys[i] = (int) currNode.s[i].studentId;
            } else {
              currNode.keys[i] = 0;
            }
          }
        }
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
    return true;
  }

  List<Long> print() {

    List<Long> listOfRecordID = new ArrayList<>();

    /**
     * TODO: Implement this function to print the B+Tree. Return a list of recordIDs from left to
     * right of leaf nodes.
     *
     */
    return listOfRecordID;
  }
}
