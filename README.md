## react-native-android-contactpicker

This is a react native module that wraps [Android-ContactPicker](https://github.com/1gravity/Android-ContactPicker) to facilitate selecting multiple contacts in one intent. This is for android version 5 (or higher) only.

> **NB.** Temporarily using `napa` to install the android-contactpicker library until v1.2 is released

## Installation

```js
npm install --save react-native-android-contactpicker
```

## Usage Example

```js
var ContactPicker = require('react-native-android-contactpicker')

ContactPicker.open({
  theme: ContactPicker.Themes.LIGHT,
  limit: 20,
  onlyWithPhone: true
})
.then( (contacts) => {
  console.log(contacts)
})
.catch( (err) => {
  console.log(err.code, err.message)
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
|  **limit** (int)  |  This parameter will limit the amount of contacts that can be selected per intent. When set to zero, then no limiting will be enforced <br/> Default: `0` |
|  **limitReachedMessage** (String)  |  This parameter sets the text displayed as a toast when the set limit is reached <br/> Default: `You can't pick more than {limit} contacts!` |
|  **showCheckAll** (Boolean)  |  This parameter decides whether to show/hide the check_all button in the menu. When `limit` > 0, this will be forced to `false`.  <br/> Default: `true` |
|  **onlyWithPhone** (Boolean)  |  This parameter sets the boolean that filters contacts that have no phone numbers <br/> Default: `false` |


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
//########################
//Temporarily
include ':android-contactpicker'
project(':android-contactpicker').projectDir = new File(settingsDir, '../node_modules/react-native-android-contactpicker/node_modules/android-contactpicker/library')
//########################

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

- If you select too many contacts, there will be an exception that crashes the app. [details](https://www.neotechsoftware.com/blog/android-intent-size-limit). The Best way to avoid this is to limit the amount of contacts a user can select per intent.

## Acknowledgements and Special Notes

- [@rhaker's](https://github.com/rhaker/) [react-native-select-contact-android](https://github.com/rhaker/react-native-select-contact-android) [Issue #5](https://github.com/rhaker/react-native-select-contact-android/issues/5) started the initiative.
- [@1gravity](https://github.com/1gravity/) for the awesome library and being open to accepting new features in the [Android-ContactPicker](https://github.com/1gravity/Android-ContactPicker) library.
