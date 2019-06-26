import java.lang.String;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Erasure<T> {
    T object;

    public Erasure(T object) {
        this.object = object;
    }
    public void accept(ArrayList<Number> erasure){
        Field[] filds=erasure.getClass().getDeclaredFields();
        Arrays.stream(filds).forEach(System.out::println);
    }
    public static void inspect(List<?> list) {
        System.out.println(list.getClass().getName());
//        System.out.println(list.size());
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        List[] a=new List[10];
        List<String>[] stringLists = a;
        a[1]=new ArrayList<Integer>();
        a[1].add(124);
        System.out.println(a[1].get(0));// 值124
        System.out.println(stringLists[1]);//指向一个integer的数组的引用  [124]
        System.out.println(stringLists[1].get(0));//ClassCastException

//       Erasure.class.getMethod("inspect", List.class).invoke(null,strs);
//       System.out.println(strs.size());
//       strs.stream().forEach(System.out::println);//只是在这里又会出错，因为往里面添加了一个integer。
    }

}
