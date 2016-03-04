import java.util.Arrays;


public class test {
	public static void main(String args[]) throws Exception
	   {
		System.out.print("Test: ");	
		System.out.println(3 | 1);
		byte[] bt = (new String("1234")).getBytes();
			System.out.println(bt);
			System.out.println(bt.length);
			System.out.println(new String(bt, "UTF-8"));
			int te = -32768;
			byte[] bv = Utils.toBytes(te, 2);
			System.out.println(bv);
			System.out.println(bv.length);
			System.out.println(bv[0]);
			System.out.println(bv[1]);
			System.out.println(Utils.fromBytes(bv));
			byte[] tv = Arrays.copyOfRange(bt, 0, 0);
			System.out.println(tv.length);
	   }
}
