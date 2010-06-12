package com.igel.expensesTracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ExpensesDbAdapter {

	public static final String DATABASE_NAME = "expensesTracker";
	public static final int DATABASE_VERSION = 1;

	private static final String EXPENSE_CATEGORY_TABLE = "expenseCategory";
	public static final String EXPENSE_CATEGORY_ID = "_id";
	public static final String EXPENSE_CATEGORY_NAME = "name";
	public static final String EXPENSE_CATEGORY_DESCRIPTION = "description";

	private static final String TAG = "ExpenseDbAdapter";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private Context mCtx;

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
	public boolean updateNote(long rowId, String name, String description) {
		ContentValues args = new ContentValues();
		args.put(EXPENSE_CATEGORY_NAME, name);
		args.put(EXPENSE_CATEGORY_DESCRIPTION, description);

		return mDb.update(EXPENSE_CATEGORY_TABLE, args, EXPENSE_CATEGORY_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor positioned at the expense category that matches the given rowId
	 * 
	 * @param rowId id of category to retrieve
	 * @return Cursor positioned to matching category, if found
	 * @throws SQLException if note could not be found/retrieved
	 */
	public Cursor fetchExpenseCategory(long rowId) throws SQLException {

		Cursor mCursor = mDb.query(false, EXPENSE_CATEGORY_TABLE, new String[] { EXPENSE_CATEGORY_ID,
				EXPENSE_CATEGORY_NAME, EXPENSE_CATEGORY_DESCRIPTION }, EXPENSE_CATEGORY_ID + "=" + rowId, null, null, null,
				null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return a Cursor over the list of all expense categories in the database
	 * 
	 * @return Cursor over all categories
	 */
	public Cursor fetchAllExpenseCategories() {

		return mDb.query(EXPENSE_CATEGORY_TABLE, new String[] { EXPENSE_CATEGORY_ID, EXPENSE_CATEGORY_NAME,
				EXPENSE_CATEGORY_DESCRIPTION }, null, null, null, null, null);
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating expenses-tracker database");
			db.execSQL("create table " + EXPENSE_CATEGORY_TABLE + "(" + EXPENSE_CATEGORY_ID
					+ " integer primary key autoincrement, " + EXPENSE_CATEGORY_NAME + " text not null, "
					+ EXPENSE_CATEGORY_DESCRIPTION + " text not null);");
			db.execSQL("insert into " + EXPENSE_CATEGORY_TABLE + " (" + EXPENSE_CATEGORY_NAME + ", "
					+ EXPENSE_CATEGORY_DESCRIPTION
					+ ") values ('Unknown', 'Default expense category for expenses of unknown type');");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + EXPENSE_CATEGORY_TABLE);
			onCreate(db);
		}
	}
}
