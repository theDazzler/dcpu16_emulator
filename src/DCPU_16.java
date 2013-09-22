
//1.7 specs
/*
 * * 16 bit words
 * 0x10000 words of ram
 * 8 registers (A, B, C, X, Y, Z, I, J)
 * program counter (PC)
 * stack pointer (SP)
 * extra/excess (EX)
 * interrupt address (IA)
 * 
 * instruction format: aaaaaabbbbbooooo
 */
public class DCPU_16 
{
	private static final int RAM_SIZE = 65536;
	private static final int SP_START_LOCATION = 0xffff;
	private static final int PC_START_LOCATION = 0;
	
	private int ram[] = new int[RAM_SIZE];
	private int A,B,C,X,Y,Z,I,J;
	private int PC;
	private int SP;
	private int EX;
	
	
	void reset()
	{
		//reset RAM
		for(int i = 0; i < this.ram.length; i++)
		{
			this.ram[i] = 0;
		}
		
		//reset registers
		this.A = 0;
		this.B = 0;
		this.C = 0;
		this.X = 0;
		this.Y = 0;
		this.Z = 0;
		this.I = 0;
		this.J = 0;
		
		this.SP = SP_START_LOCATION;
		this.PC = PC_START_LOCATION;
		this.EX = 0;
	}
	
	void decode(int instruction)
	{
		//instruction format: aaaaaabbbbbooooo
		int opcodeMask = 0x001F;
		int aMask = 0xFC00;
		int bMask = 0x03E0;
		
		int opcode = instruction & opcodeMask;
		int a = (instruction & aMask) >>> 10;
		int b = (instruction & bMask) >>> 5;
		System.out.println("a:" + a);
		System.out.println("b:" + b);
		System.out.println("opcode:0x" + String.format("%02x", opcode));
		
		switch(opcode)
		{
			case 0x01:
				this.SET(a);
				break;
			
			case 0x02:
				this.ADD(a, b);
				break;
				
			case 0x03:
				this.SUB(a, b);
				break;
				
			case 0x04:
				this.MUL(a, b);
				break;
				
			case 0x05:
				this.MLI(a, b);
				break;
				
			case 0x06:
				this.DIV(a, b);
				break;
				
			case 0x07:
				this.DVI(a, b);
				break;
				
			case 0x08:
				this.MOD(a, b);
				break;
				
			case 0x09:
				this.MDI(a, b);
				break;
				
			case 0x0A:
				this.AND(a, b);
				break;
				
			case 0x0B:
				this.BOR(a, b);
				break;
			
			case 0x0C:
				this.XOR(a, b);
				break;
				
			case 0x0D:
				this.SHR(a, b);
				break;
				
			case 0x0E:
				this.ASR(a, b);
				break;
				
			case 0x0F:
				this.SHL(a, b);
				break;
				
			case 0x10:
				this.IFB(a, b);
				break;
				
			case 0x11:
				this.IFC(a, b);
				break;
				
			case 0x12:
				this.IFE(a, b);
				break;
				
			case 0x13:
				this.IFN(a, b);
				break;
				
			case 0x14:
				this.IFG(a, b);
				break;
				
			case 0x15:
				this.IFA(a, b);
				break;
				
			case 0x16:
				this.IFL(a, b);
				break;
				
			case 0x17:
				this.IFU(a, b);
				break;
				
			case 0x1A:
				this.ADX(a, b);
				break;
				
			case 0x1B:
				this.SBX(a, b);
				break;
				
			case 0x1E:
				this.STI(a, b);
				break;
				
			case 0x1F:
				this.STD(a, b);
				break;
		}
		
	}
	
	//1 | 0x01 | SET b, a | sets b to a
	void SET(int a)
	{
		this.B = a;
		this.handleCycles(1);
	}
	
	

	//TODO: detect overflow
	//2 | 0x02 | ADD b, a | sets b to b+a, sets EX to 0x0001 if there's an overflow, 0x0 otherwise
	void ADD(int a, int b)
	{
		this.B = b + a;
		this.PC++;
		
		this.handleCycles(2);
	}
	
	//TODO: detect overflow
	//2 | 0x03 | SUB b, a | sets b to b-a, sets EX to 0xffff if there's an underflow, 0x0 otherwise
	void SUB(int a, int b)
	{
		this.B = b - a;
		this.PC++;
		
		this.handleCycles(2);
	}
	
	// 2 | 0x04 | MUL b, a | sets b to b*a, sets EX to ((b*a)>>16)&0xffff (treats b, a as unsigned)
	void MUL(int a, int b)
	{
		this.B = b * a;
		this.EX = ((b * a) >> 16) & 0xFFFF;
		
		this.PC++;
		
		this.handleCycles(2);
	}
	
	//2 | 0x05 | MLI b, a | like MUL, but treat b, a as signed
	void MLI(int a, int b)
	{
		short aSigned = (short) a;
		short bSigned = (short) b;
		
		this.B = bSigned * aSigned;
		this.EX = ((bSigned * aSigned) >> 16) & 0xFFFF;
		
		this.PC++;
		
		this.handleCycles(2);
		
	}
	
	 //3 | 0x06 | DIV b, a | sets b to b/a, sets EX to ((b<<16)/a)&0xffff. if a==0,  sets b and EX to 0 instead. (treats b, a as unsigned)
	void DIV(int a, int b)
	{		
		if(a == 0)
		{
			this.B = 0;
			this.EX = 0;
		}
		else
		{
			this.B = b / a;
			this.EX = ((b << 16 / a) & 0xFFFF);
		}
		
		this.PC++;
		
		this.handleCycles(3);
		
	}
	
	// 3 | 0x07 | DVI b, a | like DIV, but treat b, a as signed. Rounds towards 0
	void DVI(int a, int b)
	{
		short aSigned = (short) a;
		short bSigned = (short) b;

		if(aSigned == 0)
		{
			this.B = 0;
			this.EX = 0;
		}
		else
		{
			this.B = bSigned / aSigned;
			this.EX = ((bSigned << 16 / aSigned) & 0xFFFF);
		}
		
		this.PC++;
		
		this.handleCycles(3);
	}
	
	//3 | 0x08 | MOD b, a | sets b to b%a. if a==0, sets b to 0 instead.
	void MOD(int a, int b)
	{
		if(a ==0)
			this.B = 0;
		else
			this.B = b % a;
		
		this.PC++;
		this.handleCycles(3);
	}
	
	//3 | 0x09 | MDI b, a | like MOD, but treat b, a as signed. (MDI -7, 16 == -7)
	void MDI(int a, int b)
	{
		short aSigned = (short) a;
		short bSigned = (short) b;
		
		if(aSigned == 0)
			this.B = 0;
		else
			this.B = bSigned % aSigned;
		
		this.PC++;
		this.handleCycles(3);
	}
	
	//1 | 0x0A | AND b, a | sets b to b&a
	void AND(int a, int b)
	{
		this.B = b & a;
		
		this.PC++;
		this.handleCycles(1);
	}
	
	//1 | 0x0B | BOR b, a | sets b to b|a
	void BOR(int a, int b)
	{
		this.B = b | a;
		
		this.PC++;
		this.handleCycles(1);
	}
	
	//1 | 0x0C | XOR b, a | sets b to b^a
	private void XOR(int a, int b)
	{
		this.B = b ^ a;
		
		this.PC++;
		this.handleCycles(1);
	}
	
	//1 | 0x0D | SHR b, a | sets b to b>>>a, sets EX to ((b<<16)>>a)&0xffff (logical shift)
	void SHR(int a, int b)
	{
		this.B = b >>> a;
		this.EX = ((b << 16) >> a) & 0xFFFF;
		
		this.PC++;
		this.handleCycles(1);
	}
	
	//1 | 0x0E | ASR b, a | sets b to b>>a, sets EX to ((b<<16)>>>a)&0xffff  (arithmetic shift) (treats b as signed)
	void ASR(int a, int b)
	{
		short aSigned = (short) a;
		short bSigned = (short) b;
		
		this.B = bSigned >> aSigned;
		this.EX = ((b << 16) >>> a) & 0xFFFF;
		
		this.PC++;
		this.handleCycles(1);
	}
	
	//1 | 0x0F | SHL b, a | sets b to b<<a, sets EX to ((b<<a)>>16)&0xffff
	private void SHL(int a, int b)
	{
		this.B = b << a;
		this.EX = ((b << a) >> 16) & 0xFFFF;
		
		this.PC++;
		this.handleCycles(1);
	}
	
	//2+| 0x10 | IFB b, a | performs next instruction only if (b&a)!=0
	private void IFB(int a, int b)
	{
		if((b & a) != 0)
		{
			this.PC++;
		}
		
		this.handleCycles(2);
	}
	
	//2+| 0x11 | IFC b, a | performs next instruction only if (b&a)==0
	private void IFC(int a, int b)
	{
		if((b&a) == 0)
		{
			this.PC++;
		}
		
		this.handleCycles(2);
	}
	
	// 2+| 0x12 | IFE b, a | performs next instruction only if b==a 
	private void IFE(int a, int b)
	{
		if(b == a)
		{
			this.PC++;
		}
		
		this.handleCycles(2);
	}
	
	//2+| 0x13 | IFN b, a | performs next instruction only if b!=a 
	private void IFN(int a, int b)
	{
		if(b != a)
		{
			this.PC++;
		}
		
		this.handleCycles(2);
	}
	
	//2+| 0x14 | IFG b, a | performs next instruction only if b>a 
	private void IFG(int a, int b)
	{
		if(b > a)
		{
			this.PC++;
		}
		
		this.handleCycles(2);
	}
	
	//2+| 0x15 | IFA b, a | performs next instruction only if b>a (signed)
	private void IFA(int a, int b)
	{
		short aSigned = (short) a;
		short bSigned = (short) b;
		
		if(bSigned > aSigned)
		{
			this.PC++;
		}
		
		this.handleCycles(2);
	}
	
	//2+| 0x16 | IFL b, a | performs next instruction only if b<a
	private void IFL(int a, int b)
	{
		if(b < a)
		{
			this.PC++;
		}
		
		this.handleCycles(2);
	}
	
	// 2+| 0x17 | IFU b, a | performs next instruction only if b<a (signed)
	private void IFU(int a, int b)
	{
		short aSigned = (short) a;
		short bSigned = (short) b;
		
		if(bSigned < aSigned)
		{
			this.PC++;	
		}
		
		this.handleCycles(2);
	}
	
	//TODO: detect overflow
	//3 | 0x1A | ADX b, a | sets b to b+a+EX, sets EX to 0x0001 if there is an over-flow, 0x0 otherwise
	private void ADX(int a, int b)
	{
		this.B = b + a + this.EX;
		this.PC++;
		
		this.handleCycles(3);
	}
	
	//TODO: detect underflow
	//3 | 0x1B | SBX b, a | sets b to b-a+EX, sets EX to 0xFFFF if there is an under-flow, 0x0 otherwise
	private void SBX(int a, int b)
	{
		this.B = b - a + this.EX;
		
		this.PC++;
		this.handleCycles(3);
	}
	
	//2 | 0x1E | STI b, a | sets b to a, then increases I and J by 1
	private void STI(int a, int b)
	{
		this.B = a;
		this.I++;
		this.J++;
		
		this.PC++;
		this.handleCycles(2);
	}
	
	//2 | 0x1F | STD b, a | sets b to a, then decreases I and J by 1
	private void STD(int a, int b)
	{
		this.B = a;
		this.J--;
		this.I--;
		
		this.PC++;
		this.handleCycles(2);
	}
	
	private void handleCycles(int k) {
		// TODO Auto-generated method stub
		
	}
	
	void dump()
	{
		System.out.println("Registers:");
		
		System.out.println("A:" + String.format("%04x",this.A));
		System.out.println("B:" + String.format("%04x",this.B));
		System.out.println("C:" + String.format("%04x",this.C));
		System.out.println("X:" + String.format("%04x",this.X));
		System.out.println("Y:" + String.format("%04x",this.Y));
		System.out.println("Z:" + String.format("%04x",this.Z));
		System.out.println("I:" + String.format("%04x",this.I));
		System.out.println("J:" + String.format("%04x",this.J));
		
		System.out.println("PC:" + String.format("%04x",this.PC));
		System.out.println("SP:" + String.format("%04x", this.SP));
		
		System.out.println("EX:" + String.format("%04x", this.EX));
		
		
	}
	
}
