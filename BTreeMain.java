import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Main Application.
 * <p>
 * You do not need to change this class.
 */
public class BTreeMain {

  public static void main(String[] args) {
    // random generator for record
    Random rand = new Random();

    /** Read the input file -- input.txt */
    Scanner scan = null;
    try {
      scan = new Scanner(new File("src/input.txt"));//TODO
    } catch (FileNotFoundException e) {
      System.out.println("File not found.");
    }

    /** Read the minimum degree of B+Tree first */

    int degree = scan.nextInt();

    BTree bTree = new BTree(degree);

    /** Reading the database student.csv into B+Tree Node */
    List<Student> studentsDB = getStudents();

    for (Student s : studentsDB) {
      bTree.insert(s);
    }

    /** Start reading the operations now from input file */
    try {
      while (scan.hasNextLine()) {
        Scanner s2 = new Scanner(scan.nextLine());

        while (s2.hasNext()) {

          String operation = s2.next();

          switch (operation) {
            case "insert": {

              long studentId = Long.parseLong(s2.next());
              String studentName = s2.next() + " " + s2.next();
              String major = s2.next();
              String level = s2.next();
              int age = Integer.parseInt(s2.next());
              /** TODO: Write a logic to generate recordID */
              long recordID = Math.abs(rand.nextLong());

              Student s = new Student(studentId, age, studentName, major, level, recordID);
              bTree.insert(s);

              break;
            }
            case "delete": {
              long studentId = Long.parseLong(s2.next());
              boolean result = bTree.delete(studentId);
              if (result)
                System.out.println("Student deleted successfully.");
              else
                System.out.println("Student deletion failed.");

              break;
            }
            case "search": {
              long studentId = Long.parseLong(s2.next());
              long recordID = bTree.search(studentId);
              if (recordID != -1)
                System.out.println("Student exists in the database at " + recordID);
              else
                System.out.println("Student does not exist.");
              break;
            }
            case "print": {
              List<Long> listOfRecordID = new ArrayList<>();
              listOfRecordID = bTree.print();
              System.out.println("List of recordIDs in B+Tree " + listOfRecordID.toString());
            }
            default:
              System.out.println("Wrong Operation");
              break;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * parsing infomation form csv file
   * 
   * @return a list of students
   */
  private static List<Student> getStudents() {
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
        studentList.add(new Student(Long.valueOf(attribute[0]), Integer.valueOf(attribute[4]),
            attribute[1], attribute[2], attribute[3], Long.valueOf(attribute[5])));
      }
      csvReader.close();
      
      //clears the file
      FileWriter csvWriter = new FileWriter(new File("Student.csv"), false);
      csvWriter.append("");
      csvWriter.close();
      
      
    } catch (FileNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return studentList;
  }
}
