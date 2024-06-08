import java.util.Arrays;

class Block implements Cloneable{
	boolean block[];
	public final static boolean[] AESmod = {false,false,false,true,true,false,true,true};

	/* Test de la méthode modularMult
	public final static boolean[] AESmod = {false,false,true,true};*/

	public final static Block AESmodulo = new Block(AESmod);
	
	public Block(int taille) {
		this.block=new boolean[taille];
	}
	
	public Block(int taille, int val) {
		this(taille);
		for(int i = taille - 1; i > -1; i--) {
			this.block[i] = ((val % 2) == 1);
			val /= 2;
		}
	}
	
    public Block(boolean[] s){
        this(s.length);
        for(int i = 0; i < s.length; i++) {
        	this.block[i] = s[i];
        }
    }

	public Block(String s){
		this(s.length());
		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i)=='0')
				this.block[i]=false;
			else {
				if(s.charAt(i)=='1') {
					this.block[i]=true;
				}
				else {
					System.out.println("Block: bit "+i+" has value "+s.charAt(i)+" different from 0 or 1");
				}
			}
		}
	}
	
	public Block(Block[] blockList) {
		int taille = 0, cpt = 0;
		for(int i = 0; i < blockList.length; i++) {
			taille += blockList[i].block.length;
		}
		this.block=new boolean[taille];
		for(int i = 0; i < blockList.length; i++) {
			for(int j = 0; j < blockList[i].block.length; j++) {
				this.block[cpt++] = blockList[i].block[j];
			}
		}
	}
	
	public static Block[] stringToBlock(String chaine, int size) {
		Block[] toReturn = new Block[chaine.length() / size];
		for(int i = 0; i < toReturn.length; i++) {
			Block[] temp = new Block[size];
			for(int j = 0; j < size; j++) {
				Block octetBlock = new Block(8);
				char octet = chaine.charAt(i * 8 + j);
				for(int k = 0; k < 8; k++) {
					octetBlock.block[7 - k] = (octet % 2 == 1);
					octet = (char)(octet / 2);
				}
				temp[j] = octetBlock;
			}
			toReturn[i] = new Block(temp);
		}
		return toReturn;
	}
	
	public static String blockToString(Block[] blocks) {
		String toReturn = "";
		for(int i = 0; i < blocks.length; i++) {
			Block block = blocks[i];
			for(int j = 0; j < block.block.length / 8; j++) {
				char val = 0, pow2 = 1;
				for(int k = 0; k < 8; k++) {
					if(block.block[j * 8 + 7 - k]) {
						val += pow2;
					}
					pow2 *= 2;
				}
				toReturn += val;
			}
		}
		return toReturn;
	}

	public Block clone() {
		Block clone=new Block(this.block);
		return clone;
	}


	public String toString() {
		String result="";
		for(int i = 0; i < this.block.length; i++) {
			result += this.block[i]? "1": "0";
		}
		return result;
	}
	
	public String toStringH() {
		String result="";
		for(int i = 0; i < this.block.length; i += 4) {
			int val = (this.block[i]? 8: 0) + (this.block[i+1]? 4: 0) + (this.block[i+2]? 2: 0) + (this.block[i+3]? 1: 0);
			if(val < 10) {
				result += val;
			}
			else {
				result += ((char)('A' + val - 10));
			}
		}
		return result;
	}
	
	public Block portion(int nbrPortion, int index) {
		boolean[] newBlock = new boolean[this.block.length / nbrPortion];
		for(int i = 0; i < newBlock.length; i++) {
			newBlock[i] = this.block[i + index * newBlock.length];
		}
		return new Block(newBlock);
	}
	
	public Block xOr(Block secondMember) {
		Block xOrBlock = new Block(this.block.length);
		for(int i = 0; i < this.block.length;i++){
			xOrBlock.block[i] = this.block[i] ^ secondMember.block[i];
		}
		return xOrBlock;
	}
	
	public Block leftShift() {
		Block leftShiftTemp = new Block(this.block.length+1);
		Block leftShifted = new Block(this.block);

		System.arraycopy(this.block, 0, leftShiftTemp.block, 0, this.block.length);

		leftShiftTemp.block[block.length] = false;
		leftShifted.block = Arrays.copyOfRange(leftShiftTemp.block, 1, leftShiftTemp.block.length);

		return leftShifted;
	}
	
	public int rowValue() {
		return (this.block[0]? 8: 0) + (this.block[1]? 4: 0) + (this.block[2]? 2: 0) + (this.block[3]? 1: 0);
	}
	
	public int columnValue() {
		return (this.block[4]? 8: 0) + (this.block[5]? 4: 0) + (this.block[6]? 2: 0) + (this.block[7]? 1: 0);
	}
	
	public Block modularMultByX() {
		Block temp = leftShift();
		if(this.block[0])
			temp = temp.xOr(new Block(AESmod));
		return temp;
	}
	
	public Block modularMult(Block prod) {
		Block resBlock = new Block(this.block.length);
		Block temp = new Block(this.block.length);
		for(int i = 0; i < prod.block.length; i++) {
			System.arraycopy(this.block, 0, temp.block, 0, this.block.length);
			if (prod.block[i]) {
				for (int j = prod.block.length - i - 1; j > 0; j--) {
					temp = temp.modularMultByX();
				}
				System.out.println(temp);
				//première itération
				if(i == 0)
					resBlock = temp;
				//les autres faire juste le xOr
				else
					resBlock = resBlock.xOr(temp);
				System.out.println(resBlock);
			}
		}
		return resBlock;
	}
	
	public Block g(SBox sbox, Block rc) {

		// split in four
		Block n0 = new Block(this.block.length/4);
		Block n1 = new Block(this.block.length/4);
		Block n2 = new Block(this.block.length/4);
		Block n3 = new Block(this.block.length/4);

		// divide in four byte
		System.arraycopy(this.block, 8, n1.block, 0, 8);
		System.arraycopy(this.block, 16, n2.block, 8, 8);
		System.arraycopy(this.block, 24, n3.block, 16, 8);
		System.arraycopy(this.block, 0, n0.block, 24, 8);

		// swap not necessary

		// Substitute with Sbox
		Block n1Sub = new Block(n1.block.length);
		Block n2Sub = new Block(n2.block.length);
		Block n3Sub = new Block(n3.block.length);
		Block n0Sub = new Block(n0.block.length);

		//System.out.println("n0 : " + n0);
		//System.out.println("n1 : " + n1);
		//System.out.println("n2 : " + n2);
		//System.out.println("n3 : " + n3);

		n1Sub = sbox.cypher(n1);
		n2Sub = sbox.cypher(n2);
		n3Sub = sbox.cypher(n3);
		n0Sub = sbox.cypher(n0);

		//System.out.println("n0 après cypher : " + n0Sub);
		//System.out.println("n1 apres cypher : " + n1Sub);
		//System.out.println("n2 apres cypher : " + n2Sub);
		//System.out.println("n3 apres cypher : " + n3Sub);

		// concatenation et xOr dans le premier
		Block subbedBlock = new Block(this.block.length);
		System.arraycopy(n1Sub.xOr(rc).block, 0, subbedBlock.block, 0, 8);
		System.arraycopy(n2Sub.block, 0, subbedBlock.block, 8, 8);
		System.arraycopy(n3Sub.block, 0, subbedBlock.block, 16, 8);
		System.arraycopy(n0Sub.block, 0, subbedBlock.block, 24, 8);

		return subbedBlock;
	}
}

class Key{
	private Block[] bytes;
	
	public Key() {
		this.bytes = new Block[4];
		for(int i = 0; i < 4; i++) {
			this.bytes[i] = new Block(32);
		}
	}
	
	public Key(Block block) {
		this.bytes = new Block[4];
		for(int i = 0; i < 4; i++) {
			this.bytes[i] = block.portion(4, i);
		}
	}
	
	public Key(Block[] blocks) {
		this.bytes = new Block[4];
		for(int i = 0; i < 4; i++) {
			this.bytes[i] = blocks[i].clone();
		}
	}
	
	public Key(Key toCopy) {
		this.bytes = new Block[4];
		for(int i = 0; i < 4; i++) {
			this.bytes[i] = toCopy.bytes[i].clone();
		}
	}
	
	public Key[] genSubKeys(SBox sbox) {

		//Tableau des 11 clés distinctes
		Key[] keys = new Key[11];

		//Copie de la première clé
		keys[0] = new Key(this);

		//rc init à x^0
		Block rc = new Block(8,1);

		//Boucle pour créer les 10 clés restances
		for(int i = 1; i < 11; i++){

			// Pour 4 block de 32 bits
			Block w0 = this.bytes[0];
			Block w1 = this.bytes[1];
			Block w2 = this.bytes[2];
			Block w3 = this.bytes[3];

			//Générer les nouvelles sous-clés
			w0 = w0.xOr(w3.g(sbox,rc));
			w1 = w1.xOr(w0);
			w2 = w2.xOr(w1);
			w3 = w3.xOr(w2);

			//Créer la nouvelle clé grâce aux nouvelles sous-clés
			Key tempKey = new Key(new Block[]{w0,w1,w2,w3});

			//Copie de la nouvelle clé dans keys
			keys[i] = new Key(tempKey);

			//Multiplication de RC par x
			rc = rc.modularMultByX();
		}
		return keys;
	}
	
	public Block elmnt(int i, int j) {
		return this.bytes[i].portion(4, j);
	}
	
	public String toString() {
		String s = "";
		for(int i = 0; i < this.bytes.length; i++) {
			s += this.bytes[i].toString() + " ";
		}
		return s;
	}
	
	public String toStringH() {
		String s = "";
		for(int i = 0; i < this.bytes.length; i++) {
			s += this.bytes[i].toStringH() + " ";
		}
		return s;
	}
}

class State{
	private Block[][] bytes;
	
	public State() {
		this.bytes = new Block[4][4];
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				this.bytes[i][j] = new Block(8);
			}
		}
	}
	
	public State(Block block) {
		this.bytes = new Block[4][4];
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				this.bytes[i][j] = block.portion(16, i + j * 4);
			}
		}
	}
	
	public State(State toCopy) {
		this.bytes = new Block[4][4];
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				this.bytes[i][j] = toCopy.bytes[i][j].clone();
			}
		}
	}
	
	public State(int[][] val) {
		this.bytes = new Block[4][4];
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				this.bytes[i][j] = new Block(8,val[i][j]);
			}
		}
	}
	
	public State substitute(SBox sbox) {
		State newState = new State();
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 4; j++){
				//vérifier si w0, w1, w2 et w3 sont rangés en ligne ou en colonne
				newState.bytes[i][j] = sbox.cypher(this.bytes[i][j]);
			}
		}
		return newState;
	}
	
	public State shift() {
		State newState = new State(this);
		for (int i = 1; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				newState.bytes[i][j] = this.bytes[i][(j + i) % 4];
			}
		}
		return newState;
	}
	
	public State shiftInv() {
		State newState = new State(this);
		for (int i = 1; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				newState.bytes[i][(j + i) % 4] = this.bytes[i][j];
			}
		}
		return newState;
	}
	
	public State mult(State prod) {
		State newState = new State();
		for (int col = 0; col < 4; col++) {
			for (int row = 0; row < 4; row++) {
				Block result = new Block(8);
				for (int k = 0; k < 4; k++) {
					result = result.xOr(this.bytes[k][col].modularMult(prod.bytes[row][k]));
				}
				newState.bytes[row][col] = result;
			}
		}
		return newState;
	}
	
	public State xOr(Key key) {
		State newState = new State();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				newState.bytes[i][j] = this.bytes[i][j].xOr(key.elmnt(i, j));
			}
		}
		return newState;
	}
	
	public Block block() {
		Block[] blocks = new Block[16];
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				blocks[4 * j + i] = this.bytes[i][j];
			}
		}
		return new Block(blocks);
	}
	
	public String toString() {
		String s = "";
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				s += this.bytes[i][j] + " ";
			}
			s += "\n";
		}
		return s;
	}
}

class SBox{
	private int[][] matrix;
	
	public SBox(int[][] matrix) {
		this.matrix = new int[matrix.length][];
		for(int i = 0; i < matrix.length; i++) {
			this.matrix[i] = new int[matrix[i].length];
			for(int j = 0; j < matrix[i].length; j++) {
				this.matrix[i][j] = matrix[i][j];
			}
		}
	}
	
	public Block cypher(Block toCypher) {

		int toCypherRowValue = toCypher.rowValue();
		int toCypherColumnValue = toCypher.columnValue();
		//System.out.println("toCypher row value : " + toCypherRowValue);
		//System.out.println("toCypher Column value : " + toCypherColumnValue);

		int decimalValue = this.matrix[toCypherRowValue][toCypherColumnValue];
		//System.out.println("Decimal value : " + decimalValue);

		// transformer le int en boolean block
		Block cypherBlock = new Block(toCypher.block.length, decimalValue);

		return cypherBlock;

	}
}

public class AES {
	private Key[] keys;
	private SBox sbox, sboxInv;
	private State mixState, mixStateInv;
	private static int[][] s = {{0x63,0x7C,0x77,0x7B,0xF2,0x6B,0x6F,0xC5,0x30,0x01,0x67,0x2B,0xFE,0xD7,0xAB,0x76},{0xCA,0x82,0xC9,0x7D,0xFA,0x59,0x47,0xF0,0xAD,0xD4,0xA2,0xAF,0x9C,0xA4,0x72,0xC0},{0xB7,0xFD,0x93,0x26,0x36,0x3F,0xF7,0xCC,0x34,0xA5,0xE5,0xF1,0x71,0xD8,0x31,0x15},{0x04,0xC7,0x23,0xC3,0x18,0x96,0x05,0x9A,0x07,0x12,0x80,0xE2,0xEB,0x27,0xB2,0x75},{0x09,0x83,0x2C,0x1A,0x1B,0x6E,0x5A,0xA0,0x52,0x3B,0xD6,0xB3,0x29,0xE3,0x2F,0x84},{0x53,0xD1,0x00,0xED,0x20,0xFC,0xB1,0x5B,0x6A,0xCB,0xBE,0x39,0x4A,0x4C,0x58,0xCF},{0xD0,0xEF,0xAA,0xFB,0x43,0x4D,0x33,0x85,0x45,0xF9,0x02,0x7F,0x50,0x3C,0x9F,0xA8},{0x51,0xA3,0x40,0x8F,0x92,0x9D,0x38,0xF5,0xBC,0xB6,0xDA,0x21,0x10,0xFF,0xF3,0xD2},{0xCD,0x0C,0x13,0xEC,0x5F,0x97,0x44,0x17,0xC4,0xA7,0x7E,0x3D,0x64,0x5D,0x19,0x73},{0x60,0x81,0x4F,0xDC,0x22,0x2A,0x90,0x88,0x46,0xEE,0xB8,0x14,0xDE,0x5E,0x0B,0xDB},{0xE0,0x32,0x3A,0x0A,0x49,0x06,0x24,0x5C,0xC2,0xD3,0xAC,0x62,0x91,0x95,0xE4,0x79},{0xE7,0xC8,0x37,0x6D,0x8D,0xD5,0x4E,0xA9,0x6C,0x56,0xF4,0xEA,0x65,0x7A,0xAE,0x08},{0xBA,0x78,0x25,0x2E,0x1C,0xA6,0xB4,0xC6,0xE8,0xDD,0x74,0x1F,0x4B,0xBD,0x8B,0x8A},{0x70,0x3E,0xB5,0x66,0x48,0x03,0xF6,0x0E,0x61,0x35,0x57,0xB9,0x86,0xC1,0x1D,0x9E},{0xE1,0xF8,0x98,0x11,0x69,0xD9,0x8E,0x94,0x9B,0x1E,0x87,0xE9,0xCE,0x55,0x28,0xDF},{0x8C,0xA1,0x89,0x0D,0xBF,0xE6,0x42,0x68,0x41,0x99,0x2D,0x0F,0xB0,0x54,0xBB,0x16}};
	private static int[][] sInv = {{0x52,0x09,0x6A,0xD5,0x30,0x36,0xA5,0x38,0xBF,0x40,0xA3,0x9E,0x81,0xF3,0xD7,0xFB},{0x7C,0xE3,0x39,0x82,0x9B,0x2F,0xFF,0x87,0x34,0x8E,0x43,0x44,0xC4,0xDE,0xE9,0xCB},{0x54,0x7B,0x94,0x32,0xA6,0xC2,0x23,0x3D,0xEE,0x4C,0x95,0x0B,0x42,0xFA,0xC3,0x4E},{0x08,0x2E,0xA1,0x66,0x28,0xD9,0x24,0xB2,0x76,0x5B,0xA2,0x49,0x6D,0x8B,0xD1,0x25},{0x72,0xF8,0xF6,0x64,0x86,0x68,0x98,0x16,0xD4,0xA4,0x5C,0xCC,0x5D,0x65,0xB6,0x92},{0x6C,0x70,0x48,0x50,0xFD,0xED,0xB9,0xDA,0x5E,0x15,0x46,0x57,0xA7,0x8D,0x9D,0x84},{0x90,0xD8,0xAB,0x00,0x8C,0xBC,0xD3,0x0A,0xF7,0xE4,0x58,0x05,0xB8,0xB3,0x45,0x06},{0xD0,0x2C,0x1E,0x8F,0xCA,0x3F,0x0F,0x02,0xC1,0xAF,0xBD,0x03,0x01,0x13,0x8A,0x6B},{0x3A,0x91,0x11,0x41,0x4F,0x67,0xDC,0xEA,0x97,0xF2,0xCF,0xCE,0xF0,0xB4,0xE6,0x73},{0x96,0xAC,0x74,0x22,0xE7,0xAD,0x35,0x85,0xE2,0xF9,0x37,0xE8,0x1C,0x75,0xDF,0x6E},{0x47,0xF1,0x1A,0x71,0x1D,0x29,0xC5,0x89,0x6F,0xB7,0x62,0x0E,0xAA,0x18,0xBE,0x1B},{0xFC,0x56,0x3E,0x4B,0xC6,0xD2,0x79,0x20,0x9A,0xDB,0xC0,0xFE,0x78,0xCD,0x5A,0xF4},{0x1F,0xDD,0xA8,0x33,0x88,0x07,0xC7,0x31,0xB1,0x12,0x10,0x59,0x27,0x80,0xEC,0x5F},{0x60,0x51,0x7F,0xA9,0x19,0xB5,0x4A,0x0D,0x2D,0xE5,0x7A,0x9F,0x93,0xC9,0x9C,0xEF},{0xA0,0xE0,0x3B,0x4D,0xAE,0x2A,0xF5,0xB0,0xC8,0xEB,0xBB,0x3C,0x83,0x53,0x99,0x61},{0x17,0x2B,0x04,0x7E,0xBA,0x77,0xD6,0x26,0xE1,0x69,0x14,0x63,0x55,0x21,0x0C,0x7D}};
	private static int[][] mix = {{2,3,1,1},{1,2,3,1},{1,1,2,3},{3,1,1,2}}, mixInv = {{14,11,13,9},{9,14,11,13},{13,9,14,11},{11,13,9,14}};
	
	public AES(Block key) {
		//TODO
	}
	
	public Block cypher(Block plaintext) {
		//TODO
		return null;
	}
	
	public Block deCypher(Block cyphertext) {
		//TODO
		return null;
	}
	
	public static void main(String[] args) {
		String plaintext = "00000001001000110100010101100111100010011010101111001101111011111111111011011100101110101001100001110110010101000011001000010000";
		String key = "00001111000101010111000111001001010001111101100111101000010110010000110010110111101011011101011010101111011111110110011110011000";
		Block plaintextBlock = new Block(plaintext), keyBlock = new Block(key);
		AES aes = new AES(keyBlock);
		Block cypherBlock = aes.cypher(plaintextBlock);
		System.out.println(cypherBlock);
		Block deCypherBlock = aes.deCypher(cypherBlock);
		System.out.println(deCypherBlock);
		System.out.println(deCypherBlock.toString().compareTo(plaintext));
	}
}
