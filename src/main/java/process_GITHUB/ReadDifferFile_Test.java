package process_GITHUB;

import java.lang.String;

import java.io.IOException;
import java.util.ArrayList;

public class ReadDifferFile_Test {

    String year;
    String differFile_path;
    String basedir = "C:/Users/troye sivan/Desktop/fix_npe/javafiles/";

    public ReadDifferFile_Test(String year) {
        this.year = year;
        this.differFile_path = this.basedir + "changeline_" + this.year;

    }

    public void readDifferFile() throws IOException {


        ArrayList<Integer> addlist = new ArrayList<>();
        ArrayList<Integer> delist = new ArrayList<>();
        String filename="";
        String line = "fa7a91a5f4edccab4613b33d8137b546b7ae7380.java$Del$ADD 84 85 86";
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

            ComPareNewOld comPareNewOld = new ComPareNewOld(year, filename, addlist, delist);
            comPareNewOld.getslice_fileDiscriptor();
            int x=0;
            if(x<10){

            }


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
        ReadDifferFile_Test readDifferFile = new ReadDifferFile_Test("2017");
        readDifferFile.readDifferFile();

    }
}
