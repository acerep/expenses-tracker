package com.igel.expensesTracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ExpenseTracker extends Activity {

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

	private void setButtonListeners() {
		Button exit = (Button)findViewById(R.id.expenses_tracker_exit);
		exit.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				finish();
			}
		});
		Button viewCategoriesButton = (Button)findViewById(R.id.expenses_tracker_view_categories);
		viewCategoriesButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				viewExpenseCategories();
			}
		});
		Button viewExpensesButton = (Button)findViewById(R.id.expenses_tracker_view_expenses);
		viewExpensesButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				viewExpenses();
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
}
