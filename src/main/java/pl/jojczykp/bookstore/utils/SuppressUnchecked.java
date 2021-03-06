package pl.jojczykp.bookstore.utils;

import java.util.List;

public final class SuppressUnchecked {

	private SuppressUnchecked() {
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> suppressUnchecked(List list) {
		return (List<T>) list;
	}

	@SuppressWarnings("unchecked")
	public static List<String> suppressUnchecked(Object object) {
		return (List<String>) object;
	}

}
