import javax.xml.bind.DatatypeConverter;

/**
 * Utils
 * Class for printing byte arrays and the conversions: byte[] <-> int
 * 
 * @author Geysen Stijn & Moons Marnix
 *
 */
public class Utils {
	
	/**
	 * To Bytes
	 * Convert the given int into a 4 byte array
	 * 
	 * @param i
	 * @return
	 */
	static byte[] toBytes(int i)
	{
	  byte[] result = new byte[4];

	  result[0] = (byte) (i >> 24);
	  result[1] = (byte) (i >> 16);
	  result[2] = (byte) (i >> 8);
	  result[3] = (byte) (i /*>> 0*/);

	  return result;
	}
	
	/**
	 * To Bytes
	 * Convert the given int into a byte array with given length of bytes.
	 * 
	 * @param i
	 * @param length
	 * @return
	 */
	static byte[] toBytes(int i, int length)
	{
	  byte[] result = new byte[length];
	  for(int k=0; k<length; k++){
		  result[k] = (byte) (i >> (length-1-k)*8);
	  }
	  return result;
	}
		
	/**
	 * From Bytes
	 * Convert the given bytes to an integer.
	 * 
	 * @param bytes
	 * @return
	 */
	static int fromBytes(byte[] bytes) {
		// | : bitwise OR
		// & : bitwise AND
		// 0xFF = 1111 1111 (nodig om byte unsigned te maken)
		int result = 0;
		for(int k=0; k<bytes.length; k++){
			result = result | ((bytes[k]  & 0xFF) << (bytes.length-1-k)*8);
		}
		return result;
	}
	
	/**
	 * Print Hex
	 * Print a DHCP message in hexadecimal format.
	 * 
	 * @param msg
	 */
	static void printHex(byte[] msg){
		for(byte b: msg){
		 System.out.format("%x",b);
		}
		System.out.println();
	}
	
	/**
	 * To Hex String
	 * Return a byte array into hexadecimal format.
	 * 
	 * @param array
	 * @return
	 */
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}

	/**
	 * To Byte Array
	 * Return a string converted to a byte array.
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
}
