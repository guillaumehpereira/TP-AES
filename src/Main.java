public class Main {

    public static void main(String[] args) {

        Block block = new Block(8, 13);
        //Block block2 = new Block(4, 6);
        int[][] paramS = {
                {9,4,10,11},
                {13,1,8,5},
                {6,2,0,3},
                {12,14,15,7}
        };
        SBox sbox = new SBox(paramS);
        System.out.println("block initial : " + block);
        //System.out.println(block2);
        System.out.println(block.g(sbox,block));

    }

}
