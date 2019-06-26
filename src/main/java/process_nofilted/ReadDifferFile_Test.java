package process_nofilted;

import java.io.IOException;
import java.util.ArrayList;

public class ReadDifferFile_Test {

    String year;
    String differFile_path;
    String basedir = "C:/Users/troye sivan/ProcessJavaFile/src/main/java/test/";

    public ReadDifferFile_Test(String year) {
        this.year = year;
        this.differFile_path = this.basedir + "changeline_" + this.year;

    }

    public void readDifferFile() throws IOException {


        ArrayList<Integer> addlist = new ArrayList<>();
        ArrayList<Integer> delist = new ArrayList<>();
        String filename="";
        String line = "Test.java$Del $ADD 16 17 18";
        for (String item : line.split("\\$")) {

            if (item.startsWith("Del")) {
                item = item.replace("Del", "").strip();
                delist = getADDandDellist(item);

            } else if (item.startsWith("ADD")) {
                item = item.replace("ADD", "").strip();
                addlist = getADDandDellist(item);
            } else {
                filename = item.strip();
            }
        }

            ComPareNewOld comPareNewOld = new ComPareNewOld(year, filename, addlist, delist,basedir);
            comPareNewOld.getslice_nofilted();



    }

    public ArrayList<Integer> getADDandDellist(String item) {

        ArrayList<Integer> addlist = new ArrayList<>();

        if (item != "" && item != null) {
            for (String col : item.split("\\s+")) {

                int tmp = Integer.parseInt(col);
                addlist.add(tmp);
            }


        }

        return addlist;

    }

    public static void main(String[] args) throws IOException {
        ReadDifferFile_Test readDifferFile = new ReadDifferFile_Test("2019");
        readDifferFile.readDifferFile();

    }
}
