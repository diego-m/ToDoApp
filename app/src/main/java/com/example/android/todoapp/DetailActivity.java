package com.example.android.todoapp;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.android.todoapp.data.TodoContentProvider;
import com.example.android.todoapp.data.TodoTable;

/*
 * DetailActivity for create or edit an existing task
 */
public class DetailActivity extends Activity {
    private Spinner mTypes;
    private EditText mSummaryText;
    private EditText mDescriptionText;

    private Uri todoUri;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mTypes = (Spinner) findViewById(R.id.types);
        mSummaryText = (EditText) findViewById(R.id.summary_edit);
        mDescriptionText = (EditText) findViewById(R.id.description_edit);
        Button confirmButton = (Button) findViewById(R.id.confirm_button);

        Bundle extras = getIntent().getExtras();

        todoUri = (bundle == null) ? null : (Uri) bundle
                .getParcelable(TodoContentProvider.CONTENT_ITEM_TYPE);

        if (extras != null) {
            todoUri = extras
                    .getParcelable(TodoContentProvider.CONTENT_ITEM_TYPE);

            loadData(todoUri);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TextUtils.isEmpty(mSummaryText.getText().toString())) {
                    Toast.makeText(DetailActivity.this, getString(R.string.need_summary),
                            Toast.LENGTH_LONG).show();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String type = (String) mTypes.getSelectedItem();
        String summary = mSummaryText.getText().toString();
        String description = mDescriptionText.getText().toString();

        if (TextUtils.isEmpty(description) && TextUtils.isEmpty(summary)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(TodoTable.COLUMN_CATEGORY, type);
        values.put(TodoTable.COLUMN_SUMMARY, summary);
        values.put(TodoTable.COLUMN_DESCRIPTION, description);

        if (todoUri == null) {
            todoUri = getContentResolver().insert(TodoContentProvider.CONTENT_URI, values);
        } else {
            getContentResolver().update(todoUri, values, null, null);
        }
        outState.putParcelable(TodoContentProvider.CONTENT_ITEM_TYPE, todoUri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String type = (String) mTypes.getSelectedItem();
        String summary = mSummaryText.getText().toString();
        String description = mDescriptionText.getText().toString();

        if (TextUtils.isEmpty(description) && TextUtils.isEmpty(summary)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(TodoTable.COLUMN_CATEGORY, type);
        values.put(TodoTable.COLUMN_SUMMARY, summary);
        values.put(TodoTable.COLUMN_DESCRIPTION, description);

        if (todoUri == null) {
            todoUri = getContentResolver().insert(TodoContentProvider.CONTENT_URI, values);
        } else {
            getContentResolver().update(todoUri, values, null, null);
        }
    }

    private void loadData(Uri uri) {
        String[] projection = { TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_DESCRIPTION, TodoTable.COLUMN_CATEGORY };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            String category = cursor.getString(cursor
                    .getColumnIndexOrThrow(TodoTable.COLUMN_CATEGORY));
            //Select task type
            for (int i = 0; i < mTypes.getCount(); i++) {

                String s = (String) mTypes.getItemAtPosition(i);
                if (s.equalsIgnoreCase(category)) {
                    mTypes.setSelection(i);
                }
            }
            //Set task summary
            mSummaryText.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(TodoTable.COLUMN_SUMMARY)));
            //Set task description
            mDescriptionText.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(TodoTable.COLUMN_DESCRIPTION)));

            cursor.close();
        }
    }

}
