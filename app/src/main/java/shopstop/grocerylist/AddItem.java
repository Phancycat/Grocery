package shopstop.grocerylist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import shopstop.grocerylist.parse.ParseItem;
import shopstop.grocerylist.parse.ParseObjectHandler;
import shopstop.grocerylist.parse.ParsePrice;
import shopstop.grocerylist.parse.ParseStore;
import shopstop.grocerylist.tasks.AddPriceTask;
import shopstop.grocerylist.tasks.Geocoding;
import shopstop.grocerylist.tasks.GetBarcode;
import shopstop.grocerylist.tasks.HTTPResponse;
import shopstop.grocerylist.tasks.SearchBarcodeTask;


public class AddItem extends ActionBarActivity implements HTTPResponse {

    private EditText mItem;
    private EditText mBarcode;
    private EditText mPrice;
    private EditText mQuant;
    private EditText mUnit;
    private EditText mStore;
    private EditText mAddress;
    private Button mAddButton;
    private String barcode;
    private String api_key = "b000281a6ef7ab7de23ce178afd6faf3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        actionBar.setTitle("Add Price");

        mItem = (EditText)findViewById(R.id.itemEdit);
        mBarcode = (EditText)findViewById(R.id.barcodeEdit);
        mPrice = (EditText)findViewById(R.id.priceEdit);
        mQuant = (EditText)findViewById(R.id.quantEdit);
        mUnit = (EditText)findViewById(R.id.unitEdit);
        mStore = (EditText)findViewById(R.id.storeEdit);
        mAddress = (EditText)findViewById(R.id.addressEdit);
        mAddButton = (Button) findViewById(R.id.addButton);

        barcode = null;
        setListeners();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan) {
            IntentIntegrator integrator = new IntentIntegrator(AddItem.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
            integrator.setPrompt("Scan a barcode");
            integrator.setResultDisplayDuration(0);
            integrator.setWide();  // Wide scanning rectangle, may work better for 1D barcodes
            integrator.setCameraId(0);  // Use a specific camera of the device
            integrator.initiateScan();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_add_item, container, false);
            return rootView;
        }
    }

    private void setListeners() {

        final Activity act = this;

        mItem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = mItem.getText().toString();

                if (str.equals("")) {
                    mItem.setError("Input is required!");
                    mItem.setBackgroundColor(getResources().getColor(R.color.transred));
                }
                else if (!str.matches("^[a-zA-Z0-9][a-zA-Z0-9 .,'%\\-\\/\\(\\)]++")) {
                    mItem.setError("Only alphanumeric and special characters: .,'% are allowed!");
                    mItem.setBackgroundColor(getResources().getColor(R.color.transred));
                }
                else {
                    mItem.setError(null);
                    mItem.setBackgroundColor(getResources().getColor(R.color.transgreen));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = mBarcode.getText().toString();

                if (str.equals("") || str.matches("[a-zA-Z0-9 -]*")) {
                    mBarcode.setError(null);
                    mBarcode.setBackgroundColor(getResources().getColor(R.color.transgreen));
                }
                else {
                    mBarcode.setError("Only alphanumeric and special characters: - are allowed!");
                    mBarcode.setBackgroundColor(getResources().getColor(R.color.transred));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = mPrice.getText().toString();

                if (str.equals("")) {
                    mPrice.setError("Input is required!");
                    mPrice.setBackgroundColor(getResources().getColor(R.color.transred));
                }
                else if (!str.matches("\\d+[.]?\\d*")) {
                    mPrice.setError("Only numbers are allowed!");
                    mPrice.setBackgroundColor(getResources().getColor(R.color.transred));
                }
                else  {
                    mPrice.setError(null);
                    mPrice.setBackgroundColor(getResources().getColor(R.color.transgreen));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mQuant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = mQuant.getText().toString();

                if (str.equals("") || str.matches("\\d+[.]?\\d*")) {
                    mQuant.setError(null);
                    mQuant.setBackgroundColor(getResources().getColor(R.color.transgreen));
                }
                else {
                    mQuant.setError("Only numbers are allowed!");
                    mQuant.setBackgroundColor(getResources().getColor(R.color.transred));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = mUnit.getText().toString();

                if (str.equals("") || str.matches("[a-zA-Z ]*")) {
                    mUnit.setError(null);
                    mUnit.setBackgroundColor(getResources().getColor(R.color.transgreen));
                }
                else {
                    mUnit.setError("Only letters are allowed!");
                    mUnit.setBackgroundColor(getResources().getColor(R.color.transred));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mStore.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = mStore.getText().toString();

                if (str.equals("")) {
                    mStore.setError("Input is required!");
                    mStore.setBackgroundColor(getResources().getColor(R.color.transred));
                }
                else if (!str.matches("[a-zA-Z0-9 ]++")) {
                    mStore.setError("Only alphanumeric characters are allowed!");
                    mStore.setBackgroundColor(getResources().getColor(R.color.transred));
                }
                else  {
                    mStore.setError(null);
                    mStore.setBackgroundColor(getResources().getColor(R.color.transgreen));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = mAddress.getText().toString();

                if (str.equals("")) {
                    mAddress.setError("Input is required!");
                    mAddress.setBackgroundColor(getResources().getColor(R.color.transred));
                }
                else if (!str.matches("[a-zA-Z0-9 .,]++")) {
                    mAddress.setError("Only alphanumeric and special characters: ., are allowed!");
                    mAddress.setBackgroundColor(getResources().getColor(R.color.transred));
                }
                else  {
                    mAddress.setError(null);
                    mAddress.setBackgroundColor(getResources().getColor(R.color.transgreen));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final ParseObjectHandler handler = new ParseObjectHandler() {
            @Override
            public void onCallComplete(ParseObject parseObject) {
                AlertDialog alertDialog = new AlertDialog.Builder(act).create();
                alertDialog.setTitle("Success");
                alertDialog.setMessage("The price for this item was added to the database!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                act.finish();
                            }
                        });
                alertDialog.show();
            }
        };

        mAddButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mItem.getText().toString().equals("") || mItem.getError() != null ||
                        mBarcode.getError() != null ||
                        mPrice.getText().toString().equals("") || mPrice.getError() != null ||
                        mQuant.getText().toString().equals("") || mQuant.getError() != null ||
                        mStore.getText().toString().equals("") || mStore.getError() != null ||
                        mAddress.getText().toString().equals("") || mAddress.getError() != null) {
                    AlertDialog alertDialog = new AlertDialog.Builder(act).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Please check that you filled in the fields correctly.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else {
                    // Finds address when user clicks search
                    Geocoding gc = new Geocoding(AddItem.this);
                    List<Address> addresses = gc.getCoord(mAddress.getText().toString(), AddItem.this);
                    Address address;
                    double lat, lon;
                    if (addresses.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Address not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    address = addresses.get(0);
                    if (!address.hasLatitude() || !address.hasLongitude()) {
                        Toast.makeText(getApplicationContext(), "Address not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    lat = address.getLatitude();
                    lon = address.getLongitude();

                    ParseStore store = new ParseStore(mStore.getText().toString(), mAddress.getText().toString(),
                            new ParseGeoPoint(lat, lon));
                    ParseItem item = new ParseItem(mItem.getText().toString(), mUnit.getText().toString(),
                            mQuant.getText().toString(), mBarcode.getText().toString());
                    ParsePrice price = new ParsePrice(mPrice.getText().toString(), item, store);

                    Log.d("add", "adding dummy data");

                    // Add the price
                    AddPriceTask task = new AddPriceTask(handler, price);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            HttpClient client = new DefaultHttpClient();
            String url = "http://api.upcdatabase.org/json/" + api_key + "/0" + scanResult.getContents();
            barcode = scanResult.getContents();
            Log.d("Trying to get this", url);
//            new GetHTTPBarcode().execute(url);
            GetBarcode task = new GetBarcode(this);
            task.execute(url);
        }
        else {
            Toast.makeText(getApplicationContext(), "no result", Toast.LENGTH_LONG).show();
        }
    }

    public void postResult(final JSONObject result) {
        if (barcode != null) {
            mBarcode.setText(barcode);

            final ProgressDialog progress = new ProgressDialog(this);

            // Now search Parse for the item
            ParseObjectHandler handler = new ParseObjectHandler() {
                @Override
                public void onCallComplete(ParseObject parseObject) {
                    progress.dismiss();

                    if (parseObject == null) {
                        try {
                            String name = result.get("itemname").toString();
                            Log.d("result", name);
                            if (!name.isEmpty()) {
                                mItem.setText(name);
                                mQuant.setText("");
                                mUnit.setText("");
                            }
                            else {
                                Toast.makeText(getApplicationContext(),
                                        "Item not found in database.", Toast.LENGTH_LONG).show();
                                mItem.setText("");
                                mQuant.setText("");
                                mUnit.setText("");
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),
                                    "Item not found in database.", Toast.LENGTH_LONG).show();
                            mItem.setText("");
                            mQuant.setText("");
                            mUnit.setText("");
                        }
                    }
                    else {
                        mItem.setText(parseObject.getString("name"));
                        mQuant.setText(parseObject.getString("unitCount"));
                        mUnit.setText(parseObject.getString("unitName"));
                    }
                }
            };

            progress.setMessage("Searching for item...");
            progress.show();

            SearchBarcodeTask parseTask = new SearchBarcodeTask(handler, barcode);
            parseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            mBarcode.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.animator.downin, R.animator.downout);

        return;
    }
}
