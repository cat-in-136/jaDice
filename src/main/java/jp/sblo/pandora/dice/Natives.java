package jp.sblo.pandora.dice;

public class Natives {
//    static {
//        System.loadLibrary("adice");
//    }
//
//    public static native boolean countIndexWordsNative( int[] params , byte[] buff ,  int[]indexPtr  );
    public static boolean countIndexWordsNative( int[] params , byte[] buff ,  int[]indexPtr  ) {
        boolean ret = false;

        if ( params != null && buff != null && indexPtr !=null ){
            int curidx = params[0];
            int curptr = params[1];
            int max = params[2];
            int buffmax = params[3];
            int blockbits= params[4];
            int found = params[5];
            int ignore = params[6];


            int i=0;

            //
            for( ;i<buffmax && curidx < max ;i++ ){
                if ( ignore > 0 ){
                    ignore--;
                }else if ( found != 0){
                    int ptr = curptr + i + blockbits;
                    indexPtr[curidx++] = ptr;
                    ignore = blockbits-1;
                    found = 0;
                }else if ( buff[i]==0 ){
                    found = 1;
                }
            }
            params[0] = curidx;
            params[1] = curptr+i ;
            params[5] = found ;
            params[6] = ignore ;
            if ( curidx < max ){
                ret = true;
            }else{
                ret = false;
            }
        }

        return ret;
    }
}
