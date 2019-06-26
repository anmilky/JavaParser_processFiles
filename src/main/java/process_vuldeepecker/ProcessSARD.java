package process_vuldeepecker;

import java.io.File;
import java.io.IOException;

public class ProcessSARD {

    String filePath;
    String filename;
    public ProcessSARD(String filename,String filePath){
        this.filename=filename;
        this.filePath=filePath;


    }


    public static void main(String[] args) throws IOException {
        File differdiroot = new File("C:\\Users\\troye sivan\\Desktop\\SARD_filesAndSlices\\SARD_reorganize\\CWE89");
        File[] filelists = differdiroot.listFiles();
        String cweType="CWE89";
        System.out.println(filelists.length);
        for (int i = 0; i < filelists.length; i++) {

//        for (int i = 0; i < 10; i++) {
//            System.out.println("------------------------------------");
            String filename= filelists[i].getName();
//            System.out.println(filename);
//            String filename="CWE190_Integer_Overflow__short_rand_preinc_52b.java";
            CompareNewOld compareNewOld=new CompareNewOld(cweType,filename);
            compareNewOld.getSlice();
        }
    }

}
