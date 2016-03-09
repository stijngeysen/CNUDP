import javax.xml.bind.DatatypeConverter;


public class Utils {
	
	//CONVERSIONS: byte[] <-> int
	
	//for 4 byte array
	static byte[] toBytes(int i)
	{
	  byte[] result = new byte[4];

	  result[0] = (byte) (i >> 24);
	  result[1] = (byte) (i >> 16);
	  result[2] = (byte) (i >> 8);
	  result[3] = (byte) (i /*>> 0*/);

	  return result;
	}
	
	//for variable byte length
	static byte[] toBytes(int i, int length)
	{
	  byte[] result = new byte[length];
	  for(int k=0; k<length; k++){
		  result[k] = (byte) (i >> (length-1-k)*8);
	  }
	  return result;
	}
	
	// | : bitwise OR
	// & : bitwise AND
	// 0xFF = 1111 1111 (nodig om byte unsigned te maken)	
	static int fromBytes(byte[] bytes) {
		int result = 0;
		for(int k=0; k<bytes.length; k++){
			result = result | ((bytes[k]  & 0xFF) << (bytes.length-1-k)*8);
		}
		return result;
	}
	
	static void printHex(byte[] msg){
		for(byte b: msg){
		 System.out.format("%x",b);
		}
		System.out.println();
	}
	
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
}
