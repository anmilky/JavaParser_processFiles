import java.lang.String;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Func {

    public static class Fei implements Supplier<BigInteger>{

        Deque<BigInteger> queue=new LinkedList<>();
        public Fei(){
            queue.push(BigInteger.ZERO);
            queue.push(BigInteger.ONE);
        }

        @Override
        public BigInteger get() {
            BigInteger result=queue.pollLast().add(queue.peek());
            queue.push(result);
            return result;
        }
    }
    public static void main(String[] args) {
//        Stream<BigInteger> stream=Stream.generate(new Fei());
//        stream.limit(100).forEach(System.out::println);

        String[] a="A 12  c 13  d 15".split("\\s{2}");
        Stream<Person> personStream=Arrays.stream(a).map(s -> {
            String name=s.split("\\s")[0];
            int age=Integer.parseInt(s.split("\\s")[1]);
            return new Person(name,age);
        });
       List<Person> PER=personStream.collect(Collectors.toList());
       PER.forEach(System.out::println);

    }


}
