# Talks - Twitter clone

A social network similar to Twitter.
Made with Kotlin and Firebase.
All registered users can share posts of up to 500 characters, with the possibly to add a photo.

![Poster 1](app/src/main/poster1.png)


## Backend
The architecture of this application is based on Google Firebase.
- Firebase Auth: user management
- Firebase Firestore database: users collection and tweets collection
- Firebase Storage: to store profile photos and media of tweets
- Firebase Crashlytics: for runtime error collection
- Firebase Analytics: for user monitoring


## Functionality
- Dynamic application theme (dark or light)
- Registration with email and password
- Login with email and password
- Password reset with email
- Logout
- Edit account information: name, username, profile bio, profile picture
- Verified account badge for eligible users
- View all tweets on the dashboard
- Viewing a user's tweets (by clicking on their photo) (User detail page)
- Ability to like and unlike tweets
- Create a text-only tweet
- Creation of a tweet with text and image (from the gallery or taken at the moment)
- Search bar to filter the tweet text
- Scroll down to refresh tweet list
- Supported languages: Italian and English (dynamically)


![Poster 2](app/src/main/poster2.png)
