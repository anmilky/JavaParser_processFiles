package process_SARD;

import java.lang.String;

import java.io.*;

public class ProcessSARD {

    String filePath;
    String filename;
    public ProcessSARD(String filename,String filePath){
        this.filename=filename;
        this.filePath=filePath;


    }


    public static void main(String[] args) throws IOException {
        File differdiroot = new File("C:\\Users\\troye sivan\\Desktop\\SARD_filesAndSlices\\SARD_reorganize\\CWE190");
        File[] filelists = differdiroot.listFiles();
        String cweType="CWE190";
        System.out.println(filelists.length);
//        for (int i = 0; i < filelists.length; i++) {
        for (int i = 0; i < filelists.length; i++) {
            String filename= filelists[i].getName();
//            System.out.println(filename);
            CompareNewOld compareNewOld=new CompareNewOld(cweType,filename);
            compareNewOld.getSlice();
        }
    }

}
