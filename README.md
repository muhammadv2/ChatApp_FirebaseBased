# PMme is a Firebase based application 

## Firebase features used

* Authorization
* Storage
* Database

## Getting Started

**Stage2 is still under development**

To start using the sample just clone the project and add your own copy of  google-services file from your google account to the app
directory [There is how start using firebase guide](https://firebase.google.com/docs/android/setup) 

The code itself is documented and well commented "i guess" if you need any further explanation please pm me :)

### [Video showing the application working](https://youtu.be/cRNqF4fEdWI) 

### Storage rules

``` service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
      request.resource.size < 3 * 1024 * 1024;

    }
  }
}
```

### Database rules

```
{
 "rules": {
      // only authenticated users can read and write the messages node
     ".read": "auth != null",
     ".write": "auth != null",
   "users":{
     "$userId":{
       ".validate": "!data.exists()"
     }
   },
   
   "messages": {

   }
 }
}
```

#### P.S. This's the first stage of the sample, i will refactor the code using MVP, DAGGER and RxAndroid 


## License

   Copyright [2018] [Muhammad Ismail]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

## Acknowledgments

* Udacity team who teach me alot including firebase course that this code is based on it [you can find the course here](https://eg.udacity.com/course/firebase-in-a-weekend-by-google-android--ud0352) 
