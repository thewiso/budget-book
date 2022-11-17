package de.schoenebaum.budgetbook.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CollectionUtils {

	/**
	 * @return Returns index of first element in col1 that is also contained in
	 *         col2. If not item matched, returns -1
	 */
	public static <T> int getFirstMatchingIndex(Collection<T> coll1, Collection<T> coll2) {
		Objects.requireNonNull(coll1);
		Objects.requireNonNull(coll2);

		int index = 0;
		for (T item : coll1) {
			if (coll2.contains(item)) {
				return index;
			}
			index++;
		}

		return -1;
	}

	/**
	 * @return Returns all indices of elements in col1 that are also contained in
	 *         col2. If not item matched, returns empty list
	 */
	public static <T> List<Integer> getAllMatchingIndices(Collection<T> coll1, Collection<T> coll2) {
		Objects.requireNonNull(coll1);
		Objects.requireNonNull(coll2);

		List<Integer> matchingIndices = new LinkedList<>();

		int index = 0;
		for (T item : coll1) {
			if (coll2.contains(item)) {
				matchingIndices.add(index);
			}
			index++;
		}

		return matchingIndices;
	}

}
