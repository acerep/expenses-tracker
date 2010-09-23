package com.igel.expenses.tracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ExportExpensesUtils {

	private static final String LOG_TAG = "ExportExpenses";

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	public static void clearDirectory(File exportDirectory) {
		for (File file : exportDirectory.listFiles()) {
			file.delete();
		}
	}

	public static File getExportDirectory(Activity activity) {
		// get export directory URI from preferences
		SharedPreferences prefs = activity.getSharedPreferences(activity.getApplicationContext().getPackageName()
				+ "_preferences", Activity.MODE_PRIVATE);
		String directoryUriString = prefs.getString(activity.getString(R.string.expenses_tracker_preferences_folder),
				null);

		// initialize stuff
		File exportDirectory = null;
		directoryUriString = null;

		if (directoryUriString != null) {
			// if something found in preferences
			URI uri;
			try {
				uri = new URI(directoryUriString);
				exportDirectory = new File(uri);
			} catch (URISyntaxException e) {
				showError(activity, activity.getString(R.string.export_expenses_warning_cannot_find_directoy) + " "
						+ directoryUriString);
				Log.d(LOG_TAG, e.getMessage());
				return null;
			}
		} else {
			// default directory
			exportDirectory = getDefaultDirectory(activity.getApplicationContext().getPackageName());
		}
		return exportDirectory;
	}

	public static File initExportDirectory(Activity activity) {
		// check if external storage is writable
		boolean externalStorageWritable = ExportExpensesUtils.isExternalStorageWritable();
		File exportDirectory = getExportDirectory(activity);
		// show error if export is not possible
		if (!externalStorageWritable || !exportDirectory.canWrite()) {
			showError(activity, activity.getString(R.string.export_expenses_warning_cannot_write_to_export_directoy)
					+ " " + exportDirectory.getAbsolutePath());
			return null;
		}
		// add .nomedia file if it does not exist
		maintainNomediaFile(exportDirectory);

		return exportDirectory;
	}

	public static String getExportFileName() {
		Date currentDateTime = Calendar.getInstance().getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("'_'yyyyMMdd'_'HHmmss'.csv'");
		String postfix = simpleDateFormat.format(currentDateTime);
		return postfix;
	}

	public static void exportExpenseCategories(File exportDirectory, String postfix, ExpensesDbAdapter dbAdapter) {
		// create export file
		String fileName = "expenseCategories" + postfix;
		File categoriesFiles = new File(exportDirectory, fileName);

		// forward declarations
		BufferedWriter writer = null;
		Cursor cursor = null;

		try {
			cursor = dbAdapter.fetchAllExpenseCategoriesForExport();
			boolean hasData = cursor.moveToFirst();
			if (hasData) {
				// create the file and a writer
				categoriesFiles.createNewFile();
				writer = new BufferedWriter(new FileWriter(categoriesFiles));

				// write the header
				String formatString = "%s,\"%s\",\"%s\",%s";
				String header = String.format(formatString, ExpensesDbAdapter.EXPENSE_CATEGORY_ID,
						ExpensesDbAdapter.EXPENSE_CATEGORY_NAME, ExpensesDbAdapter.EXPENSE_CATEGORY_DESCRIPTION,
						ExpensesDbAdapter.EXPENSE_CATEGORY_DELETED);
				writer.write(header);
				writer.newLine();

				do {
					String categoryId = cursor.getString(cursor
							.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_CATEGORY_ID));
					String categoryName = cursor.getString(cursor
							.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_CATEGORY_NAME));
					String categoryDescription = cursor.getString(cursor
							.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_CATEGORY_DESCRIPTION));
					int deleted = cursor.getInt(cursor
							.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_CATEGORY_DELETED));
					String deletedString = deleted == ExpensesDbAdapter.TRUE ? "TRUE" : "FALSE";
					String line = String.format(formatString, categoryId, categoryName, categoryDescription,
							deletedString);
					writer.write(line);
					writer.newLine();
				} while (cursor.moveToNext());
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(LOG_TAG, e.getMessage());
		}
		// clean up
		closeWriter(writer);
		if (cursor != null)
			cursor.close();
	}

	public static void exportExpenses(File exportDirectory, String postfix, ExpensesDbAdapter dbAdapter, Calendar from,
			Calendar to) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy'-'MM");

		while (!from.after(to)) {
			String prefix = simpleDateFormat.format(from.getTime());
			long currentFromInMillis = from.getTimeInMillis();
			long currentToInMillis = CalendarUtils.getLastDayOfMonth(from).getTimeInMillis();
			exportExpensesInRange(exportDirectory, prefix + postfix, dbAdapter, currentFromInMillis, currentToInMillis);
			from.add(Calendar.MONTH, 1);
			from = CalendarUtils.getFirstDayOfMonth(from);
		}
	}

	private static void exportExpensesInRange(File exportDirectory, String fileName, ExpensesDbAdapter dbAdapter,
			long fromInMillis, long toInMillis) {
		// create export file
		File exportFile = new File(exportDirectory, fileName);

		// forward declarations
		BufferedWriter writer = null;
		Cursor cursor = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd");
		Calendar date = Calendar.getInstance();

		try {
			cursor = dbAdapter.fetchAllExpensesInRangeForExport(fromInMillis, toInMillis);
			boolean hasData = cursor.moveToFirst();
			if (hasData) {
				// create the file and a writer
				exportFile.createNewFile();
				writer = new BufferedWriter(new FileWriter(exportFile));

				// write the header
				String formatString = "%s,%s,%s,\"%s\",%s";
				String header = String.format(formatString, ExpensesDbAdapter.EXPENSE_ID, ExpensesDbAdapter.EXPENSE_DATE,
						ExpensesDbAdapter.EXPENSE_AMOUNT, ExpensesDbAdapter.EXPENSE_DETAILS,
						ExpensesDbAdapter.EXPENSE_EXPENSE_CATEGORY_ID);
				writer.write(header);
				writer.newLine();

				do {
					String expenseId = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_ID));
					long expenseDateMillis = cursor.getLong(cursor
							.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_DATE));
					date.setTimeInMillis(expenseDateMillis);
					String expenseDateString = simpleDateFormat.format(date.getTime());
					String expenseAmount = cursor.getString(cursor
							.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_AMOUNT));
					String expenseDetails = cursor.getString(cursor
							.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_DETAILS));
					String expenseCategoryId = cursor.getString(cursor
							.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_EXPENSE_CATEGORY_ID));
					String line = String.format(formatString, expenseId, expenseDateString, expenseAmount,
							expenseDetails, expenseCategoryId);
					writer.write(line);
					writer.newLine();
				} while (cursor.moveToNext());
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(LOG_TAG, e.getMessage());
		}
		// clean up
		closeWriter(writer);
		if (cursor != null)
			cursor.close();
	}

	private static void closeWriter(BufferedWriter writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				Log.d(LOG_TAG, e.getMessage());
			}
		}
	}

	private static File getDefaultDirectory(String packageName) {
		File externalStorageDirectory = Environment.getExternalStorageDirectory();
		String externalStoragePath = externalStorageDirectory.getAbsolutePath();
		externalStoragePath += "/Android/data/";
		externalStoragePath += packageName;
		externalStoragePath += "/files";
		File exportDirectory = new File(externalStoragePath);
		if (!exportDirectory.exists())
			exportDirectory.mkdirs();
		return exportDirectory;
	}

	private static void maintainNomediaFile(File exportDirectory) {
		// try to create a .nomedia file in the export directory; this prevents scanning of media trackers
		File nomedia = new File(exportDirectory, ".nomedia");
		if (!nomedia.exists())
			try {
				nomedia.createNewFile();
			} catch (IOException e) {
				;
			}
	}

	private static void showError(Activity activity, String errorMessage) {
		Toast toast = Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG);
		toast.show();
	}
}
