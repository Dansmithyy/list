/* The List powered by Creative Commons

   Copyright (C) 2014, 2015 Creative Commons

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.creativecommons.thelist.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.creativecommons.thelist.R;
import org.creativecommons.thelist.authentication.AccountGeneral;
import org.creativecommons.thelist.fragments.AccountFragment;
import org.creativecommons.thelist.fragments.ExplainerFragment;
import org.creativecommons.thelist.utils.ListApplication;
import org.creativecommons.thelist.utils.ListUser;
import org.creativecommons.thelist.utils.MessageHelper;
import org.creativecommons.thelist.utils.SharedPreferencesMethods;


public class StartActivity extends FragmentActivity implements ExplainerFragment.OnClickListener,
        AccountFragment.AuthListener {
    public static final String TAG = StartActivity.class.getSimpleName();
    ListUser mCurrentUser;
    protected Button mStartButton;
    protected Button mAccountButton;
    protected TextView mTermsLink;
    protected Context mContext;
    protected SharedPreferencesMethods mSharedPref;
    private AccountManager am;
    protected FrameLayout mFrameLayout;

    //Fragment
    ExplainerFragment explainerFragment = new ExplainerFragment();
    AccountFragment loginFragment = new AccountFragment();

    // ---------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mContext = this;
        mSharedPref = new SharedPreferencesMethods(mContext);
        mCurrentUser = new ListUser(StartActivity.this);
        am = AccountManager.get(getBaseContext());

        //Google Analytics Tracker
        ((ListApplication) getApplication()).getTracker(ListApplication.TrackerName.GLOBAL_TRACKER);

        //Create App SharedPreferences
        SharedPreferences sharedPref = mContext.getSharedPreferences
                (SharedPreferencesMethods.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);

        //TODO: add google analytics opt-in
        //Display Google Analytics Message
        if(!(mSharedPref.getGaMessageViewed())){
            //The beta version of this app uses google analytics message
            MessageHelper mh = new MessageHelper(mContext);
            mh.showDialog(mContext, "The List Beta Uses Google Analytics", "Hey just a heads up that " +
                    "we’re using Google Analytics to help us learn how to make the app better. " +
                    "We don’t collect personal info!");
            mSharedPref.setMessageViewed();
        }

        //UI Elements
        mFrameLayout = (FrameLayout)findViewById(R.id.fragment_container);
        mStartButton = (Button) findViewById(R.id.startButton);
        mAccountButton = (Button) findViewById(R.id.accountButton);
        mTermsLink = (TextView) findViewById(R.id.cc_logo_label);

        //“I’m new to the list”
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Load explainerFragment
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container,explainerFragment)
                        .commit();
                mFrameLayout.setClickable(true);
            }
        }); //StartButton ClickListener

        //“I already have an account”
        mAccountButton.setOnClickListener(new View.OnClickListener() {
            //If you have accounts > show picker; if not, show login
            @Override
            public void onClick(View v) {
                Account availableAccounts[] = am.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

                //TODO: switch getAuthed: login to first account if there if only one, if there is more than more go to accountPicker
                if(availableAccounts.length > 1){
                    mCurrentUser.showAccountPicker(new ListUser.AuthCallback() {
                        @Override
                        public void onSuccess(String authtoken) {
                            Log.d(TAG, "I have an account > Got an authtoken");
                            //TODO: is this actually needed?
                            Intent intent = new Intent(StartActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                } else {
                    mCurrentUser.getAuthed(new ListUser.AuthCallback() {
                        @Override
                        public void onSuccess(String authtoken) {
                            Log.d(TAG, "I have an account + I re-authenticated > Got an authtoken");
                            //TODO: is this actually needed?
                            Intent intent = new Intent(StartActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                }
            }
        }); //accountButton

        //Enable links
        if(mTermsLink != null){
            mTermsLink.setMovementMethod(LinkMovementMethod.getInstance());
        }
    } //OnCreate

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.v(TAG, "ON RESTART CALLED");
        if(!(mCurrentUser.isTempUser())) {
            Log.v(TAG, "ONRESTART: USER IS LOGGED IN");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.v(TAG, "ONRESTART: USER IS NOT LOGGED IN");
        }
        Log.v(TAG, "ON RESTART CALLED – AFTER START MAIN INTENT");
    } //onRestart

    @Override
    protected void onStart(){
        super.onStart();
        Log.v(TAG, "ON START CALLED");
        if(!(mCurrentUser.isTempUser())) {
            Log.v(TAG, "ONSTART: USER IS LOGGED IN");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.v(TAG, "ONSTART: USER IS NOT LOGGED IN");
        }

        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        Log.v(TAG, "ON START CALLED – AFTER GA CALLED");
    } //onStart

    @Override
    protected void onStop(){
        super.onStop();
        Log.v(TAG, "ON STOP CALLED");
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        Log.v(TAG, "ON STOP CALLED – AFTER GA CALLED");
    } //onStop

    @Override
    public void onNextClicked() {
        Intent intent = new Intent(StartActivity.this, CategoryListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUserSignedIn(Bundle userData) {
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onUserSignedUp(Bundle userData) {
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onCancelLogin() {
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .remove(loginFragment)
                .commit();
        mFrameLayout.setClickable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    } //onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    } //onOptionsItemSelected
} //StartActivity
