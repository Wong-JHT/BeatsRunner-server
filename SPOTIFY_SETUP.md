# Spotify Developer Dashboard Setup Guide

To resolve the **403 Forbidden** error ("User not registered in the Developer Dashboard"), you need to configure your Spotify App in the Developer Dashboard.

## 1. Log in to the Dashboard
Go to [developer.spotify.com/dashboard](https://developer.spotify.com/dashboard) and log in with your Spotify account.

## 2. Select Your App
Click on the application you created for **BeatsRunner** (the one with Client ID `470833c0fbd64c03b44c23fa7a532ee4`).

## 3. Add User (Crucial Step!)
Since your app is likely in **Development Mode**, you must explicitly add users who are allowed to access it.

1.  Click on the **Settings** button (often a gear icon or a "Settings" tab).
2.  Navigate to the **Users and Access** section.
3.  Click **Add New User**.
4.  Enter the **name** and **email address** associated with your Spotify account.
5.  Click **Add**.

**Note:** You must add the email address of the account you are using to test (or the one associated with the credentials if it's the same).

## 4. Verify App Status
Check if your app is in "Development Mode". If you want it to be accessible to anyone without adding them manually, you would need to apply for "Quota Extension" to move to "Production Mode", but for development/testing, adding your user is sufficient.

## 5. Retry
Once you have added your user, wait a minute and then try running the server or the test script again.
