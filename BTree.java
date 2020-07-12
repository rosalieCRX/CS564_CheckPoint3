/**
 * CS564 Group 17 Rosalie Cai, Ruiqi Hu
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

      // increment key-value pair count
      root.n++;

    } else {
      insertTree(root, student, new BTreeNode(t, false));
    }

    // write to csv
    try {
      csvWriter = new FileWriter(new File("Student.csv"), true);
      // add student infomation to csv
      csvWriter.append(student.studentId + "," + student.studentName + "," + student.major + ","
          + student.level + "," + student.age + "," + student.recordId + "\n");
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
      insertTree(currNode.children[getInsertIndex(currNode.keys, student.studentId)], student,
          newChild);
      // usual case; didn’t split child
      if (isNull(newChild)) {
        return;
      }
      // we split child, must insert *newchildentry in currNode
      else {
        //the newly splitted node
        BTreeNode newNode = new BTreeNode(t,false);
        copy(newChild,newNode);
        
        // if we have space to accept the newChild
        if (hasSpace(currNode.keys)) {
          // insert child
          int index = getInsertIndex(currNode.keys, newNode.keys[0]);
          if (index == 0) {
            insertChild(currNode.children, 0, newNode);
          } else {
            insertChild(currNode.children, getInsertIndex(currNode.keys, newNode.keys[0]) + 1,
                newNode);
          }
          // insert key
          insertValue(currNode.keys, index, newNode.keys[0]);

          setNull(newChild);
          return;
        }
        // if we do ont have space
        else {

          // keep propogating
          splitInternal(currNode, newChild);
          
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
        insertValue(currNode.values, getInsertIndex(currNode.keys, student.studentId),
            student.recordId);
        insertValue(currNode.keys, getInsertIndex(currNode.keys, student.studentId),
            student.studentId);

        // increment key-value pair count
        currNode.n++;

        setNull(newChild);
        return;
      }
      // if splitting is needed
      else {
        // keep propogating
        splitLeaf(currNode, student,newChild);
        
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
    remove(root, root, studentId, new BTreeNode(t, true));

    // update csv file
    try {
      File inputFile = new File("student.csv");
      File outputFile = new File("student.csv");
      BufferedReader reader = new BufferedReader(new FileReader(inputFile));
      BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
      long Id = studentId;
      String currentLine;


      // TODO : you need to read all data into an arraylist/something and then use another loop to
      // put these data back
      // what you have can cause an INFINITE LOOP!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //
      while ((currentLine = reader.readLine()) != null) {
        if (currentLine.equals(Long.toString(Id))) {
          // TODO

        }
        // // TODO
        // else add to file


      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return true;
  }

  /**
   * TODO:paste instrution and add comment
   * 
   * @param parentNode
   * @param currNode
   * @param studentId
   * @param oldChild
   */
  void remove(BTreeNode parentNode, BTreeNode currNode, long studentId, BTreeNode oldChild) {
    // non-leaf node
    if (!currNode.leaf) {
      // TODO
      remove(currNode, currNode.children[getInsertIndex(parentNode.keys, studentId)], studentId,
          oldChild);

      // usual case, child not deleted. oldChild is null
      if (isNull(oldChild)) {
        return;
      } else {
        // TODO

        // delete child
        int index = getDeleteIndex(currNode.keys, oldChild.keys[0]);
        if (index == 0) {
          deleteChild(currNode.children, index);
        } else {
          deleteChild(currNode.children, index + 1);
        }
        // delete key
        deleteValue(currNode.keys, index);

        // currNode has entries to spare
        if (elementNum(currNode.keys) > t) {
          // delete doesn't go further
          setNull(oldChild);
          return;
        }
        // need to redistribute &/ merge
        else {
          // use parentpointer to find sibling
          int currIndex = getDeleteIndex(parentNode.keys, currNode.keys[0]);
          // get left sibling
          BTreeNode leftSibling = null;
          if (currIndex - 1 >= 0) {
            leftSibling = parentNode.children[currIndex - 1];
          }
          BTreeNode rightSibling = null;
          if (currIndex + 1 < elementNum(parentNode.keys)) {
            rightSibling = parentNode.children[currIndex + 1];
          }


          // Left S has extra entries
          if (rightSibling != null
              && elementNum(rightSibling.keys) >= 2 * t - elementNum(currNode.keys)) {
            // // redistribute evenly between N and S through parent
            // // move keys to left in the currNode


            // if (currNode.keys[index - 1] == 0) {
            // for (int i = 0; i < 2 * currNode.t; i++) {
            // if (currNode.keys[i + 1] != 0 && currNode.keys[i] == 0) {
            // currNode.keys[i] = currNode.keys[i + 1];
            // }
            // }
            // }
            // // redistribute sibling's keys to currNode
            // int elementNum = elementNum(currNode.keys);
            // for (int i = 0; i <= 2 * rightSibling.t; i++) {
            // if (i >= rightSibling.t) {
            // currNode.keys[elementNum + i] = rightSibling.keys[i];
            // if (rightSibling.keys[i + 1] != 0 && rightSibling.keys[i] == 0) {
            // rightSibling.keys[i] = rightSibling.keys[i + 1];
            // }
            // }
            // }
            //
            // // move the smallest key of sibling to parent
            // // for(int i = 0; i < sibling.keys.length; i++) {
            // if (elementNum(parentNode.keys) < parentNode.t) {
            // parentNode.keys[0] = rightSibling.keys[0];//TODO ?????????????????
            // }
            // // }


            // TODO:check or revise according to your version
            // transfer keys and children
            int copyNum = t - elementNum(currNode.keys);
            System.arraycopy(rightSibling.keys, 0, currNode.keys, elementNum(currNode.keys),
                copyNum);
            System.arraycopy(rightSibling.children, 0, currNode.children, elementNum(currNode.keys),
                copyNum);


            // update sibling
            rightSibling.keys =
                Arrays.copyOfRange(rightSibling.keys, copyNum, rightSibling.keys.length - 1);
            rightSibling.keys = Arrays.copyOf(rightSibling.keys, currNode.keys.length);
            rightSibling.children =
                Arrays.copyOfRange(rightSibling.children, copyNum, rightSibling.keys.length - 1);
            rightSibling.children = Arrays.copyOf(rightSibling.children, currNode.keys.length);

            // update parent
            parentNode.keys[currIndex] = rightSibling.keys[0];

            setNull(oldChild);
            return;

          }
          // check the other sibling
          else if (leftSibling != null
              && elementNum(leftSibling.keys) >= 2 * t - elementNum(currNode.keys)) {


            // TODO: Copy and revise the above if



            // update parent
            parentNode.keys[currIndex] = currNode.keys[0];


            setNull(oldChild);
            return;
          }
          // call node on the right hand side, merge
          else if (rightSibling != null) {
            // merge N and S
            copy(currNode, oldChild);

            // move parent's key down
            currNode.keys[elementNum(currNode.keys)] = parentNode.keys[currIndex + 1];
            // move keys in from sibling
            System.arraycopy(rightSibling.children, 0, currNode.children, elementNum(currNode.keys),
                elementNum(rightSibling.keys) + 1);
            System.arraycopy(rightSibling.keys, 0, currNode.keys, elementNum(currNode.keys),
                elementNum(rightSibling.keys));

            // discard sibling
            deleteChild(parentNode.children, getInsertIndex(parentNode.keys, rightSibling.keys[0]));
            deleteValue(parentNode.keys, currIndex + 1);

            //
            // // move keys to left in the currNode
            // if (currNode.keys[index - 1] == 0) {
            // for (int i = 0; i < 2 * currNode.t; i++) {
            // if (currNode.keys[i + 1] != 0 && currNode.keys[i] == 0) {
            // currNode.keys[i] = currNode.keys[i + 1];
            // }
            // }
            // }
            // // move all sibling's keys to currNode
            // int elementNum = elementNum(currNode.keys);
            // for (int i = 0; i <= rightSibling.keys.length; i++) {
            // currNode.keys[elementNum + i] = rightSibling.keys[i];
            // }
            // rightSibling = null;

            // // delete parentNode
            // parentNode.children[0] = currNode;
            // // move keys to left in the parentNode
            // for (int i = 0; i < 2 * parentNode.t; i++) {
            // if (parentNode.keys[i + 1] != 0 && parentNode.keys[i] == 0) {
            // parentNode.keys[i] = parentNode.keys[i + 1];
            // }
            // }
            // // oldchildentry = & (current entry in parent for M)
            // int newindex = getInsertIndex(parentNode.keys, studentId);
            // oldChild = parentNode.children[newindex];
            // // redistribute parentNode's children
            // // move all entries from M to node on left
            // for (int i = 0; i < parentNode.children.length; i++) {
            // if (i != parentNode.children.length - 1)
            // parentNode.children[i] = parentNode.children[i + 1];
            // }
            //
            // // discard empty node M, return!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // for (int i = 0; i < parentNode.children.length; i++) {
            // if (parentNode.children[i] == null) {
            // // ?????????????
            // }
            // }

            return;
          }
          // call node on the left hand side, merge with left sibling
          else if (leftSibling != null) {
            // TODO:copy & revise like the above merge or your own ideas

            return;
          }
        }
      }
    }
    // if leaf
    else {
      // L has entries to spare
      if (elementNum(currNode.keys) > t) {
        // remove entry
        int keyPosition = 0;
        for (int i = 0; i < currNode.keys.length; i++) {
          if (studentId == currNode.keys[i]) {
            currNode.keys[i] = 0;
            for (keyPosition = i; keyPosition < currNode.keys.length; keyPosition++) {
              if (currNode.keys[i + 1] != 0)
                currNode.keys[i] = currNode.keys[i + 1];
            }
          }
        }
        // TODO: you need to delete the value in the key-value pair too



        // my version
        keyPosition = getDeleteIndex(currNode.keys, studentId);
        deleteValue(currNode.keys, keyPosition);
        deleteValue(currNode.values, keyPosition);


        setNull(oldChild);
        return;
      }
      // once in a while, the leaf becomes underfull
      else {

        // TODO: similar to the redistribute and merge above
        // TODO: you can use BTreeNode.n here
        // TODO: you need to decrement n after you delete a key-value pair



        // TODO: I paste from the redistribute and merge above


        // use parentpointer to find sibling
        int currIndex = getDeleteIndex(parentNode.keys, currNode.keys[0]);
        // get left sibling
        BTreeNode leftSibling = null;
        if (currIndex - 1 >= 0) {
          leftSibling = parentNode.children[currIndex - 1];
        }
        BTreeNode rightSibling = null;
        if (currIndex + 1 < elementNum(parentNode.keys)) {
          rightSibling = parentNode.children[currIndex + 1];
        }


        // Left S has extra entries
        if (rightSibling != null
            && elementNum(rightSibling.keys) >= 2 * t - elementNum(currNode.keys)) {
          // // redistribute evenly between N and S through parent
          // // move keys to left in the currNode

          // transfer keys and children
          int copyNum = t - elementNum(currNode.keys);
          System.arraycopy(rightSibling.keys, 0, currNode.keys, elementNum(currNode.keys), copyNum);
          System.arraycopy(rightSibling.children, 0, currNode.children, elementNum(currNode.keys),
              copyNum);


          // update sibling
          rightSibling.keys =
              Arrays.copyOfRange(rightSibling.keys, copyNum, rightSibling.keys.length - 1);
          rightSibling.keys = Arrays.copyOf(rightSibling.keys, currNode.keys.length);
          rightSibling.children =
              Arrays.copyOfRange(rightSibling.children, copyNum, rightSibling.keys.length - 1);
          rightSibling.children = Arrays.copyOf(rightSibling.children, currNode.keys.length);

          // update parent
          parentNode.keys[currIndex] = rightSibling.keys[0];

          setNull(oldChild);
          return;

        }
        // check the other sibling
        else if (leftSibling != null
            && elementNum(leftSibling.keys) >= 2 * t - elementNum(currNode.keys)) {


          // TODO: Copy and revise the above if



          // update parent
          parentNode.keys[currIndex] = currNode.keys[0];


          setNull(oldChild);
          return;
        }
        // call node on the right hand side, merge
        else if (rightSibling != null) {
          // merge N and S
          copy(currNode, oldChild);
          
          // move parent's key down
          currNode.keys[elementNum(currNode.keys)] = parentNode.keys[currIndex + 1];
          // move keys in from sibling
          System.arraycopy(rightSibling.children, 0, currNode.children, elementNum(currNode.keys),
              elementNum(rightSibling.keys) + 1);
          System.arraycopy(rightSibling.keys, 0, currNode.keys, elementNum(currNode.keys),
              elementNum(rightSibling.keys));

          // discard sibling
          deleteChild(parentNode.children, getInsertIndex(parentNode.keys, rightSibling.keys[0]));
          deleteValue(parentNode.keys, currIndex + 1);

          return;
        }
        // call node on the left hand side, merge with left sibling
        else if (leftSibling != null) {
          // TODO:copy & revise like the above merge or your own ideas

          return;
        }



        // use parentNode to find sibling
        int index1 = getInsertIndex(currNode.keys, studentId);
        BTreeNode sibling = parentNode.children[index1 + 1];
        // S has extra entries
        if (elementNum(sibling.keys) < sibling.t && elementNum(sibling.keys) <= 2 * sibling.t) {
          // redistribute evenly between N and S through parent
          // move keys to left in the currNode
          if (currNode.keys[index1 - 1] == 0) {
            for (int i = 0; i < 2 * currNode.t; i++) {
              if (currNode.keys[i + 1] != 0 && currNode.keys[i] == 0) {
                currNode.keys[i] = currNode.keys[i + 1];
              }
            }
          }
          // redistribute sibling's keys to currNode
          int elementNum = elementNum(currNode.keys);
          for (int i = 0; i <= 2 * sibling.t; i++) {
            if (i >= sibling.t) {
              currNode.keys[elementNum + i] = sibling.keys[i];
              if (sibling.keys[i + 1] != 0 && sibling.keys[i] == 0) {
                sibling.keys[i] = sibling.keys[i + 1];
              }
            }
          }

          // find entry in parent for node on right
          // move the smallest key of sibling to parent
          if (elementNum(parentNode.keys) < parentNode.t) {
            parentNode.keys[0] = sibling.keys[0];
          }
          setNull(oldChild);
          return;
        }
        // call node on right hand side M
        else {
          // merge L and S
          // move keys to left in the currNode
          if (currNode.keys[index1 - 1] == 0) {
            for (int i = 0; i < 2 * currNode.t; i++) {
              if (currNode.keys[i + 1] != 0 && currNode.keys[i] == 0) {
                currNode.keys[i] = currNode.keys[i + 1];
              }
            }
          }
          // move all sibling's keys to currNode
          int elementNum = elementNum(currNode.keys);
          for (int i = 0; i <= sibling.keys.length; i++) {
            currNode.keys[elementNum + i] = sibling.keys[i];
          }
          sibling = null;
          // delete parentNode
          parentNode.children[0] = currNode;
          // move keys to left in the parentNode
          for (int i = 0; i < 2 * parentNode.t; i++) {
            if (parentNode.keys[i + 1] != 0 && parentNode.keys[i] == 0) {
              parentNode.keys[i] = parentNode.keys[i + 1];
            }
          }

          // oldchildentry = & (current entry in parent for M)
          int newindex = getInsertIndex(parentNode.keys, studentId);
          oldChild = parentNode.children[newindex];
          // move all entries from M to node on left;
          for (int i = 0; i < parentNode.children.length; i++) {
            if (i != parentNode.children.length - 1)
              parentNode.children[i] = parentNode.children[i + 1];
          }
          // discard empty node M, return!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          for (int i = 0; i < parentNode.children.length; i++) {
            if (parentNode.children[i] == null) {
              // ?????????????
            }
          }



        }
      }
    }



  }


  /**
   * Return a list of recordIDs from left to right of leaf nodes.
   *
   */
  List<Long> print() {

    List<Long> listOfRecordID = new ArrayList<>();


    BTreeNode currNode = root;
    if (root != null) {
      // go through all root's keys


      // TODO: what are you doing here????
      // for (int i = 0; i < root.children.length; i++) {
      // // check the availability
      // if (root.children[i] != null) {
      // // reach to the leaf
      // while (currNode.children != null) {
      // if (currNode.leaf) {
      // for (int j = 0; j < currNode.keys.length; j++) {
      // if (currNode.keys[i] != 0)
      // listOfRecordID.add(currNode.values[i]);
      // }
      // }
      // }
      // }
      // }



      // find the smallest leaf
      while (!currNode.leaf) {
        currNode = currNode.children[0];
      }
      // add all recordIds
      while (currNode.next != null) {
        for (int i = 0; i < currNode.n; i++) {
          listOfRecordID.add(currNode.values[i]);
        }
      }
    }
    return listOfRecordID;
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
   * get the appropriote index for insertion
   * 
   * @param list
   * @param element
   * @return the appropriote index
   */
  int getInsertIndex(long[] list, long element) {
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
      if (list[index] <= element && element < list[index + 1]) {
        return ++index;
      }
    }
    return index;
  }

  /**
   * get the appropriote index for deletion
   * 
   * @param list
   * @param element
   * @return the appropriote index
   */
  int getDeleteIndex(long[] list, long element) {
    if (getInsertIndex(list, element) > 0) {
      return getInsertIndex(list, element) - 1;
    }
    return 0;
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
    for (int i = list.length - 1; i > index; i--) {
      list[i] = list[i - 1];
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
    for (int i = list.length - 1; i >= index; i--) {
      list[i] = list[i - 1];
    }
    list[index] = new BTreeNode(t,false);
    copy(child,list[index]);
  }
  /**
   * Split an internal node
   * 
   * @param currNode
   * @param newChild placeholder for splitted node
   */
  void splitInternal(BTreeNode currNode, BTreeNode newChild) {
    // copy first
    newChild.keys = Arrays.copyOfRange(currNode.keys, t, currNode.keys.length);
    newChild.children = Arrays.copyOfRange(currNode.children, t, currNode.children.length);
    newChild.keys = Arrays.copyOf(newChild.keys, currNode.keys.length);
    newChild.children = Arrays.copyOf(newChild.children, currNode.children.length);


    // clears the second half of the original keys and children list
    Arrays.fill(currNode.keys, t, currNode.keys.length, 0);
    Arrays.fill(currNode.children, t, currNode.children.length, null);

    // if the newChild should be added to the newNode
    if (newChild.keys[0] > currNode.keys[t - 1]) {
      // insert child and value
      insertChild(newChild.children, getInsertIndex(newChild.keys, newChild.keys[0]) + 1, newChild);
      insertValue(newChild.keys, getInsertIndex(newChild.keys, newChild.keys[0]), newChild.keys[0]);
    }
    // insert into currNode
    else {
      // insert child and value
      insertChild(currNode.children, getInsertIndex(currNode.keys, newChild.keys[0]) + 1, newChild);
      insertValue(currNode.keys, getInsertIndex(currNode.keys, newChild.keys[0]), newChild.keys[0]);
    }

  }

  /**
   * split a leaf node
   * 
   * @param currNode
   * @param student 
   * @param newChild--the new node splitted that stores the larger keys and values
   * 
   */
  void splitLeaf(BTreeNode currNode, Student student, BTreeNode newChild) {
    newChild.leaf=true;
    // copy first
    newChild.keys = Arrays.copyOfRange(currNode.keys, t, currNode.keys.length);
    newChild.values = Arrays.copyOfRange(currNode.values, t, currNode.keys.length);
    newChild.keys = Arrays.copyOf(newChild.keys, currNode.keys.length);
    newChild.values = Arrays.copyOf(newChild.values, currNode.keys.length);

    // clears the second half of the original keys and value list
    Arrays.fill(currNode.keys, t, currNode.keys.length, 0);
    Arrays.fill(currNode.values, t, currNode.keys.length, 0);

    // add the new entry
    // if the entry should be added to the newNode
    if (student.studentId > currNode.keys[t - 1]) {
      // insert values
      insertValue(newChild.values, getInsertIndex(newChild.keys, student.studentId),
          student.recordId);
      insertValue(newChild.keys, getInsertIndex(newChild.keys, student.studentId), student.studentId);

    }
    // insert into currNode
    else {
      // insert values
      insertValue(currNode.values, getInsertIndex(currNode.keys, student.studentId),
          student.recordId);
      insertValue(currNode.keys, getInsertIndex(currNode.keys, student.studentId),
          student.studentId);

    }

    // updates the key-value pair count
    currNode.n = elementNum(currNode.keys);
    newChild.n = elementNum(currNode.keys);

    // updates the sibling pointers
    newChild.next = currNode.next;
    currNode.next = newChild;

  }



  /**
   * shift the values in the array and delete the value
   * 
   * @param list
   * @param index
   * @param value
   */
  void deleteValue(long[] list, int index) {
    // list[index]=0;
    for (int i = index; i < list.length; i++) {
      list[i] = list[i + 1];
    }
    list[list.length - 1] = 0;
  }

  /**
   * delete a tree node at some index
   * 
   * @param list
   * @param index
   * @param child
   */
  void deleteChild(BTreeNode[] list, int index) {
    // list[index]=null;
    for (int i = index; i < list.length; i++) {
      list[i] = list[i + 1];
    }
    list[list.length - 1] = null;
  }

  /**
   * set a child to null
   * 
   * @param dest
   * @param source
   */
  void setNull(BTreeNode node) {
    node.t = t;
    node.leaf = false;
    node.keys = new long[2 * t - 1];
    node.children = new BTreeNode[2 * t];
    node.n = 0;
    node.next = null;
    node.values = new long[2 * t - 1];

  }

  /**
   * check if node is null
   * 
   * @param node
   * @return true if null
   */
  boolean isNull(BTreeNode node) {
    return elementNum(node.keys) == 0;
  }

  /*
   * deep copy from a node into another node
   */
  void copy(BTreeNode source, BTreeNode dest) {
    dest.t = source.t;
    dest.leaf = source.leaf; 
    //deep copy key list
    for(int i=0;i<source.keys.length;i++) {
      dest.keys[i] = source.keys[i];
    }
    //deep copy child list
    for(int i=0;i<source.children.length;i++) {
      dest.children[i] = source.children[i];
    }
    dest.n = source.n;
    dest.next = source.next;
    //deep copy value list
    for(int i=0;i<source.values.length;i++) {
      dest.values[i] = source.values[i];
    }
  }
}


