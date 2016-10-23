package com.lwhiteley.reactnativecontactpicker;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.content.Intent;
import android.database.Cursor;
import android.os.CountDownTimer;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.group.Group;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RNContactPickerManager extends ReactContextBaseJavaModule implements ActivityEventListener {

    // initialize variables
    private static final int REQUEST_CONTACT = 1766909987;
    private Activity mActivity = null;
    static int foundFlag = 0;
    static int interval = 0;
    static WritableArray contactMaps;
    static CountDownTimer counter;

    // set the activity - pulled in from Main
    public RNContactPickerManager(ReactApplicationContext reactContext) {
        super(reactContext);
        // Add the listener for `onActivityResult`
        reactContext.addActivityEventListener(this);
    }

    public void onNewIntent(Intent intent) {

    }

    public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
        this.handleMultipleContactsActivityResult(requestCode, resultCode, intent);
    }

    protected boolean handleMultipleContactsActivityResult(int requestCode, int resultCode, Intent data) {
        if (mActivity == null) {
            Log.i(RNContactConstants.COMPONENT_NAME, "Activity is null, may not be the current intent or there is a problem");
            return false;
        }
        if (resultCode == mActivity.RESULT_CANCELED) {
            // set the flag to indicate user hit back button in address book (polling stops)
            foundFlag = 2;
            return false;
        }
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK &&
                data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {

            // process contacts
            List<String> contactIds = new ArrayList<>();
            contactMaps = Arguments.createArray();
            List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            List<Group> groups = (List<Group>) data.getSerializableExtra(ContactPickerActivity.RESULT_GROUP_DATA);
            for (Group group : groups) {
                // process the groups...
                contacts.addAll(group.getContacts());
            }

            for (Contact contact : contacts) {
                String id = Long.toString(contact.getId());

                if (!contactIds.contains(id)) {
                    contactIds.add(id);
                    WritableMap contactMap = Arguments.createMap();
                    contactMap.putString(RNContactConstants.ID_PROP_NAME, id);
                    contactMap.putMap(RNContactConstants.NAME_PROP_NAME, getContactName(contact));
                    contactMap.putArray(RNContactConstants.PHONE_PROP_NAME, getPhones(mActivity, id));
                    contactMap.putArray(RNContactConstants.EMAIL_PROP_NAME, getEmails(mActivity, id));
                    contactMaps.pushMap(contactMap);
                }
            }
            // set the flag to indicate selection made (polling stops)
            foundFlag = 1;
        }
        return true;
    }


    @ReactMethod
    public void open(final ReadableMap options, final Promise promise) {
        mActivity = getCurrentActivity();
        final Activity finalActivity = mActivity;

        // reset values in case multiple picks
        interval = 0;
        foundFlag = 0;
        counter = null;
        int timeout = 45000;


        if (options != null && options.hasKey("timeout") && options.getInt("timeout") > 0) {
            timeout = options.getInt("timeout");
            Log.i(RNContactConstants.COMPONENT_NAME, "custom timeout set: " + timeout + " ms");
        }

        // check if android version < 5.0
        if ((android.os.Build.VERSION.RELEASE.startsWith("1.")) ||
                (android.os.Build.VERSION.RELEASE.startsWith("2.")) ||
                (android.os.Build.VERSION.RELEASE.startsWith("3.")) ||
                (android.os.Build.VERSION.RELEASE.startsWith("4."))) {

            promise.reject(
                    RNContactConstants.UNSUPPORTED,
                    generateCustomError("android version not supported: " + Build.VERSION.RELEASE));

        } else {

            // start the activity to pick the contact from addressbook
            int theme = R.style.ContactPicker_Theme_Light;
            if (options != null && options.hasKey("theme")) {
                int _theme = options.getInt("theme");
                if (getValidThemes().contains(_theme)) {
                    theme = _theme;
                }
            }
            Intent pickContactIntent = new Intent(mActivity, ContactPickerActivity.class)
                    .putExtra(ContactPickerActivity.EXTRA_THEME, theme)
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());
            mActivity.startActivityForResult(pickContactIntent, REQUEST_CONTACT);

            // poll for user input selection - max of 45 seconds
            counter = new CountDownTimer(timeout, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // cancel polling if user picks
                    if (foundFlag == 1) {
                        // cancel countdown, send result
                        counter.cancel();
                        promise.resolve(contactMaps);
                    }

                    // cancel polling if user hits back button
                    if (foundFlag == 2) {
                        // cancel countdown, send result
                        counter.cancel();
                        // send user canceled
                        promise.reject(RNContactConstants.USER_CANCEL, generateCustomError("user canceled"));
                    }

                }

                @Override
                public void onFinish() {
                    // poll for 45 cycles - or 45 seconds max
                    if (foundFlag == 0) {
                        if (options != null && options.hasKey("closeOnTimeout") && options.getBoolean("closeOnTimeout")) {
                            finalActivity.finishActivity(REQUEST_CONTACT);
                        }
                        // send timed out result
                        promise.reject(RNContactConstants.TIMEOUT, generateCustomError("timed out"));
                    }
                }
            }.start();

        }

    }

    private WritableMap getContactName(Contact contact) {
        WritableMap meta = Arguments.createMap();
        meta.putString("display", contact.getDisplayName());
        meta.putString("first", contact.getFirstName());
        meta.putString("last", contact.getLastName());
        return meta;
    }

    private Throwable generateCustomError(String message) {
        Throwable throwObj = new Exception(message);
        return throwObj;
    }

    private WritableArray getPhones(Activity mActivity, String id) {
        WritableArray array = Arguments.createArray();

        Cursor cursor = mActivity.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                null, null);
        while (cursor != null && cursor.moveToNext()) {
            String cNumber = cursor.getString(cursor.getColumnIndex("data1"));
            if (!cNumber.isEmpty()) {
                WritableMap map = Arguments.createMap();
                map.putString("number", cNumber);
                array.pushMap(map);
            }
        }

        return array;
    }

    private WritableArray getEmails(Activity mActivity, String id) {
        WritableArray array = Arguments.createArray();

        Cursor cursor = mActivity.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID
                        + " = ?", new String[]{id}, null);

        while (cursor != null && cursor.moveToNext()) {
            String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            if (!email.isEmpty()) {
                WritableMap map = Arguments.createMap();
                map.putString("email", email);
                array.pushMap(map);
            }
        }

        return array;
    }


    @Override
    public String getName() {
        return RNContactConstants.COMPONENT_NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("Themes", getThemeMap());
        constants.put("Errors", getErroMap());
        return constants;
    }

    private WritableMap getThemeMap() {
        WritableMap map = Arguments.createMap();
        map.putInt("DARK", R.style.ContactPicker_Theme_Dark);
        map.putInt("LIGHT", R.style.ContactPicker_Theme_Light);
        return map;
    }
    private WritableMap getErroMap() {
        WritableMap map = Arguments.createMap();
        map.putString("TIMEOUT", RNContactConstants.TIMEOUT);
        map.putString("UNSUPPORTED", RNContactConstants.UNSUPPORTED);
        map.putString("USER_CANCEL", RNContactConstants.USER_CANCEL);
        return map;
    }

    private List<Integer> getValidThemes() {
        List<Integer> themes = new ArrayList<>();
        themes.add(R.style.ContactPicker_Theme_Dark);
        themes.add(R.style.ContactPicker_Theme_Light);
        return themes;
    }
}
