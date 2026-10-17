package algorithm;

public class Sort {
    public static void sortFromLowest(int [] arr) {
        for (int i = 0; i < arr.length; i++) {

            for (int j = i + 1; j < arr.length; j++) {

                int temp = 0;
                if (arr[j] < arr[i]) {

                    temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }
        }
    }
}
