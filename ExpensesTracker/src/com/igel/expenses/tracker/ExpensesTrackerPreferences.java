package com.igel.expenses.tracker;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class ExpensesTrackerPreferences extends android.preference.PreferenceActivity {

	private PreferenceScreen mFolderPreference;

	private static final int ACTION_CHOOSE_FOLDER = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		mFolderPreference = (PreferenceScreen) getPreferenceScreen().findPreference(
				getString(R.string.expenses_tracker_preferences_folder));
		mFolderPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				folderPreferenceClicked(preference);
				return true;
			}
		});
	}

	private void folderPreferenceClicked(Preference preference) {
		Intent oiFileManagerIntent = new Intent(getString(R.string.open_intents_file_picker_intent));
		List<ResolveInfo> queryIntentActivities = getPackageManager().queryIntentActivities(oiFileManagerIntent,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (queryIntentActivities.isEmpty()) {
			Toast toast = Toast.makeText(this,
					R.string.expenses_tracker_preferences_warning_cannot_find_io_file_manager, Toast.LENGTH_LONG);
			toast.show();
		} else {
			startActivityForResult(oiFileManagerIntent, ACTION_CHOOSE_FOLDER);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// called when started activity is finished
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == ACTION_CHOOSE_FOLDER) {
			if (resultCode == RESULT_CANCELED)
				return;
			if (resultCode == RESULT_OK) {
				Uri data = intent.getData();
				if (data != null) {
					Editor editor = mFolderPreference.getEditor();
					editor.putString(getString(R.string.expenses_tracker_preferences_folder), data.toString());
					editor.commit();
				}
			}
		}
	}

}
