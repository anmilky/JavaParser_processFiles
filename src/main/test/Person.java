import java.util.Comparator;

public class Person implements Comparator {
    public int age;
    public Person(int age){
          this.age=age;
    }


    @Override
    public int compare(Object o1, Object o2) {
      if(((Person)o1).age<((Person)o1).age){
          return -1;
      } if(((Person)o1).age>((Person)o1).age){
          return 1;
      }
      return 0;
    }
}
