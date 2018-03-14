package com.github.rosklyar.utils;

import java.math.BigInteger;

/**
 * Static class that contains a handful of array helper functions.
 */
public class ArrayUtils {


	/**
	 * Concatenates byte arrays and returns the result.
	 *
	 * @param arrays The arrays.
	 * @return A single array containing all elements in all arrays.
	 */
	public static byte[] concat(final byte[]... arrays) {
		int totalSize = 0;
		for (final byte[] array : arrays) {
			totalSize += array.length;
		}

		int startIndex = 0;
		final byte[] result = new byte[totalSize];
		for (final byte[] array : arrays) {
			System.arraycopy(array, 0, result, startIndex, array.length);
			startIndex += array.length;
		}

		return result;
	}


	/**
	 * Converts a BigInteger to a little endian byte array.
	 *
	 * @param value The value to convert.
	 * @param numBytes The number of bytes in the destination array.
	 * @return The resulting little endian byte array.
	 */
	public static byte[] toByteArray(final BigInteger value, final int numBytes) {
		final byte[] outputBytes = new byte[numBytes];
		final byte[] bigIntegerBytes = value.toByteArray();

		int copyStartIndex = (0x00 == bigIntegerBytes[0]) ? 1 : 0;
		int numBytesToCopy = bigIntegerBytes.length - copyStartIndex;
		if (numBytesToCopy > numBytes) {
			copyStartIndex += numBytesToCopy - numBytes;
			numBytesToCopy = numBytes;
		}

		for (int i = 0; i < numBytesToCopy; ++i) {
			outputBytes[i] = bigIntegerBytes[copyStartIndex + numBytesToCopy - i - 1];
		}

		return outputBytes;
	}

	/**
	 * Converts a little endian byte array to a BigInteger.
	 *
	 * @param bytes The bytes to convert.
	 * @return The resulting BigInteger.
	 */
	public static BigInteger toBigInteger(final byte[] bytes) {
		final byte[] bigEndianBytes = new byte[bytes.length + 1];
		for (int i = 0; i < bytes.length; ++i) {
			bigEndianBytes[i + 1] = bytes[bytes.length - i - 1];
		}

		return new BigInteger(bigEndianBytes);
	}

	/**
	 * Constant-time byte[] comparison. The constant time behavior eliminates side channel attacks.
	 *
	 * @param b An array.
	 * @param c An array.
	 * @return 1 if b and c are equal, 0 otherwise.
	 */
	public static int isEqualConstantTime(final byte[] b, final byte[] c) {
		int result = 0;
		result |= b.length - c.length;
		for (int i = 0; i < b.length; i++) {
			result |= b[i] ^ c[i];
		}

		return ByteUtils.isEqualConstantTime(result, 0);
	}

	/**
	 * Gets the i'th bit of a byte array.
	 *
	 * @param h The byte array.
	 * @param i The bit index.
	 * @return The value of the i'th bit in h
	 */
	public static int getBit(final byte[] h, final int i) {
		return (h[i >> 3] >> (i & 7)) & 1;
	}
}
