package project.gunay.demoSTT.stt;

/**
 * Created by Gunay Gultekin on 6/14/2017.
 */
public class complex {

    public static void main (String[] args) throws java.lang.Exception
    {
        String x = "abdbcde";
        String y = "bacde";
        System.out.println(lcs(x,y));
    }

    public static String lcs(String a, String b){
        int aLen = a.length();
        int bLen = b.length();
        if(aLen == 0 || bLen == 0){
            return "";
        }else if(a.charAt(aLen-1) == b.charAt(bLen-1)){
            String lcs = lcs(a.substring(0, aLen - 1), b.substring(0, bLen - 1));
            char c = a.charAt(aLen - 1);
            return lcs + c;
        }else{
            String x = lcs(a, b.substring(0,bLen-1));
            String y = lcs(a.substring(0,aLen-1), b);
            return (x.length() > y.length()) ? x : y;
        }
    }

}