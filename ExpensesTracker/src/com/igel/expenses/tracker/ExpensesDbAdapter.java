package com.igel.expenses.tracker;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class ExpensesDbAdapter {

	public static final String DATABASE_NAME = "expensesTracker";
	public static final int DATABASE_VERSION = 1;

	public static final int TRUE = 0;
	public static final int FALSE = 1;

	private static final String EXPENSE_CATEGORY_TABLE = "expenseCategory";
	public static final String EXPENSE_CATEGORY_ID = "_id";
	public static final String EXPENSE_CATEGORY_NAME = "name";
	public static final String EXPENSE_CATEGORY_DESCRIPTION = "description";
	public static final String EXPENSE_CATEGORY_DELETED = "deleted";

	public static final int UNKNOWN_EXPENSE_CATEGORY_ID = 1;

	private static final String EXPENSE_TABLE = "expense";
	public static final String EXPENSE_ID = "_id";
	public static final String EXPENSE_DATE = "expenseDate";
	public static final String EXPENSE_AMOUNT = "amount";
	public static final String EXPENSE_DETAILS = "details";
	public static final String EXPENSE_EXPENSE_CATEGORY_ID = "expenseCategoryId";

	private static final String TAG = "ExpenseDbAdapter";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private Context mCtx;

	private static HashMap<String, String> sExpensesProjectionMap;

	/**
	 * Constructor - takes the context to allow the database to be opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public ExpensesDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the expense-tracker database. If it cannot be opened, try to create a new instance of the database. If it
	 * cannot be created, throw an exception to signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public ExpensesDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		sExpensesProjectionMap = new HashMap<String, String>();
		sExpensesProjectionMap.put(EXPENSE_TABLE + "." + EXPENSE_ID, EXPENSE_TABLE + "." + EXPENSE_ID + " AS "
				+ EXPENSE_ID);
		sExpensesProjectionMap.put(EXPENSE_DATE, EXPENSE_DATE);
		sExpensesProjectionMap.put(EXPENSE_AMOUNT, EXPENSE_AMOUNT);
		sExpensesProjectionMap.put(EXPENSE_DETAILS, EXPENSE_DETAILS);
		sExpensesProjectionMap.put(EXPENSE_CATEGORY_NAME, EXPENSE_CATEGORY_NAME);
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new expense category using provided information. If the category is successfully created return the new
	 * rowId for that category, otherwise return a -1 to indicate failure.
	 * 
	 * @param name the name of the category
	 * @param description the description of the category
	 * @return rowId or -1 if failed
	 */
	public long createExpenseCategory(String name, String description) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(EXPENSE_CATEGORY_NAME, name);
		initialValues.put(EXPENSE_CATEGORY_DESCRIPTION, description);
		initialValues.put(EXPENSE_CATEGORY_DELETED, FALSE);

		return mDb.insert(EXPENSE_CATEGORY_TABLE, null, initialValues);
	}

	/**
	 * Update the expense category using the details provided. The category to be updated is specified using the rowId.
	 * 
	 * @param rowId id of category to update
	 * @param name name to set category name to
	 * @param description description to set category description to
	 * @return true if the category was successfully updated, false otherwise
	 */
	public boolean updateExpenseCategory(long rowId, String name, String description) {
		ContentValues args = new ContentValues();
		args.put(EXPENSE_CATEGORY_NAME, name);
		args.put(EXPENSE_CATEGORY_DESCRIPTION, description);

		return mDb.update(EXPENSE_CATEGORY_TABLE, args, EXPENSE_CATEGORY_ID + "=" + rowId, null) > 0;
	}

	public boolean deleteExpenseCategory(long rowId) {
		ContentValues args = new ContentValues();
		args.put(EXPENSE_CATEGORY_DELETED, TRUE);
		return mDb.update(EXPENSE_CATEGORY_TABLE, args, EXPENSE_CATEGORY_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor positioned at the expense category that matches the given rowId
	 * 
	 * @param rowId id of category to retrieve
	 * @return Cursor positioned to matching category, if found
	 * @throws SQLException if expense category could not be found/retrieved
	 */
	public Cursor fetchExpenseCategory(long rowId) throws SQLException {

		Cursor cursor = mDb.query(false, EXPENSE_CATEGORY_TABLE, new String[] { EXPENSE_CATEGORY_ID,
				EXPENSE_CATEGORY_NAME, EXPENSE_CATEGORY_DESCRIPTION }, EXPENSE_CATEGORY_ID + "=" + rowId, null, null,
				null, null, null);

		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * Return a Cursor over the list of all expense categories in the database
	 * 
	 * @return Cursor over all categories
	 */
	public Cursor fetchAllExpenseCategories() {
		return mDb.query(EXPENSE_CATEGORY_TABLE, new String[] { EXPENSE_CATEGORY_ID, EXPENSE_CATEGORY_NAME,
				EXPENSE_CATEGORY_DESCRIPTION }, EXPENSE_CATEGORY_DELETED + "=" + FALSE, null, null, null, null);
	}

	/**
	 * Return a Cursor over the list of all expense categories in the database for export
	 * 
	 * @return Cursor over all categories
	 */
	public Cursor fetchAllExpenseCategoriesForExport() {
		return mDb.query(EXPENSE_CATEGORY_TABLE, new String[] { EXPENSE_CATEGORY_ID, EXPENSE_CATEGORY_NAME,
				EXPENSE_CATEGORY_DESCRIPTION, EXPENSE_CATEGORY_DELETED }, null, null, null, null, null);
	}

	/**
	 * Create a new expense using provided information. If the expense is successfully created return the new rowId for
	 * that expense, otherwise return a -1 to indicate failure.
	 * 
	 * @param date the date of the expense in milliseconds
	 * @param amount the expense amount
	 * @param expenseCategoryId id of the expense category of the expense
	 * @param details the details of the expense
	 * @return rowId or -1 if failed
	 */
	public long createExpense(long dateInMillis, int amount, long expenseCategoryId, String details) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(EXPENSE_DATE, dateInMillis);
		initialValues.put(EXPENSE_AMOUNT, amount);
		initialValues.put(EXPENSE_EXPENSE_CATEGORY_ID, expenseCategoryId);
		initialValues.put(EXPENSE_DETAILS, details);

		return mDb.insert(EXPENSE_TABLE, null, initialValues);
	}

	/**
	 * Update the expense using the details provided. The expense to be updated is specified using the rowId.
	 * 
	 * @param rowId id of expense to update
	 * @param date the date of the expense in milliseconds
	 * @param amount the expense amount
	 * @param expenseCategoryId id of the expense category of the expense
	 * @param details the details of the expense
	 * @return true if the category was successfully updated, false otherwise
	 */
	public boolean updateExpense(long rowId, long dateInMillis, int amount, long expenseCategoryId, String details) {
		ContentValues args = new ContentValues();
		args.put(EXPENSE_DATE, dateInMillis);
		args.put(EXPENSE_AMOUNT, amount);
		args.put(EXPENSE_EXPENSE_CATEGORY_ID, expenseCategoryId);
		args.put(EXPENSE_DETAILS, details);

		return mDb.update(EXPENSE_TABLE, args, EXPENSE_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor positioned at the expense that matches the given rowId
	 * 
	 * @param rowId id of expense to retrieve
	 * @return Cursor positioned to matching expense, if found
	 * @throws SQLException if expense could not be found/retrieved
	 */
	public Cursor fetchExpense(long rowId) throws SQLException {
		Cursor cursor = mDb.query(false, EXPENSE_TABLE, new String[] { EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT,
				EXPENSE_DETAILS, EXPENSE_EXPENSE_CATEGORY_ID }, EXPENSE_ID + "=" + rowId, null, null, null, null, null);

		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * Delete the expense with the given rowId
	 * 
	 * @param rowId id of expense to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteExpense(long rowId) {
		return mDb.delete(EXPENSE_TABLE, EXPENSE_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Delete expenses prior to the given time in millis.
	 * 
	 * @param priorTo Time prior which expenses are deleted.
	 * @return true if something was deleted, false otherwise
	 */
	public boolean deleteExpensePriorTo(long priorTo) {
		return mDb.delete(EXPENSE_TABLE, EXPENSE_DATE + "<" + priorTo, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all expenses in the database joined with the referenced expense category name in
	 * the given range.
	 * 
	 * @param from From time in millis
	 * @param to To time in millis
	 * @return Cursor over all expenses
	 */
	public Cursor fetchAllExpensesInRange(long from, long to) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EXPENSE_TABLE + " left outer join " + EXPENSE_CATEGORY_TABLE + " on (" + EXPENSE_TABLE + "."
				+ EXPENSE_EXPENSE_CATEGORY_ID + " = " + EXPENSE_CATEGORY_TABLE + "." + EXPENSE_CATEGORY_ID + ")");
		qb.setProjectionMap(sExpensesProjectionMap);
		return qb.query(mDb, new String[] { EXPENSE_TABLE + "." + EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT,
				EXPENSE_DETAILS, EXPENSE_CATEGORY_NAME }, EXPENSE_DATE + ">=" + from + " and " + EXPENSE_DATE + "<"
				+ to, null, null, null, EXPENSE_TABLE + "." + EXPENSE_DATE + " desc");
	}

	/**
	 * Return a Cursor over the list of all expenses in the given range for export
	 * 
	 * @return Cursor over all expenses
	 */
	public Cursor fetchAllExpensesInRangeForExport(long from, long to) {
		return mDb.query(EXPENSE_TABLE, new String[] { EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, EXPENSE_DETAILS,
				EXPENSE_EXPENSE_CATEGORY_ID }, EXPENSE_DATE + ">=" + from + " and " + EXPENSE_DATE + "<" + to, null,
				null, null, null);
	}

	/**
	 * Returns the sum of all expenses in the given range.
	 * 
	 * @param from From time in millis
	 * @param to To time in millis
	 * @return The sum of all expenses
	 */
	public long getExpensesSum(long from, long to) {
		Cursor cursor = mDb.query(false, EXPENSE_TABLE, new String[] { "sum(" + EXPENSE_AMOUNT + ")"}, EXPENSE_DATE + " >= ? and " + EXPENSE_DATE + " < ?"
				, new String[] {"" + from, "" + to}, null, null, null, null);

		long result = 0;
		if (cursor != null && cursor.moveToFirst()) {
			String amountString = cursor.getString(cursor.getColumnIndexOrThrow("sum(" + EXPENSE_AMOUNT + ")"));
			if (amountString != null)
				result = Long.valueOf(amountString);
		}
		if (cursor != null)
			cursor.close();
		return result;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		private Context mContext;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating expenses-tracker database");
			db.execSQL("create table " + EXPENSE_CATEGORY_TABLE + "(" + EXPENSE_CATEGORY_ID
					+ " integer primary key autoincrement, " + EXPENSE_CATEGORY_NAME + " text not null, "
					+ EXPENSE_CATEGORY_DESCRIPTION + " text not null, " + EXPENSE_CATEGORY_DELETED
					+ " integer not null);");
			db.execSQL("create table " + EXPENSE_TABLE + "(" + EXPENSE_ID + " integer primary key autoincrement, "
					+ EXPENSE_DATE + " integer not null, " + EXPENSE_AMOUNT + " integer not null, "
					+ EXPENSE_EXPENSE_CATEGORY_ID + " integer not null, " + EXPENSE_DETAILS
					+ " text not null, constraint exp2expCat foreign key (" + EXPENSE_EXPENSE_CATEGORY_ID
					+ ") references " + EXPENSE_CATEGORY_TABLE + " (" + EXPENSE_CATEGORY_ID + "));");
			db.execSQL("insert into " + EXPENSE_CATEGORY_TABLE + " (" + EXPENSE_CATEGORY_ID + ", "
					+ EXPENSE_CATEGORY_NAME + ", " + EXPENSE_CATEGORY_DESCRIPTION + ", " + EXPENSE_CATEGORY_DELETED
					+ ") values ( " + UNKNOWN_EXPENSE_CATEGORY_ID + ", '"
					+ mContext.getString(R.string.unknown_expense_category_name) + "', '"
					+ mContext.getString(R.string.unknown_expense_category_description) + "', " + FALSE + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + EXPENSE_CATEGORY_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + EXPENSE_TABLE);
			onCreate(db);
		}
	}
}
