package de.schoenebaum.budgetbook.utils;

public class StringUtils {

	public static boolean atMostMatches(final CharSequence str, final char ch, int matchCount) {
		if (org.apache.commons.lang3.StringUtils.isEmpty(str)) {
			return matchCount == 0;
		}
		int count = 0;
		// We could also call str.toCharArray() for faster look ups but that would
		// generate more garbage.
		for (int i = 0; i < str.length(); i++) {
			if (ch == str.charAt(i) && ++count > matchCount) {
				return false;
			}
		}
		return true;
	}

	public static String join(char separator, String... s) {
		return org.apache.commons.lang3.StringUtils.join(s, separator);
	}

	public static String joinSpace(String s1, String s2) {
		return join(' ', s1, s2);
	}
}
