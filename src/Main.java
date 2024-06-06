public class Main {

    public static void main(String[] args) {
        Block block = new Block(8, 255);
        System.out.println(block);
        System.out.println(block.leftShift());
        System.out.println(block.modularMultByX());
    }
}
