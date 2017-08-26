package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.pets.data.ChocolateContract;


public class ChocolateCursorAdapter extends CursorAdapter {
    public static final String LOG_TAG = ChocolateCursorAdapter.class.getSimpleName();
    private ImageView productPicture;
    private Uri chocolatePicture;
    private int quantity;

    public ChocolateCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return view;
    }

    /**
     * This method binds the chocolate data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current chocolate can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        ImageView pictureView = (ImageView) view.findViewById(R.id.picture);
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        //Find the columns of chocolate attributes that we're interested in
        int imageColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PICTURE);
        int nameColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY);
        //Read the chocolate attributes from the Cursor for the current chocolate
        String chocolatePictureString = cursor.getString(imageColumnIndex);
        if (chocolatePictureString != null) {
            chocolatePicture = Uri.parse(chocolatePictureString);
        }
        String chocolateName = cursor.getString(nameColumnIndex);
        String chocolatePrice = cursor.getString(priceColumnIndex);
        String chocolateQuantity = cursor.getString(quantityColumnIndex);
        // If the quantity and price are empty strings or null, then use some default text
        // that says "Unknown info", so the TextView isn't blank. And if the quantity is 0 than set
        //the no sale image
        productPicture = (ImageView) view.findViewById(R.id.cart_picture);
        final int chocolateId = cursor.getInt(cursor.getColumnIndex(ChocolateContract.ChocolateEntry._ID));
        final String finalChocolateQuantity = chocolateQuantity;
        productPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getting the int value of the String chocolateQuantity
                quantity = Integer.parseInt(finalChocolateQuantity);
                Uri chocolateUri = ContentUris.withAppendedId(ChocolateContract.ChocolateEntry.CONTENT_URI, chocolateId);
                saleButton(context, chocolateUri, quantity);
            }
        });
        if (TextUtils.isEmpty(chocolateQuantity) && TextUtils.isEmpty(chocolatePrice)) {
            chocolatePrice = context.getString(R.string.unknown_details);
            chocolateQuantity = context.getString(R.string.unknown_details);
        } else if (Integer.parseInt(chocolateQuantity) == 0) {
            productPicture.setImageResource(R.drawable.ic_remove_shopping_cart_black_24dp);
        } else {
            productPicture.setImageResource(R.drawable.ic_shopping_cart_black_24dp);
        }
        // Update the TextViews with the attributes for the current chocolate
        pictureView.setImageURI(chocolatePicture);
        nameTextView.setText(chocolateName);
        priceTextView.setText(chocolatePrice);
        quantityTextView.setText(chocolateQuantity);
    }

    private void saleButton(Context context, Uri uri, int quantity) {
        if (quantity == 0) {
            Log.e(LOG_TAG, context.getString(R.string.empty_stock));
        } else {
            quantity--;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChocolateContract.ChocolateEntry.COLUMN_CHOCOLATE_QUANTITY, quantity);
            int rowsAffected = context.getContentResolver().update(uri, contentValues, null, null);
            if (!(rowsAffected > 0)) {
                Log.e(LOG_TAG, context.getString(R.string.error_sale_update));
            }
        }
    }
}
