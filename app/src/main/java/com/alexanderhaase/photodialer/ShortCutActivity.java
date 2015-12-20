package com.alexanderhaase.photodialer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class ShortCutActivity extends AppCompatActivity {

    public static Intent photoPickerIntent() {
        //http://stackoverflow.com/questions/2708128/single-intent-to-let-user-take-picture-or-pick-image-from-gallery-in-android
        Intent takePhotoIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(photoPickerIntent,"Select Photo With");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                new Intent[]{takePhotoIntent});

        return chooserIntent;
    }

    final static String TAG = "ShortCutActivity";
    final static int PHONE_RESULT = 10001;
    final static int PHOTO_RESULT = 10002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_cut);
        Intent contactPickerIntent = new Intent( Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI );
        startActivityForResult(contactPickerIntent, PHONE_RESULT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_short_cut, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    Uri phoneURI = null;
    Uri photoURI = null;

    void setPhoneURI( Uri newPhoneURI ) {
        phoneURI = newPhoneURI;
        if( ! tryComplete() ) {
            //TODO: get photo
            startActivityForResult( photoPickerIntent(), PHOTO_RESULT );
        }
    }

    void setPhotoURI( Uri newPhotoURI ) {
        photoURI = newPhotoURI;
        if( ! tryComplete() ) {
            Intent contactPickerIntent = new Intent( Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI );
            startActivityForResult(contactPickerIntent, PHONE_RESULT);
        }
    }


    Uri phoneFromContactURI( final Uri uri ) {
        try {
            Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            cursor.moveToFirst();
            return Uri.parse( "tel:" + cursor.getString(0) );
        } catch ( Exception e ) {
            Log.e( TAG, "Failed to resolve URI: " + uri, e );
            return null;
        }
    }

    boolean tryComplete() {
        if( phoneURI != null && photoURI != null ) {
            /*
                   Turns out these URIs are temporary--we need to fully resolve them.
             */
            /* store photo */
            final UUID uuid = UUID.randomUUID();
            try {
                Log.i(TAG, "Saving image to: " + uuid );
                FileOutputStream file = openFileOutput(uuid.toString(), Context.MODE_PRIVATE);
                final InputStream input = getContentResolver().openInputStream(photoURI);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    file.write(buffer, 0, bytesRead);
                }
                file.close();
                input.close();
            } catch( Exception e ) {
                Log.e(TAG, "Failed to save image " + phoneURI );
                Toast.makeText(this, "Failed to use image!", Toast.LENGTH_SHORT );
            }

            final Intent photoDialer = new Intent( this, DialerActivity.class );
            photoDialer.setComponent( new ComponentName( getApplicationContext(), DialerActivity.class ));
            photoDialer.setAction(Intent.ACTION_MAIN);
            photoDialer.putExtra( DialerActivity.EXTRA_PHONE_URI, phoneURI ); // phoneFromContactURI( phoneURI ) );
            photoDialer.putExtra( DialerActivity.EXTRA_PHOTO_URI, uuid.toString() );

            final Intent shortcutIntent = new Intent();
            shortcutIntent.putExtra( Intent.EXTRA_SHORTCUT_INTENT, photoDialer );

            shortcutIntent.putExtra( Intent.EXTRA_SHORTCUT_NAME, "Test");
            //shortcutIntent.putExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE, photoURI );
            shortcutIntent.setAction( "com.android.launcher.action.INSTALL_SHORTCUT" );

            setResult(RESULT_OK, shortcutIntent);
            finish();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_OK ) {
            switch(requestCode) {
                case PHONE_RESULT: {
                    Uri result = data.getData();
                    Log.v(TAG, "Received phone number: " + result.toString());
                    setPhoneURI(result);
                    break;
                }

                case PHOTO_RESULT: {
                    Uri result = data.getData();
                    Log.v(TAG, "Received phone number: " + result.toString());
                    setPhotoURI(result);
                    break;
                }
            }

        } else {
            Log.w(TAG, "Non-success result for " + requestCode);
        }
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
