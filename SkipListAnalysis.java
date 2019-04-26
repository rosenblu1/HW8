import java.io.PrintWriter;

public class SkipListAnalysis {

  public static void main(String[] args) {
   PrintWriter pen = new PrintWriter(System.out, true);
   SkipList<String, String> l = new SkipList<String,String>();
   
   String[] str = "alphbets".split("");

   for (int i = 0; i < str.length; i++) {
     l.set(str[i], str[i]);
   } // for
   
   pen.println("For size " + str.length + ", operations = " + l.counter);
   pen.flush();
   
   SkipList<String, String> l2 = new SkipList<String,String>();
   String[] str2 = "abcdefghijklm".split("");

   for (int i = 0; i < str2.length; i++) {
     l2.set(str2[i], str2[i]);
   } // for
   
   pen.println("For size " + str2.length + ", operations = " + l2.counter);
   pen.flush();
   
   SkipList<String, String> l3 = new SkipList<String,String>();
   String[] str3 = "abcdefghijklmopqrstuvwxyz".split("");

   for (int i = 0; i < str3.length; i++) {
     l3.set(str3[i], str3[i]);
   } // for
   
   pen.println("For size " + str3.length + ", operations = " + l3.counter);
   pen.flush();
   
   
  } //main

}
