package process_nofilted;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.exit;

public class  ReadDifferFile {

    String year;
    String differFile_path;
    String basedir = "C:\\Users\\troye sivan\\Desktop\\GithubDATA\\file descriptor\\javafiles\\";
    int lastline;
    public ReadDifferFile(String year,int lastline) {
        this.year = year;
        this.differFile_path = this.basedir + "changeline_" + this.year;
        this.lastline=lastline;
    }

    public void readDifferFile() throws IOException {
//        System.out.println(differFile_path);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(differFile_path));
        String line;
        int line_count=0;
        while ((line = bufferedReader.readLine()) != null) {
//            try {
            line_count = line_count+ 1;
            if (line_count > lastline) {
//                System.out.print(line_count+" : ");
                ArrayList<Integer> addlist = new ArrayList<>();
                ArrayList<Integer> delist = new ArrayList<>();
                String filename = "";
                //            System.out.println(line);
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
//            System.out.println(filename);
                ComPareNewOld comPareNewOld = new ComPareNewOld(year, filename, addlist, delist,basedir);
                comPareNewOld.getslice_nofilted();

            }


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
        String[] years={"2009","2010","2011","2012","2013","2014","2018"};
        for (String year:years
             ) {
            ReadDifferFile readDifferFile = new ReadDifferFile(year,0);
            readDifferFile.readDifferFile();
            System.out.println("finished "+year);
        }


}
}
