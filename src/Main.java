
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DCPU_16 dcpu = new DCPU_16();
		dcpu.reset();
		//aaaaaabbbbbooooo
		//0001010100100011
		dcpu.decode(0x1528);
		dcpu.dump();

	}

}
