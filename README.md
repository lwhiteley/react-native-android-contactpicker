## react-native-android-contactpicker

This is a react native module that wraps [Android-ContactPicker](https://github.com/1gravity/Android-ContactPicker) to facilitate selecting multiple contacts in one intent. This is for android version 5 (or higher) only.

## Installation

```js
npm install --save react-native-android-contactpicker
```

## Usage Example

```js
var ContactPicker = require('react-native-android-contactpicker')

ContactPicker.open({
  theme: ContactPicker.Themes.LIGHT
})
.then( (contacts) => {
  console.log(contacts)
})
.catch( (err) => {
  console.log(err)
})

/**
Sample contact list:
[
  {
    id: "100",
    name: {
      display:"John Doe",
      first: "John",
      last: "Doe"
    },
    phoneNumbers: [ {"number": "+1-555-555-5555"} ],
    emailAddresses: [ {"email": "john.doe@email.com"} ]
  }
]
**/
```

### Options

| Property  | Description  |
|---|:---|
|  **theme** (int)  |  This option sets the theme for  [Android-ContactPicker](https://github.com/1gravity/Android-ContactPicker) multi-select view only <br/> Default: `ContactPicker.Themes.LIGHT` |  

### Constants

```
ContactPicker.Themes = {
  DARK,
  LIGHT
}

ContactPicker.Errors = {
  UNSUPPORTED,
  USER_CANCEL
}
```

## Getting Started - Android
* In `android/settings.gradle`
```gradle
...

include ':react-native-android-contactpicker'
project(':react-native-android-contactpicker').projectDir = new File(settingsDir, '../node_modules/react-native-android-contactpicker/android')

```

* In `android/app/build.gradle`
```gradle
...
dependencies {
    ...
    compile project(':react-native-android-contactpicker')
}
```

* register module (in android/app/src/main/java/{your-app-namespace}/MainApplication.java)
```java
import com.lwhiteley.reactnativecontactpicker.RNContactPicker; // <------ add import

public class MainApplication extends Application implements ReactApplication {
  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {

    ...

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new RNContactPicker()
      );
    }
  };

  ...
}
```

* Add Contacts permission and Activity (in android/app/src/main/AndroidManifest.xml)
```xml
...
  <uses-permission android:name="android.permission.READ_CONTACTS" />
...

<application
     android:name=".MainApplication"
     android:allowBackup="true"
     android:label="@string/app_name"
     android:icon="@mipmap/ic_launcher"
     android:theme="@style/AppTheme">

     ...

        <activity
            android:name="com.onegravity.contactpicker.core.ContactPickerActivity"
            android:enabled="true"
            android:exported="false" >

            <intent-filter>
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
            </intent-filter>
        </activity>

      ...
</application>
```
## Additional Notes

- The properties phoneNumbers and emailAddresses will be returned as empty arrays if no phone numbers or emails are found.

### Possible Promise Rejection Reasons

The following will cause a rejection that invokes the catch method of the promise that indicates an error (use the console.log to see the specific message):

1) Android Version below 5.0 is used.

2) User denies access to the addressbook

4) User hits the back button and never picks a contact.

### Known issues

- If you select too many contacts, there will be an exception that crashes the app. [details](https://www.neotechsoftware.com/blog/android-intent-size-limit). Possible solution would be to find a way to limit the selected contacts

## Acknowledgements and Special Notes

- [@rhaker's](https://github.com/rhaker/) [react-native-select-contact-android](https://github.com/rhaker/react-native-select-contact-android) [Issue #5](https://github.com/rhaker/react-native-select-contact-android/issues/5) started the initiative.
