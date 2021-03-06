/**
 * CS564 Group 17 Rosalie Cai, Ruiqi Hu
 * 
 * Delete() by Ruiqi Hu Other BTree.java methods mostly by Rosalie Cai
 * 
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

      else if (studentId > currNode.keys[elementNum(currNode.keys) - 1]) {
        return searchTree(currNode.children[elementNum(currNode.keys)], studentId);
      }
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
        // the newly splitted node
        BTreeNode newNode = new BTreeNode(t, false);
        copy(newChild, newNode);

        // if we have space to accept the newChild
        if (hasSpace(currNode.keys)) {
          // insert child
          int index = getInsertIndex(currNode.keys, newNode.keys[0]);
          if (index == 0) {
            insertChild(currNode.children, 0, newNode);
          } else {
            insertChild(currNode.children, index + 1, newNode);
            // update reference
            currNode.children[index].next = newNode;
          }
          // insert key
          insertValue(currNode.keys, index, newNode.keys[0]);

          setNull(newChild);
          return;
        }
        // if we do ont have space
        else {
          // find the middle key
          long middleKey = currNode.keys[t - 1];
          // keep propogating
          splitInternal(currNode, newChild, newNode);

          // if current node is root, create a new node
          if (currNode == root) {
            root = new BTreeNode(t, false);
            root.children[0] = currNode;
            root.children[1] = newChild;
            root.keys[0] = middleKey;
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
        splitLeaf(currNode, student, newChild);

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

    // a list for students
    List<Student> studentList = new ArrayList<>();
    BufferedReader csvReader;
    String tuple;
    // fetching infomation from csv
    try {
      csvReader = new BufferedReader(new FileReader("Student.csv"));

      // a loop to read the csv file
      while ((tuple = csvReader.readLine()) != null) {
        String[] attribute = tuple.split(",");
        if (Long.valueOf(attribute[0]) != studentId)
          studentList.add(new Student(Long.valueOf(attribute[0]), Integer.valueOf(attribute[4]),
              attribute[1], attribute[2], attribute[3], Long.valueOf(attribute[5])));
      }
      csvReader.close();

      // clears the file
      FileWriter csvWriter = new FileWriter(new File("Student.csv"), false);
      csvWriter.append("");
      csvWriter.close();

      csvWriter = new FileWriter(new File("Student.csv"), true);


      // a loop to write to csv
      for (int i = 0; i < studentList.size(); i++) {
        csvWriter.append(studentList.get(i).studentId + "," + studentList.get(i).studentName + ","
            + studentList.get(i).major + "," + studentList.get(i).level + ","
            + studentList.get(i).age + "," + studentList.get(i).recordId + "\n");
      }
      csvReader.close();
      csvWriter.flush();
      csvWriter.close();


    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
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
      remove(currNode, currNode.children[getInsertIndex(currNode.keys, studentId)], studentId,
          oldChild);

      // usual case, child not deleted. oldChild is null
      if (isNull(oldChild)) {
        return;
      } else {
        // TODO


        int index = getDeleteIndex(currNode.keys, oldChild.keys[0]);

        // if reduced to only root
        if (parentNode == currNode && elementNum(parentNode.keys) == 1) {

          // // move keys in from sibling
          // System.arraycopy( root.children[1].values, 0, root.children[0].values,
          // elementNum( root.children[0].keys), elementNum( root.children[1].keys));
          // System.arraycopy(root.children[1].keys, 0,root.children[0].keys,
          // elementNum(root.children[0].keys), elementNum(root.children[1].keys));
          if (oldChild.keys[0] >= root.keys[0]) {
            root = root.children[0];

          } else {
            root = root.children[1];
          }

          setNull(oldChild);
          return;
        }
        // remove *oldchildentry from N
        // delete child
        if (index == 0 && oldChild.keys[0] < currNode.keys[0]) {
          deleteChild(currNode.children, index);
        } else {
          deleteChild(currNode.children, index + 1);
        }
        // delete key
        deleteValue(currNode.keys, index);



        // if currNode has entries to spare
        if (elementNum(currNode.keys) >= t) {
          // delete doesn't go further
          setNull(oldChild);
          return;
        }
        // need to redistribute &/ merge
        else {
          if (currNode == parentNode) {
            // delete doesn't go further
            setNull(oldChild);
            return;
          }
          // get siblings
          // use parentpointer to find sibling
          int currIndex = getDeleteIndex(parentNode.keys, currNode.keys[0]);


          // get left sibling
          BTreeNode leftSibling = null;
          if (currIndex >= 0 && currNode != parentNode.children[currIndex]) {
            leftSibling = parentNode.children[currIndex];
          }
          BTreeNode rightSibling = null;
          if (currIndex < elementNum(parentNode.keys)) {
            if (currNode.keys[0] < parentNode.keys[0]) {
              rightSibling = parentNode.children[currIndex + 1];
            } else {
              if (currIndex != 0 && (currIndex != elementNum(parentNode.keys) - 1))
                rightSibling = parentNode.children[currIndex + 2];
            }
          }

          // // get left sibling
          // BTreeNode leftSibling = null;
          // if (currIndex - 1 >= 0) {
          // leftSibling = parentNode.children[currIndex - 1];
          // }
          // // get right sibling
          // BTreeNode rightSibling = null;
          // if (currIndex < elementNum(parentNode.keys)) {
          // rightSibling = parentNode.children[currIndex + 1];
          // }

          //
          // // if reduced to only root
          // if (parentNode == root && elementNum(parentNode.keys) == 1 && currIndex == 0) {
          // // discard sibling
          //
          // root.children[0].keys[elementNum(root.children[0].keys)] = root.keys[0];
          //
          // // move keys in from sibling
          // System.arraycopy(root.children[1].children, 0, root.children[0].children,
          // elementNum(root.children[0].keys), elementNum(root.children[1].keys) + 1);
          // System.arraycopy(root.children[1].keys, 0, root.children[0].keys,
          // elementNum(root.children[0].keys), elementNum(root.children[1].keys));
          //
          // root = root.children[0];
          // if (currNode.leaf) {
          // root.leaf = true;
          // }
          // setNull(oldChild);
          // return;
          // }
          //

          // Right S has extra entries
          if (rightSibling != null && elementNum(rightSibling.keys) > t) {



            // // redistribute evenly between N and S through parent
            // // move keys to left in the currNode
            // TODO:check or revise according to your version
            // transfer keys and children
            int copyNum = (elementNum(rightSibling.keys) + elementNum(currNode.keys)) / 2
                - elementNum(currNode.keys);
            // TODO: move parent key down first and then move the
            currNode.keys[elementNum(currNode.keys)] = parentNode.keys[currIndex];
            parentNode.keys[currIndex] = rightSibling.keys[copyNum - 1];


            System.arraycopy(rightSibling.children, 0, currNode.children, elementNum(currNode.keys),
                copyNum);
            System.arraycopy(rightSibling.keys, 0, currNode.keys, elementNum(currNode.keys),
                copyNum - 1);

            // update sibling
            rightSibling.keys =
                Arrays.copyOfRange(rightSibling.keys, copyNum, rightSibling.keys.length);
            rightSibling.keys = Arrays.copyOf(rightSibling.keys, currNode.keys.length);
            rightSibling.children =
                Arrays.copyOfRange(rightSibling.children, copyNum, rightSibling.children.length);
            rightSibling.children = Arrays.copyOf(rightSibling.children, currNode.keys.length);


            setNull(oldChild);
            return;

          }
          // check the other sibling
          else if (leftSibling != null && elementNum(leftSibling.keys) > t) {


            int copyNum = (elementNum(leftSibling.keys) + elementNum(currNode.keys)) / 2
                - elementNum(currNode.keys);

            // move parent key down first
            insertValue(currNode.keys, 0, parentNode.keys[currIndex]);
            parentNode.keys[currIndex] = leftSibling.keys[elementNum(leftSibling.keys) - copyNum];
            BTreeNode[] values = new BTreeNode[leftSibling.keys.length + 1];
            long[] keys = new long[leftSibling.keys.length];


            System.arraycopy(leftSibling.children, elementNum(leftSibling.keys) - copyNum + 1,
                values, 0, copyNum);
            System.arraycopy(leftSibling.keys, elementNum(leftSibling.keys) - copyNum + 1, keys, 0,
                copyNum - 1);
            Arrays.fill(leftSibling.children, elementNum(leftSibling.keys) - copyNum + 1,
                leftSibling.children.length, null);
            Arrays.fill(leftSibling.keys, elementNum(leftSibling.keys) - copyNum,
                leftSibling.keys.length, 0);

            System.arraycopy(currNode.children, 0, values, copyNum, elementNum(currNode.keys));
            System.arraycopy(currNode.keys, 0, keys, copyNum - 1, elementNum(currNode.keys));



            // update sibling
            currNode.keys = Arrays.copyOf(keys, currNode.keys.length);
            currNode.children = Arrays.copyOf(values, currNode.values.length);

            //
            //
            // System.arraycopy(leftSibling.keys, 0, currNode.keys, elementNum(currNode.keys),
            // copyNum);
            // System.arraycopy(leftSibling.children, 0, currNode.children,
            // elementNum(currNode.keys),
            // copyNum);

            // update sibling
            leftSibling.children =
                Arrays.copyOfRange(leftSibling.children, 0, leftSibling.keys.length - copyNum);
            leftSibling.children = Arrays.copyOf(leftSibling.children, currNode.keys.length);
            leftSibling.keys =
                Arrays.copyOfRange(leftSibling.keys, 0, leftSibling.keys.length - copyNum);
            leftSibling.keys = Arrays.copyOf(leftSibling.keys, currNode.keys.length);


            setNull(oldChild);
            return;
          }
          // call node on the right hand side, merge
          else if (rightSibling != null) {

            // merge N and S
            oldChild.keys = rightSibling.keys;
            oldChild.leaf = rightSibling.leaf;

            // move parent's key down
            if (currNode.keys[0] < parentNode.keys[0]) {
              currNode.keys[elementNum(currNode.keys)] = parentNode.keys[currIndex];
            } else
              currNode.keys[elementNum(currNode.keys)] = parentNode.keys[currIndex + 1];
            // move keys in from sibling
            System.arraycopy(rightSibling.children, 0, currNode.children, elementNum(currNode.keys),
                elementNum(rightSibling.keys) + 1);
            System.arraycopy(rightSibling.keys, 0, currNode.keys, elementNum(currNode.keys),
                elementNum(rightSibling.keys));

            // // discard sibling
            // deleteChild(parentNode.children, getInsertIndex(parentNode.keys,
            // rightSibling.keys[0]));
            //
            // if(currNode.keys[0]<parentNode.keys[0]) {
            // deleteValue(parentNode.keys, currIndex );
            // }
            // else deleteValue(parentNode.keys, currIndex + 1);

            return;
          }
          // call node on the left hand side, merge with left sibling
          else if (leftSibling != null) {

            // TODO:copy & revise like the above merge or your own ideas
            // merge N and S
            oldChild.keys = currNode.keys;
            oldChild.leaf = currNode.leaf;

            // move parent's key down
            leftSibling.keys[elementNum(leftSibling.keys)] = parentNode.keys[currIndex];
            // move keys in from sibling
            System.arraycopy(currNode.children, 0, leftSibling.children,
                elementNum(leftSibling.keys), elementNum(currNode.keys) + 1);
            System.arraycopy(currNode.keys, 0, leftSibling.keys, elementNum(leftSibling.keys),
                elementNum(currNode.keys));

            // // discard sibling
            // deleteChild(parentNode.children, getInsertIndex(parentNode.keys,
            // leftSibling.keys[0]));
            // deleteValue(parentNode.keys, currIndex + 1);

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

        keyPosition = getDeleteIndex(currNode.keys, studentId);
        deleteValue(currNode.keys, keyPosition);
        deleteValue(currNode.values, keyPosition);

        setNull(oldChild);
        return;
      }
      // once in a while, the leaf becomes underfull
      else {
        // remove
        int keyPosition = getDeleteIndex(currNode.keys, studentId);
        deleteValue(currNode.keys, keyPosition);
        deleteValue(currNode.values, keyPosition);

        if (currNode == parentNode) {
          // if(elementNum(currNode.keys)==1) {
          // root = null;
          // }

          setNull(oldChild);
          return;
        }
        // TODO: similar to the redistribute and merge above
        // TODO: you can use BTreeNode.n here
        // TODO: you need to decrement n after you delete a key-value pair

        // TODO: I paste from the redistribute and merge above

        // find sibling
        int currIndex = getDeleteIndex(parentNode.keys, currNode.keys[0]);
        // get left sibling
        BTreeNode leftSibling = null;
        if (currIndex >= 0 && currNode != parentNode.children[currIndex]) {
          leftSibling = parentNode.children[currIndex];
        }
        BTreeNode rightSibling = null;

        if (currIndex < elementNum(parentNode.keys)) {
          if (currNode.keys[0] < parentNode.keys[0]) {
            rightSibling = parentNode.children[currIndex + 1];
          } else {
            if (currIndex != 0 && (currIndex != elementNum(parentNode.keys) - 1))
              rightSibling = parentNode.children[currIndex + 2];
          }
        }

        // Left S has extra entries
        if (rightSibling != null && elementNum(rightSibling.keys) > t) {

          // // redistribute evenly between N and S through parent
          // // move keys to left in the currNode

          // transfer keys and children
          int copyNum = (elementNum(leftSibling.keys) + elementNum(currNode.keys)) / 2
              - elementNum(currNode.keys);
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
        else if (leftSibling != null && elementNum(leftSibling.keys) > t) {
          long[] values = new long[leftSibling.keys.length];
          long[] keys = new long[leftSibling.keys.length];
          int copyNum = (elementNum(leftSibling.keys) + elementNum(currNode.keys)) / 2
              - elementNum(currNode.keys);

          System.arraycopy(leftSibling.keys, elementNum(leftSibling.keys) - copyNum, keys, 0,
              copyNum);
          System.arraycopy(leftSibling.values, elementNum(leftSibling.keys) - copyNum, values, 0,
              copyNum);
          Arrays.fill(leftSibling.keys, elementNum(leftSibling.keys) - copyNum,
              leftSibling.keys.length, 0);
          Arrays.fill(leftSibling.values, elementNum(leftSibling.keys) - copyNum,
              leftSibling.values.length, 0);



          System.arraycopy(currNode.keys, 0, keys, elementNum(leftSibling.keys) - copyNum,
              elementNum(currNode.keys));

          System.arraycopy(currNode.values, 0, values, elementNum(leftSibling.keys) - copyNum,
              elementNum(currNode.keys));

          // update sibling
          currNode.keys = Arrays.copyOf(keys, currNode.keys.length);
          currNode.values = Arrays.copyOf(values, currNode.values.length);
          // update parent
          parentNode.keys[currIndex] = currNode.keys[0];

          setNull(oldChild);
          return;
        }

        // call node on the right hand side, merge
        else if (rightSibling != null && (elementNum(rightSibling.keys)
            + elementNum(currNode.keys) <= currNode.keys.length)) {
          // merge L and S
          oldChild.keys = rightSibling.keys;
          oldChild.leaf = rightSibling.leaf;

          // // delete keys and values;
          // deleteValue(currNode.values, getDeleteIndex(currNode.keys, studentId));
          // deleteValue(currNode.keys, getDeleteIndex(currNode.keys, studentId));

          // move parent's key down
          // leftSibling.keys[elementNum(currNode.keys)] = parentNode.keys[currIndex];

          // move keys in from sibling
          System.arraycopy(rightSibling.values, 0, currNode.values, elementNum(currNode.keys),
              elementNum(rightSibling.keys));
          System.arraycopy(rightSibling.keys, 0, currNode.keys, elementNum(currNode.keys),
              elementNum(rightSibling.keys));

          currNode.next = rightSibling.next;


          return;
        }

        // call node on the left hand side, merge with left sibling
        else if (leftSibling != null
            && (elementNum(leftSibling.keys) + elementNum(currNode.keys) <= currNode.keys.length)) {

          // TODO:copy & revise like the above merge or your own ideas
          // merge L and S
          oldChild.keys = currNode.keys;

          // // delete keys and values;
          // deleteValue(currNode.values, getDeleteIndex(currNode.keys, studentId));
          // deleteValue(currNode.keys, getDeleteIndex(currNode.keys, studentId));

          // move parent's key down
          // leftSibling.keys[elementNum(currNode.keys)] = parentNode.keys[currIndex];

          // move keys in from sibling
          System.arraycopy(currNode.values, 0, leftSibling.values, elementNum(leftSibling.keys),
              elementNum(currNode.keys));
          System.arraycopy(currNode.keys, 0, leftSibling.keys, elementNum(leftSibling.keys),
              elementNum(currNode.keys));

          leftSibling.next = currNode.next;

          // // discard sibling
          // deleteChild(parentNode.children, getInsertIndex(parentNode.keys, leftSibling.keys[0]));
          // deleteValue(parentNode.keys, currIndex + 1);
          // // update parent
          // parentNode.keys[currIndex] = leftSibling.keys[0];
          //

          return;
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

      // find the smallest leaf
      while (!currNode.leaf) {
        currNode = currNode.children[0];
      }
      // add all recordIds
      while (currNode != null) {
        for (int i = 0; i < currNode.n; i++) {
          listOfRecordID.add(currNode.values[i]);
        }
        currNode = currNode.next;
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
    if (list[0] == 0 || element < list[0]) {
      return index;
    }
    // if greater than all elements
    if (list[elementNum(list) - 1] <= element) {
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
    for (int i = list.length - 1; i > index; i--) {
      list[i] = list[i - 1];
    }
    list[index] = child;
  }


  /**
   * Split an internal node
   * 
   * @param currNode
   * @param newNode  placeholder for splitted node
   * @param newChild node that need to be added
   */
  void splitInternal(BTreeNode currNode, BTreeNode newNode, BTreeNode newChild) {
    newNode.leaf = false;
    // copy first
    newNode.keys = Arrays.copyOfRange(currNode.keys, t, currNode.keys.length);
    newNode.children = Arrays.copyOfRange(currNode.children, t, currNode.children.length);
    newNode.keys = Arrays.copyOf(newNode.keys, currNode.keys.length);
    newNode.children = Arrays.copyOf(newNode.children, currNode.children.length);
    // keep the middle key
    long middleKey = currNode.keys[t];
    // clears the second half of the original keys and children list
    Arrays.fill(currNode.keys, t - 1, currNode.keys.length, 0);
    Arrays.fill(currNode.children, t, currNode.children.length, null);

    // if the newNode should be added to the newChild
    if (newChild.keys[0] >= middleKey) {
      // insert child and value
      insertChild(newNode.children, getInsertIndex(newNode.keys, newChild.keys[0]) + 1, newChild);
      insertValue(newNode.keys, getInsertIndex(newNode.keys, newChild.keys[0]), newChild.keys[0]);
      // update reference
      if (getInsertIndex(newNode.keys, newChild.keys[0]) == 0) {
        currNode.children[t - 1].next = newChild;
      } else {
        newNode.children[getInsertIndex(newNode.keys, newChild.keys[0])].next = newChild;
      }
    }
    // insert into currNode
    else {
      // insert child and value
      insertChild(currNode.children, getInsertIndex(currNode.keys, newChild.keys[0]) + 1, newChild);
      insertValue(currNode.keys, getInsertIndex(currNode.keys, newChild.keys[0]), newChild.keys[0]);
      // update reference
      currNode.children[getInsertIndex(newNode.keys, newChild.keys[0])].next = newChild;
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
    newChild.leaf = true;
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
      insertValue(newChild.keys, getInsertIndex(newChild.keys, student.studentId),
          student.studentId);

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
    for (int i = index; i < list.length - 1; i++) {
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
    for (int i = index; i < list.length - 1; i++) {
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
    // deep copy key list
    for (int i = 0; i < source.keys.length; i++) {
      dest.keys[i] = source.keys[i];
    }
    // deep copy child list
    for (int i = 0; i < source.children.length; i++) {
      dest.children[i] = source.children[i];
    }
    dest.n = source.n;
    dest.next = source.next;
    // deep copy value list
    for (int i = 0; i < source.values.length; i++) {
      dest.values[i] = source.values[i];
    }
  }
}


