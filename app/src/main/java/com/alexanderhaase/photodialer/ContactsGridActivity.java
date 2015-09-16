package com.alexanderhaase.photodialer;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class ContactsGridActivity extends AppCompatActivity {

    //String items[] = { "joe", "bob", "riggs" };

    /*
    * Defines an array that contains column names to move from
    * the Cursor to the ListView.
    */
    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME
    };
    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            android.R.id.text1
    };

    private final String[] queryColumns = {
            ContactsContract.Contacts._ID,
            FROM_COLUMNS[ 0 ],
            ContactsContract.Contacts.PHOTO_ID,
    };

    private final String queryString =
            ContactsContract.Contacts.HAS_PHONE_NUMBER + " == 1 and " +
            ContactsContract.Contacts.PHOTO_ID + " != NULL ";

    SimpleCursorAdapter mCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_grid);

        final GridView gridView = (GridView) findViewById( R.id.gridview );
        //gridView.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_contact_item, items ));

        mCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.activity_contact_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0);

        gridView.setAdapter( mCursorAdapter );

        ContentResolver resolver = getContentResolver();

        Cursor cursor = resolver.query( ContactsContract.Contacts.CONTENT_URI, queryColumns, queryString, null, null );
        mCursorAdapter.changeCursor( cursor );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
