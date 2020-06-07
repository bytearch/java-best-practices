public class StringTest {
    public static void main(String[] args) {
        String a = "A";
        String b = "B";
        System.out.println(a + b);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            a += i;
        }
        System.out.println("++ time:" +  (System.currentTimeMillis() - start));
        long appenStart = System.currentTimeMillis();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            stringBuilder.append(i);
        }
        stringBuilder.toString();
        System.out.println("apendTime:" + (System.currentTimeMillis() - appenStart));
    }
}
