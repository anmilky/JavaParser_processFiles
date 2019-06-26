package process_nofilted;

import java.util.ArrayList;

public class Test{


    public static String fencimethod(String s) {
        String final_s = "";
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("get");
        arrayList.add("set");
        for(String s1:arrayList){
            if(final_s.contains(s1)){
                System.out.println(final_s.indexOf(s1));
                System.out.println(final_s.indexOf(s1));

            }
        }

        for (char item : s.toCharArray()) {
            if (item > 64 && item < 91) {
                final_s = final_s + " ";
            }
            final_s = final_s + item;
        }
        return final_s;
    }

    public static void main(String[] args) {
        fencimethod("dasdsddsasetdasdsada");
    }


}