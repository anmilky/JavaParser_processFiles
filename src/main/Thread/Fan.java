import java.lang.String;

import java.util.ArrayList;
import java.util.List;

public class Fan{

    static class Food extends World{} // 默认继承Object
    static class World{} // 默认继承Object
    static class Fruit extends Food{}
    static class Meat extends Food {}

    static class Apple extends Fruit{}
    static class RedApple extends Apple {}
    static  class Beef extends Meat{}

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public static <T,E extends Food >void test2(T t, List<E> foods){
        foods.add((E) new Object());

    } public static <E extends Food >void test3(Food food){
        System.out.println(food.getClass());
    }

    public static void main(String[] args) {
        List<? extends Food> foods=new ArrayList<>();
//        test3(new World());
        List<String> strings=new ArrayList<>();
        strings.add(new String());
//        foods.add(new Apple());
//        foods.add(new Food());
//        foods.add(new Fruit());
//        foods.add(new Object());
//
//        List<Fruit> fruits=new ArrayList<>();
//        foods=fruits;
//
//        List<? super Apple> apples=new ArrayList<>();
//        apples.add(new RedApple());
//        apples.add(new Apple());
//        apples.add(new Food());
//        apples.add(new Object());



    }





}