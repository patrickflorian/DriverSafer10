package com.potholes.driversafer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.potholes.View.Dialog.RegistrationDialogBuilder;
import com.potholes.db.Account;
import com.potholes.db.HttpHandler;
import com.potholes.db.Settings;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "LoginActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private CreateAccount mRegistrationTask;
    private AlertDialog registrationDialog;
    private TextView login, password;
    private FindAccount myLoginTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        if (isServicesOK()) {
            init();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, DetectionActivity.class);
                startActivity(intent);
            }
        });
        init();
        hideSoftKeyboard();
    }

    private void showNavigationActivity() {
        Intent intent = new Intent(LoginActivity.this, Navigation.class);
        startActivity(intent);
        myLoginTask = null;
    }

    private void init() {
        login = findViewById(R.id.login);
        password = findViewById(R.id.password);

        Button register = findViewById(R.id.email_register_button);
        Button btnMap = findViewById(R.id.email_sign_in_button);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNavigationActivity();
                //attemptLogin();
            }

        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerDialog();
            }
        });

    }

    private void registerDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.registration_dialog, null);

        final RegistrationDialogBuilder dialogBuilder = new RegistrationDialogBuilder(LoginActivity.this, alertView);
        dialogBuilder.setCancelable(true);
        registrationDialog = dialogBuilder.create();
        dialogBuilder.setPositiveButtonListener("register", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mRegistrationTask != null && dialogBuilder.attemptRegistration()) {
                    //si la tachhe de creation est nulle ou le formulaire es mal rempli
                    registrationDialog.cancel();
                } else {
                    mRegistrationTask = new CreateAccount(dialogBuilder.email.getText().toString(),
                            dialogBuilder.pass.getText().toString(),
                            dialogBuilder.cni.getText().toString(),
                            dialogBuilder.name.getText().toString());
                    mRegistrationTask.execute();

                }
            }
        });
        dialogBuilder.setNegativeButtonListener("cancel", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrationDialog.dismiss();
                Toast.makeText(getApplication(),
                        "No button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        registrationDialog.show();
    }


    public boolean isServicesOK() {
        Log.d(TAG, " isServicesOK : checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and user can map request
            Log.d(TAG, "isService Working :  GooglePlayServices is Working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            // an error occured but we can resolve it
            Log.d(TAG, "isServicesOk : an Error Occured but wecan resolve it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LoginActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void attemptLogin() {
        if (myLoginTask != null) return;
        // Reset errors.
        login.setError(null);
        password.setError(null);


        // Store values at the time of the login attempt.

        String loginVal = login.getText().toString();

        String passwordVal = password.getText().toString();


        View focusView = null;
        boolean cancel = false;
        //check valid login
        if (TextUtils.isEmpty(loginVal)) {
            login.setError(getApplicationContext().getString(R.string.error_field_required));
            focusView = login;
            cancel = true;
        } else if (!isEmailValid(loginVal)) {
            this.login.setError(getApplicationContext().getString(R.string.error_invalid_email));
            focusView = login;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            myLoginTask = new FindAccount();
            myLoginTask.execute((Void) null);
        }

    }

    private boolean isEmailValid(String loginVal) {
        return loginVal.contains("@");
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    private class FindAccount extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(LoginActivity.this, "Json Data is downloading", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler("GET");
            // Making a request to url and getting response
            String url = "http://" + Settings.SERVER_IP + "/potholes/app/account.php?login=" + login.getText().toString() + "&pass=" + password.getText().toString();
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node

                    // looping through All Contacts

                    String nom = jsonObj.getString("nom");
                    String cni = jsonObj.getString("cni");
                    int account_id = jsonObj.getInt("compte_id");
                    String login = jsonObj.getString("login");
                    String pass = jsonObj.getString("pass");
                    String type = jsonObj.getString("type");
                    int activite = jsonObj.getInt("activite");


                    // tmp hash map for single contact
                    Account.login = login;
                    Account.nom = nom;
                    Account.activite = activite;
                    Account.pass = pass;
                    Account.cni = cni;
                    Account.compte_id = account_id;
                    Account.type = type;
                    ///user_account.save(LoginActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Compte ID :: " + Account.compte_id,
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (final JSONException e) {


                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    return false;
                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Account exist", Toast.LENGTH_LONG).show();
                        Account.Exist = true;
                        showNavigationActivity();
                    }

                });
                finish();

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Wrong Login or PassWord", Toast.LENGTH_LONG).show();
                    }
                });

            }

        }
    }

    private class CreateAccount extends AsyncTask<Void, Void, Boolean> {

        String email;
        String pass;
        String cni;
        String name;

        public CreateAccount(String email, String pass, String cni, String name) {
            super();
            this.email = email;
            this.pass = pass;
            this.cni = cni;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Json Data is downloading", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler("GET");
            // Making a request to url and getting response
            String url = "http://" + Settings.SERVER_IP + "/potholes/app/register.php?email=" +
                    email + "&pass=" +
                    pass + "&cni=" +
                    cni + "&nom=" +
                    name;

            String response = sh.makeServiceCall(url);

            return response.contains("true");
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(LoginActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                        registrationDialog.dismiss();
                    }
                });


            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "unable to create this account ", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }

    }

}


