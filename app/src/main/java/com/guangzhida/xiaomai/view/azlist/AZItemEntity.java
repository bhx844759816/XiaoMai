package com.guangzhida.xiaomai.view.azlist;


public class AZItemEntity<T> {

	private T      mValue;
	private String mSortLetters;

	public T getValue() {
		return mValue;
	}

	public void setValue(T value) {
		mValue = value;
	}

	public String getSortLetters() {
		return mSortLetters;
	}

	public void setSortLetters(String sortLetters) {
		mSortLetters = sortLetters;
	}


	@Override
	public String toString() {
		return "AZItemEntity{" +
				"mValue=" + mValue.toString() +
				", mSortLetters='" + mSortLetters + '\'' +
				'}';
	}
}
