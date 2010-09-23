package com.igel.expenses.tracker;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ExpenseTracker extends Activity {

	// menu item id
	private static final int SHOW_PREFERENCES = Menu.FIRST;
	private static final int REMOVE_EXPORTED_FILES = Menu.FIRST + 1;

	// constants used to create dialogs
	private static final int REMOVE_EXPORTED_FILES_DIALOG = 0;

	// activity codes for creating intents
	private static final int ACTIVITY_SHOW_PREFERENCES = 0;

	private File mExportDirectory;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set view
		setContentView(R.layout.expense_tracker);

		// set button listeners
		setButtonListeners();
		setTitle(R.string.expenses_tracker_title);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// called when options menu is created
		super.onCreateOptionsMenu(menu);
		// add menu item to add expense category
		menu.add(0, SHOW_PREFERENCES, 0, R.string.expenses_tracker_preferences);
		menu.add(0, REMOVE_EXPORTED_FILES, 0, R.string.expenses_tracker_remove_files);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// called when menu item is selected
		switch (item.getItemId()) {
		case SHOW_PREFERENCES:
			Intent intent = new Intent(this, ExpensesTrackerPreferences.class);
			startActivityForResult(intent, ACTIVITY_SHOW_PREFERENCES);
			return true;
		case REMOVE_EXPORTED_FILES:
			if (mExportDirectory == null) {
				mExportDirectory = ExportExpensesUtils.getExportDirectory(this);
				if (mExportDirectory == null) {
					Toast toast = Toast.makeText(this, R.string.export_expenses_warning_cannot_find_directoy,
							Toast.LENGTH_LONG);
					toast.show();
					return false;
				}
			}
			showDialog(REMOVE_EXPORTED_FILES_DIALOG);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void setButtonListeners() {
		Button exit = (Button) findViewById(R.id.expenses_tracker_exit);
		exit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		Button viewCategoriesButton = (Button) findViewById(R.id.expenses_tracker_view_categories);
		viewCategoriesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				viewExpenseCategories();
			}
		});
		Button viewExpensesButton = (Button) findViewById(R.id.expenses_tracker_view_expenses);
		viewExpensesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				viewExpenses();
			}
		});
		Button addExpenseButton = (Button) findViewById(R.id.expenses_tracker_add_expense);
		addExpenseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addExpense();
			}
		});
		Button exportExpenses = (Button) findViewById(R.id.expenses_tracker_export_expenses);
		exportExpenses.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				exportExpenses();
			}
		});
	}

	private void viewExpenseCategories() {
		Intent intent = new Intent(this, ViewExpenseCategories.class);
		startActivity(intent);
	}

	private void viewExpenses() {
		Intent intent = new Intent(this, ViewExpenses.class);
		startActivity(intent);
	}

	private void addExpense() {
		Intent intent = new Intent(this, EditExpense.class);
		startActivity(intent);
	}

	private void exportExpenses() {
		Intent intent = new Intent(this, ExportExpenses.class);
		startActivity(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case REMOVE_EXPORTED_FILES_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			String message = String.format(getString(R.string.expenses_tracker_remove_files_message), mExportDirectory
					.getAbsolutePath());
			builder.setMessage(message).setCancelable(false).setPositiveButton(R.string.expenses_tracker_yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ExportExpensesUtils.clearDirectory(mExportDirectory);
						}
					}).setNegativeButton(R.string.expenses_tracker_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			return builder.create();
		}
		return null;
	}

}
