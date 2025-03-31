package distributed.core;


public  class Tools {
    public static int[] transFormValueIndexToData(int index,int dataLength){
        int[] resultdata =new int[dataLength];
        for(int j = resultdata.length-1 ;j>= 0 ;j--){
            int tmp = (int) Math.pow(2,j);
            resultdata[j] = index/tmp;
            index = index%tmp;
        }
        return  resultdata;
    }


    public static int transFormDataToValueIndex(int[] inputData){
        int value = 0;
        for(int i=0;i<inputData.length;i++){
            value += (int) Math.pow(2,i)*inputData[i];
        }
        return value;
    }
}
