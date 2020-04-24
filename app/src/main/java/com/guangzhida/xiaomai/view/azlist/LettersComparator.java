package com.guangzhida.xiaomai.view.azlist;



import com.guangzhida.xiaomai.room.entity.UserEntity;
import com.guangzhida.xiaomai.ui.user.UserActivity;

import java.util.Comparator;

public class LettersComparator implements Comparator<AZItemEntity<UserEntity>> {

	public int compare(AZItemEntity<UserEntity> o1, AZItemEntity<UserEntity> o2) {
		if (o1.getSortLetters().equals("@")
			|| o2.getSortLetters().equals("#")) {
			return 1;
		} else if (o1.getSortLetters().equals("#")
				   || o2.getSortLetters().equals("@")) {
			return -1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
