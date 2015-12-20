package com.alexanderhaase.photodialer;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DialerActivity extends AppCompatActivity {

    static final String TAG = "DialerActivity";
    static public final String EXTRA_PHOTO_URI = "com.alexanderhaase.photodialer.extra_photo_uri";
    static public final String EXTRA_PHONE_URI = "com.alexanderhaase.photodialer.extra_phone_uri";

    ImageButton photoButton;
    Button cancelButton;

    Uri phoneURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        photoButton = (ImageButton) findViewById(R.id.contactPhoto);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        final Intent intent = getIntent();
        final String uuid = intent.getStringExtra(EXTRA_PHOTO_URI);
        phoneURI = intent.getParcelableExtra(EXTRA_PHONE_URI);

        // set image on button or fail gracefully
        try {
            loadImageUri(uuid, photoButton);
        } catch( Exception e ) {
            Log.e( TAG, "Failed to load photo by UUID: " + uuid, e );
        }

        // set action on photo click
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // resolve phone number
                Log.i( TAG, "Dialing: " + phoneURI);
                startActivity(new Intent( Intent.ACTION_CALL, phoneFromContactURI( phoneURI )));
                finish();
            }
        });

        // set action on cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i( TAG, "Canceling activity: " + phoneURI);
                finish();
            }
        });
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

    final static int imageBufferSize = 64*1024;

    void loadImageUri( final String uuid, final ImageButton button) throws FileNotFoundException, IOException {
        // get button size
        final ViewGroup.LayoutParams imageLayout = button.getLayoutParams();

        // setup streams
        Log.i( TAG, "Loading photo by UUID: " + uuid );
        final InputStream stream = openFileInput( uuid );
        //final InputStream stream = getContentResolver().openInputStream( uri );
        final BufferedInputStream buffer = new BufferedInputStream( stream, imageBufferSize );

        // Find image size
        final BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        buffer.mark(imageBufferSize);
        BitmapFactory.decodeStream(buffer, null, boundsOptions);
        buffer.reset();

        // create scale
        final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = Math.max( 1, Math.min( boundsOptions.outWidth/imageLayout.width, boundsOptions.outHeight/imageLayout.height ) );

        // decode and install bitmap
        final Bitmap bitmap = BitmapFactory.decodeStream( buffer, null, bitmapOptions );
        button.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dialer, menu);
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
