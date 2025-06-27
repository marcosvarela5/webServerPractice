package es.udc.redes.tutorial.copy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;


public class Copy {

    public static void main(String[] args) throws IOException {

        if (args.length==2){

            try (FileInputStream in = new FileInputStream(args[0]); FileOutputStream out = new FileOutputStream(args[1])) {
                int c;

                while ((c = in.read()) != -1) {
                    out.write(c);
                }
            }
        }
        else
            System.out.println("File not found");
    }


}
