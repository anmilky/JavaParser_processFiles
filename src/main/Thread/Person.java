import java.lang.String;

public class Person {
    String name;
    int age;
    static int A=10;
    static {
        A=12;
    }
    static {
        System.out.println("hello");
    }
    public static int Value=20;
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

//    @Override
//    public String toString() {
//        return "Person{" +
//                "name='" + name + '\'' +
//                ", age=" + age +
//                '}';
//    }

}
