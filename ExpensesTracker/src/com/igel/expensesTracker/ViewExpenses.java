package com.igel.expensesTracker;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.igel.expensesTracker.R.string;

public class ViewExpenses extends ListActivity {

	// menu item id
    private static final int ADD_EXPENSE_ID = Menu.FIRST;
    private static final int DELETE_EXPENSE_ID = Menu.FIRST + 1;

    // activity codes for creating intents
    private static final int ACTIVITY_ADD_EXPENSE = 0;
    private static final int ACTIVITY_EDIT_EXPENSE = 1;

	// constants used to create dialogs
	private static final int DELETE_EXPENSE_DIALOG = 0;
	
	// used to pass the ID of the expense to the delete dialog (bad but bundle not available)
	private long mExpenseId;
	
	// database adapter
	private ExpensesDbAdapter mDbAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // create new database adapter and open it
        mDbAdapter = new ExpensesDbAdapter(this);
        mDbAdapter.open();
        
        // set view
        setContentView(R.layout.view_expenses);
        setTitle("honk");
        
        // update view
        fetchDataFromDb();
        
        registerForContextMenu(getListView());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// called when options menu is created
        super.onCreateOptionsMenu(menu);
        // add menu item to add expense category
        menu.add(0, ADD_EXPENSE_ID, 0, R.string.view_expenses_add_expense);
        return true;
    }
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_EXPENSE_ID, 0, R.string.view_expenses_delete_expense);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case DELETE_EXPENSE_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    		mExpenseId = info.id;
    		showDialog(DELETE_EXPENSE_DIALOG);
    		return true;
    	}
    	return super.onContextItemSelected(item);
	}

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// called when menu item is selected
        switch(item.getItemId()) {
            case ADD_EXPENSE_ID:
                addExpense();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void addExpense() {
    	Intent intent = new Intent(this, EditExpense.class);
    	startActivityForResult(intent, ACTIVITY_ADD_EXPENSE);
    }
    
    private void deleteSelectedExpense() {
		mDbAdapter.deleteExpense(mExpenseId);
		fetchDataFromDb();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	// called when list item is clicked
        super.onListItemClick(l, v, position, id);

        // create new intent
        Intent intent = new Intent(this, EditExpense.class);
        
        // pass ID of expense category 
        intent.putExtra(EditExpense.KEY_EXPENSE_ID, id);
        
        // start activity
        startActivityForResult(intent, ACTIVITY_EDIT_EXPENSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	// called when started activity is finished
        super.onActivityResult(requestCode, resultCode, intent);
        fetchDataFromDb();
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DELETE_EXPENSE_DIALOG:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(string.view_expenses_delete_expense_message)
	    	       .setCancelable(false)
	    	       .setPositiveButton(string.expenses_tracker_yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							deleteSelectedExpense();
						}
	    	       })
	    	       .setNegativeButton(string.expenses_tracker_no, new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                dialog.cancel();
	    	           }
	    	       });
			return builder.create();
		}
		return null;
	}
	
    private void fetchDataFromDb() {
        // Get all of expenses from the database and create the item list
        Cursor expensesCursor = mDbAdapter.fetchAllExpenses();
        startManagingCursor(expensesCursor);

        // Create an array to specify the fields we want to display in the list
		String[] from = new String[] { ExpensesDbAdapter.EXPENSE_DATE, ExpensesDbAdapter.EXPENSE_AMOUNT,
				ExpensesDbAdapter.EXPENSE_DETAILS, ExpensesDbAdapter.EXPENSE_CATEGORY_NAME };

        // and an array of the fields we want to bind those fields to
		int[] to = new int[] { R.id.view_expense_row_date, R.id.view_expense_row_amount, R.id.view_expense_row_details,
				R.id.view_expense_row_category };

        // Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter expenses = new ExpensesCursorAdapter(this, R.layout.view_expenses_row,
				expensesCursor, from, to);
        setListAdapter(expenses);
    }   

    private class ExpensesCursorAdapter extends SimpleCursorAdapter {

    	private Activity mContext;
    	// used to format date for display
    	private DateFormat mDateFormat;
    	private Calendar mCalendar;
    	
		public ExpensesCursorAdapter(Activity context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
			mContext = context;
			mDateFormat = android.text.format.DateFormat.getDateFormat(mContext);
			mCalendar = Calendar.getInstance();
		}
		
		@Override
	    public void setViewText(TextView v, String text) {
			if (v.getId() == R.id.view_expense_row_date) {
				long millis = new Long(text).longValue();
				mCalendar.setTimeInMillis(millis);
				String dateString = mDateFormat.format(mCalendar.getTime());
				v.setText(dateString);
			}
			else
				super.setViewText(v, text);
	    }
    }
}
