import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BTreeTests {

  public static void main(String[] args) {
    BTree bTree = new BTree(3);
    // TODO Auto-generated method stub
    /** Reading the database student.csv into B+Tree Node */
    List<Student> studentsDB = getStudents();

    for (Student s : studentsDB) {
      bTree.insert(s);
    }

    List b = bTree.print();
    for (int i = 0; i < b.size(); i++) {
      System.out.print(bTree.print().get(i) + " ");
    }
    
    for (int i = 0; i < studentsDB.size(); i++) {
      System.out.println("search " + studentsDB.get(i).recordId + " is "
          + bTree.search(studentsDB.get(i).studentId));
    }
    
    for (int i = 0; i < studentsDB.size(); i++) {
      System.out.println("delete" + studentsDB.get(i).studentId + "is"
          + bTree.delete(studentsDB.get(i).studentId));
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

      // clears the file
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
