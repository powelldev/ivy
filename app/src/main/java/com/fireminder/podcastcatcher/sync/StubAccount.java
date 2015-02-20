package com.fireminder.podcastcatcher.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;

public class StubAccount {
    public static final String AUTHORITY = PodcastCatcherContract.CONTENT_AUTHORITY;
    public static final String ACCOUNT_TYPE = "com.fireminder";
    public static final String ACCOUNT = "dummyaccount";

    Account mAccount;

    StubAccount(Context context) {
        mAccount = CreateSyncAccount(context);
    }

    public static Account CreateSyncAccount(Context context) {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(newAccount, null, null);
        return newAccount;
    }

    public static Account getAccount(Context context) {
        return CreateSyncAccount(context);
    }
}
