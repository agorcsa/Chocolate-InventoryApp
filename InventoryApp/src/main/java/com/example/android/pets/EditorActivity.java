/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.pets.data.ChocolateContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;

/**
 * Allows user to create a new chocolate entry or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    static final int RESULT_LOAD_PICTURE = 1;
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private EditText mNameEditText;
    private Uri mCurrentChocolateUri;
    private Uri pictureUri;
    private ImageView mPictureView;
    private EditText mPriceEditText;
    private EditText mQuantitytEditText;
    private int quantity;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;
    private EditText mSupplierEmailEditText;
    private String mSavePictureText;
    private Button decrementButton;
    private Button incrementButton;
    private boolean mChocolateHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mChocolateHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        //** Use getIntent() and getData to get the associated URI */
        Intent intent = getIntent();
        mCurrentChocolateUri = intent.getData();
        if (mCurrentChocolateUri == null) {
            /** add a new chocolate */
            setTitle(getString(R.string.editor_activity_title_add_chocolate));
            //* Invalidate the options menu, so the "Delete" menu option can be hidden. */
            // (It doesn't make sense to delete a chocolate that hasn't been created yet.) */
            invalidateOptionsMenu();
        } else {
            /** edit an existing chocolate*/
            setTitle(getString(R.string.editor_activity_title_edit_chocolate));
        }
        mPictureView = (ImageView) findViewById(R.id.new_picture);
        mPictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_PICTURE);
            }
        });
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.chocolate_name);
        mPriceEditText = (EditText) findViewById(R.id.chocolate_price);
        mQuantitytEditText = (EditText) findViewById(R.id.chocolate_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.supplier_phone);
        mSupplierEmailEditText = (EditText) findViewById(R.id.supplier_email);
        decrementButton = (Button) findViewById(R.id.minus_button);
        incrementButton = (Button) findViewById(R.id.plus_button);
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantitytEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        //* By each click the decrement button will reduce quantity with 1 unit. */
        decrementButton.setOnTouchListener(mTouchListener);
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Integer variable for quantity changer that will be initiated with
                // mQuantityEditText value.
                if (isEmpty(mQuantitytEditText.getText().toString())) {
                    Toast.makeText(EditorActivity.this, R.string.positive_quantity,
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    quantity = Integer.parseInt(valueOf(mQuantitytEditText.getText().toString()));
                    if (quantity == 0) {
                        Toast.makeText(EditorActivity.this, R.string.positive_quantity,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        quantity = quantity - 1;
                        mQuantitytEditText.setText(valueOf(quantity));
                    }
                }
            }
        });
        //* By each click the increment button will add 1 unit to the quantity. */
        incrementButton.setOnTouchListener(mTouchListener);
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Integer variable for quantity changer that will be initiated with
                // mQuantityEditText value.
                if (isEmpty(mQuantitytEditText.getText().toString())) {
                    Log.e(LOG_TAG, getString(R.string.empty_stock));
                    return;
                } else {
                    quantity = Integer.parseInt(mQuantitytEditText.getText().toString());
                    quantity = quantity + 1;
                    mQuantitytEditText.setText(valueOf(quantity));
                }
            }
        });
        FloatingActionButton phoneFab = (FloatingActionButton) findViewById(R.id.phone_order);
        phoneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mSupplierPhoneEditText.getText().toString()));
                startActivity(intent);
            }
        });
        FloatingActionButton emailFab = (FloatingActionButton) findViewById(R.id.email_order);
        emailFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + mSupplierEmailEditText.getText().toString()));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_email_subject) +
                        mNameEditText.getText().toString());
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.order_email));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_PICTURE && resultCode == RESULT_OK && null != data) {
            try {
                pictureUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + pictureUri.toString());
                mSavePictureText = pictureUri.toString();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    if (Build.VERSION.SDK_INT > 19) {
                        getContentResolver().takePersistableUriPermission(pictureUri, takeFlags);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                mPictureView.setImageBitmap(getBitmapFromUri(pictureUri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().isEmpty())
            return null;
        int targetW = mPictureView.getWidth();
        int targetH = mPictureView.getHeight();
        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;
        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
            }
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mChocolateHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //* User clicked "Discard" button, close the current activity. */
                        finish();
                    }
                };
        // Show that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveChocolate() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantitytEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        /**Check the validity of the data */
        if (mCurrentChocolateUri == null && TextUtils.isEmpty(mSavePictureText) && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty((priceString)) && TextUtils.isEmpty(quantityString)) {
            return;
        }
        if (pictureUri == null) {
            return;
        }
        // Create a ContentValues object
        ContentValues values = new ContentValues();
        values.put(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PICTURE, pictureUri.toString());
        values.put(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_NAME, nameString);
        //* If the price and quantity are not provided by the user, don't parse the string
        //* into an integer value. Use 0 by default for both of them. */
        Double price = 0.0;
        int quantity = 0;
        if (!isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        } else {
            Toast.makeText(this, R.string.null_quantity, Toast.LENGTH_SHORT).show();
        }
        if (!isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        } else {
            Toast.makeText(this, R.string.null_quantity, Toast.LENGTH_SHORT).show();
        }
        values.put(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PRICE, price);
        values.put(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY, quantity);
        values.put(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_NAME, supplierNameString);
        values.put(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_PHONE, supplierPhoneString);
        values.put(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_EMAIL, supplierEmailString);
        // Determine if this is a new or existing chocolate item by checking if mCurrentChocolateUri is null or not
        try {
            if (mCurrentChocolateUri == null) {
                ContentResolver cr = getContentResolver();
                Uri newUri;
                newUri = cr.insert(ChocolateContract.ChocolateEntry.CONTENT_URI, values);
                //show a toast message depending on whether or not the insertion  was successful
                if (newUri == null) {
                    //If the new content URI is null , then there was an error with insertion
                    Toast.makeText(this, getString(R.string.editor_insert_failed), Toast.LENGTH_SHORT).show();
                } else {
                    //otherwise, the insertion was successful and we can display a toast
                    Toast.makeText(this, getString(R.string.editor_insert_successfull), Toast.LENGTH_SHORT).show();
                    mCurrentChocolateUri = newUri; // ~~~added this line
                }
            } else {
                int rowsAffected = getContentResolver().update(mCurrentChocolateUri, values, null, null);
                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/edit_menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    //This method is called after invalidateOptionsMenu(), so that the menu can be updated
    // (some menu items can be hidden or made visible)
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new entry, hide the "Delete" menu item.
        if (mCurrentChocolateUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save chocolate to database
                saveChocolate();
                //exist activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mChocolateHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define a projection that specifies the columns from the table that we are interested
        String[] projection = {
                ChocolateContract.ChocolateEntry._ID,
                ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PICTURE,
                ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_NAME,
                ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PRICE,
                ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY,
                ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_NAME,
                ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_EMAIL,
                ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_SUPPLIER_PHONE};
        //This loader will execute the ContentProvider 's query method on a background thread
        return new CursorLoader(this,
                mCurrentChocolateUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            int imageColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PICTURE);
            int nameColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.
                    COLUMN_CHOCOLATE_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.
                    COLUMN_CHOCOLATE_SUPPLIER_PHONE);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.
                    COLUMN_CHOCOLATE_SUPPLIER_EMAIL);
            // Extract out the value from the Cursor for the given column index
            String image = cursor.getString(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            // Update the views on the screen with the values from the database
            if (image != null) {
                pictureUri = Uri.parse(image);
                mPictureView.setImageURI(pictureUri);
            }
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantitytEditText.setText(Integer.toString(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhone);
            mSupplierEmailEditText.setText(supplierEmail);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if the loader is invalidated , clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantitytEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mSupplierEmailEditText.setText("");
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteChocolate();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the chocolate item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteChocolate() {
        //* Only perform the delete if this is an existing chocolate item */
        if (mCurrentChocolateUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentChocolateUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}


