package com.igel.expensesTracker;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditExpenseCategory extends Activity {
	
	// keys used to store information in the activity state or pass information by an intent
	public static final String KEY_EXPENSE_CATEGORY_ID = "keyExpenseCategoryId";
	public static final String KEY_EXPENSE_CATEGORY_NAME = "keyExpenseCategoryName";
	public static final String KEY_EXPENSE_CATEGORY_DESCRIPTION = "keyExpenseCategoryDescription";
	
	// the ID of the expense category to be edited
	private Long mExpenseCategoryId;
	
	// important widgets
	private EditText mExpenseCategoryNameWidget;
	private EditText mExpenseCategoryDescriptionWidget;
	
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
        setContentView(R.layout.edit_expense_category);
        
        // get views
		mExpenseCategoryNameWidget = (EditText)findViewById(R.id.edit_expense_category_name);
		mExpenseCategoryDescriptionWidget = (EditText)findViewById(R.id.edit_expense_category_description);

		// extras may be given when the activity is called from someone else
        Bundle extras = getIntent().getExtras();
        if (extras != null) {		
        	// set expense category ID (if given)
        	mExpenseCategoryId = (Long)extras.getSerializable(KEY_EXPENSE_CATEGORY_ID);
        	if (mExpenseCategoryId != null) {
        		// initialize view
        		fetchDataFromDb();        		
        	}
        }

		// set button listeners
		setButtonListeners();		
    }

	@Override
    protected void onSaveInstanceState(Bundle outState) {		
    	super.onSaveInstanceState(outState);
    	// this method is called by the system before onPause
    	outState.putSerializable(KEY_EXPENSE_CATEGORY_ID, mExpenseCategoryId);
    	String expenseCategoryName = mExpenseCategoryNameWidget.getText().toString();
    	outState.putString(KEY_EXPENSE_CATEGORY_NAME, expenseCategoryName);
    	String expenseCategoryDescription = mExpenseCategoryDescriptionWidget.getText().toString();
    	outState.putString(KEY_EXPENSE_CATEGORY_DESCRIPTION, expenseCategoryDescription);
    }
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// this method is called when the system restores the activity state (after onStart)
		super.onRestoreInstanceState(savedInstanceState);
		mExpenseCategoryId = (Long)savedInstanceState.getSerializable(KEY_EXPENSE_CATEGORY_ID);
    	String expenseCategoryName = savedInstanceState.getString(KEY_EXPENSE_CATEGORY_NAME);
    	mExpenseCategoryNameWidget.setText(expenseCategoryName);
    	String expenseCategoryDescription= savedInstanceState.getString(KEY_EXPENSE_CATEGORY_DESCRIPTION);
    	mExpenseCategoryDescriptionWidget.setText(expenseCategoryDescription);		
	}

    private void saveExpenseCategory() {
    	// get entered information
    	String expenseCategoryName = mExpenseCategoryNameWidget.getText().toString();
    	String expenseCategoryDescription = mExpenseCategoryDescriptionWidget.getText().toString();

    	if (mExpenseCategoryId == null) {
    		// create new expense category
    		long id = mDbAdapter.createExpenseCategory(expenseCategoryName, expenseCategoryDescription);
    		if (id > 0)
    			mExpenseCategoryId = id;
    	}
    	else {
    		mDbAdapter.updateNote(mExpenseCategoryId, expenseCategoryName, expenseCategoryDescription);
    	}
    }
    
	private void setButtonListeners() {
		Button cancelButton = (Button)findViewById(R.id.edit_expense_category_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		Button okButton = (Button)findViewById(R.id.edit_expense_category_ok);
		okButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				setResult(RESULT_OK);
				saveExpenseCategory();
				finish();
			}
		});
	}

   private void fetchDataFromDb() {
		Cursor expenseCategory = mDbAdapter.fetchExpenseCategory(mExpenseCategoryId);
		startManagingCursor(expenseCategory);
		mExpenseCategoryNameWidget.setText(expenseCategory.getString(expenseCategory
				.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_CATEGORY_NAME)));
		mExpenseCategoryDescriptionWidget.setText(expenseCategory.getString(expenseCategory
				.getColumnIndexOrThrow(ExpensesDbAdapter.EXPENSE_CATEGORY_DESCRIPTION)));
    }
}