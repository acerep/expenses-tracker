package com.igel.expenses.tracker;

import android.os.Environment;

public class ExportExpensesUtils {

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();

		return Environment.MEDIA_MOUNTED.equals(state);
	}
}
