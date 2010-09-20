package com.igel.expenses.tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;

public class ExportExpenses extends Activity {

	// keys used to store information in the activity state or pass information by an intent
	public static final String KEY_FROM_DATE = "keyFromDate";
	public static final String KEY_TO_DATE = "keyToDate";

	// constants used to create dialogs
	private static final int FROM_DATE_DIALOG = 0;
	private static final int TO_DATE_DIALOG = 1;

	// widgets
	private Button mFromDateButton;
	private Button mToDateButton;

	// other expense information
	private Calendar mFromDate;
	private Calendar mToDate;

	// used to format date for display
	private DateFormat mDateFormat;

	// database adapter
	private ExpensesDbAdapter mDbAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set view
		setContentView(R.layout.export_expenses);
		mDateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");

		// extras may be given when the activity is called from someone else
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			// initialize dates to current date
			mFromDate = Calendar.getInstance();
	        mFromDate.set(Calendar.DAY_OF_MONTH, 1);
			mToDate = (Calendar)mFromDate.clone();
			mToDate.add(Calendar.MONTH, 1);
			mToDate.add(Calendar.DAY_OF_MONTH, -1);
		}

		initializeWidgets();
		setButtonListeners();
		setTitle(R.string.export_expenses_title);
		updateView();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// this method is called by the system before onPause
		outState.putSerializable(KEY_FROM_DATE, mFromDate.getTimeInMillis());
		outState.putSerializable(KEY_TO_DATE, mToDate.getTimeInMillis());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// this method is called when the system restores the activity state (after onStart)
		super.onRestoreInstanceState(savedInstanceState);
		Long fromDateInMillis = (Long) savedInstanceState.getSerializable(KEY_FROM_DATE);
		mFromDate = Calendar.getInstance();
		mFromDate.setTimeInMillis(fromDateInMillis);
		Long toDateInMillis = (Long) savedInstanceState.getSerializable(KEY_TO_DATE);
		mToDate = Calendar.getInstance();
		mToDate.setTimeInMillis(toDateInMillis);
	}		
	
	private void initializeWidgets() {
		mFromDateButton = (Button) findViewById(R.id.export_expenses_from_date);
		mToDateButton = (Button) findViewById(R.id.export_expenses_to_date);
	}
	
	private void setButtonListeners() {
		mFromDateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(FROM_DATE_DIALOG);
			}
		});
		mToDateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(TO_DATE_DIALOG);
			}
		});

		Button cancelButton = (Button)findViewById(R.id.export_expenses_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		Button saveButton = (Button)findViewById(R.id.export_expenses_export);
		saveButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				setResult(RESULT_OK);
//				saveExpense();
				finish();
			}
		});
}
	
	private void updateView() {
		if (mFromDate != null)
			mFromDateButton.setText(mDateFormat.format(mFromDate.getTime()));
		if (mToDate != null)
			mToDateButton.setText(mDateFormat.format(mToDate.getTime()));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case FROM_DATE_DIALOG:
			return new DatePickerDialog(this, mFromDateSetListener, mFromDate.get(Calendar.YEAR), mFromDate
					.get(Calendar.MONTH), mFromDate.get(Calendar.DAY_OF_MONTH));
		case TO_DATE_DIALOG:
			return new DatePickerDialog(this, mToDateSetListener, mToDate.get(Calendar.YEAR), mToDate
					.get(Calendar.MONTH), mToDate.get(Calendar.DAY_OF_MONTH));
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener mFromDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mFromDate.set(Calendar.YEAR, year);
			mFromDate.set(Calendar.MONTH, monthOfYear);
			mFromDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateView();
		}
	};

	private DatePickerDialog.OnDateSetListener mToDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mToDate.set(Calendar.YEAR, year);
			mToDate.set(Calendar.MONTH, monthOfYear);
			mToDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateView();
		}
	};

}
