package com.igel.expensesTracker;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ViewExpenseCategories extends ListActivity {

	// menu item id
    private static final int ADD_EXPENSE_CATEGORY_ID = Menu.FIRST;

    // activity codes for creating intents
    private static final int ACTIVITY_ADD_EXPENSE_CATEGORY = 0;
    private static final int ACTIVITY_EDIT_EXPENSE_CATEGORY = 1;

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
        setContentView(R.layout.view_expense_categories);
        setTitle("honk");
        
        // update view
        fetchDataFromDb();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// called when options menu is created
        super.onCreateOptionsMenu(menu);
        // add menu item to add expense category
        menu.add(0, ADD_EXPENSE_CATEGORY_ID, 0, R.string.view_expense_categories_add_expense_category);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// called when menu item is selected
        switch(item.getItemId()) {
            case ADD_EXPENSE_CATEGORY_ID:
                addExpenseCategory();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void addExpenseCategory() {
    	Intent intent = new Intent(this, EditExpenseCategory.class);
    	startActivityForResult(intent, ACTIVITY_ADD_EXPENSE_CATEGORY);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	// called when list item is clicked
        super.onListItemClick(l, v, position, id);
        
        // create new intent
        Intent intent = new Intent(this, EditExpenseCategory.class);
        
        // pass ID of expense category 
        intent.putExtra(EditExpenseCategory.KEY_EXPENSE_CATEGORY_ID, id);
        
        // start activity
        startActivityForResult(intent, ACTIVITY_EDIT_EXPENSE_CATEGORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	// called when started activity is finished
        super.onActivityResult(requestCode, resultCode, intent);
        fetchDataFromDb();
    }
    
    private void fetchDataFromDb() {
        // Get all of expense categories from the database and create the item list
        Cursor expenseCategoryCursor = mDbAdapter.fetchAllExpenseCategories();
        startManagingCursor(expenseCategoryCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{ExpensesDbAdapter.EXPENSE_CATEGORY_NAME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.view_expense_categories_row_name};

        // Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.view_expense_categories_row,
				expenseCategoryCursor, from, to);
        setListAdapter(notes);
    }   
}