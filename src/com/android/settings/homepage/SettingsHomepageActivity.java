/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.homepage;

import static android.provider.Settings.ACTION_SETTINGS_EMBED_DEEP_LINK_ACTIVITY;
import static android.provider.Settings.EXTRA_SETTINGS_EMBEDDED_DEEP_LINK_HIGHLIGHT_MENU_KEY;
import static android.provider.Settings.EXTRA_SETTINGS_EMBEDDED_DEEP_LINK_INTENT_URI;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.settings.SettingsEnums;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.FeatureFlagUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.window.embedding.SplitRule;

import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsApplication;
import com.android.settings.activityembedding.ActivityEmbeddingRulesController;
import com.android.settings.activityembedding.ActivityEmbeddingUtils;
import com.android.settings.core.CategoryMixin;
import com.android.settings.core.FeatureFlags;
import com.android.settings.homepage.contextualcards.ContextualCardsFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.Utils;
import com.android.settingslib.core.lifecycle.HideNonSystemOverlayMixin;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import java.util.ArrayList;
import java.net.URISyntaxException;
import java.util.Set;

/** Settings homepage activity */
public class SettingsHomepageActivity extends FragmentActivity implements
        CategoryMixin.CategoryHandler {

    private static final String TAG = "SettingsHomepageActivity";

    // Additional extra of Settings#ACTION_SETTINGS_LARGE_SCREEN_DEEP_LINK.
    // Put true value to the intent when startActivity for a deep link intent from this Activity.
    public static final String EXTRA_IS_FROM_SETTINGS_HOMEPAGE = "is_from_settings_homepage";

    // Additional extra of Settings#ACTION_SETTINGS_LARGE_SCREEN_DEEP_LINK.
    // Set & get Uri of the Intent separately to prevent failure of Intent#ParseUri.
    public static final String EXTRA_SETTINGS_LARGE_SCREEN_DEEP_LINK_INTENT_DATA =
            "settings_large_screen_deep_link_intent_data";

    static final int DEFAULT_HIGHLIGHT_MENU_KEY = R.string.menu_key_network;
    private static final long HOMEPAGE_LOADING_TIMEOUT_MS = 300;

    private TopLevelSettings mMainFragment;
    private View mHomepageView;
    private CategoryMixin mCategoryMixin;
    private Set<HomepageLoadedListener> mLoadedListeners;
    private boolean mIsEmbeddingActivityEnabled;
    CollapsingToolbarLayout collapsing_toolbar;
    
    static ArrayList<String> text=new ArrayList<>();
    static {
        text.add("Arise, Young one.");
	    text.add("Welcome Stranger!");
	    text.add("One's soul shreds uniqueness.");
        text.add("Calm down young one, catch your breath.");
        text.add("Roquelaire, would you like a cracker?");
        text.add("My little friend, always busy-busy.");
        text.add("Ho ho! You found me!");
        text.add("Welcome to the Secret Shop!");
        text.add("Some tea while you wait?");
        text.add("Those go together nicely.");
        text.add("Your foes will fear you now.");
        text.add("My favorite customer!");
        text.add("Business is brisk.");
        text.add("How is your journey little one?");
        text.add("Mistakes are always part of one's life, youngster.");
        text.add("Have a lucky day human!");
        text.add("You can do it Stranger!");
	    text.add("It was never wrong to try, young one.");
	    text.add("The learned one strikes.");
	    text.add("They will never know what hit them.");
	    text.add("Turn the tables!");
	    text.add("The enemy will be destroyed, no matter the cost!");
	    text.add("A good strategist always keeps something in reserve.");
	    text.add("Never Settle?");
	    text.add("Gratitude unlocks the fullness of life, Milord.");
	    text.add("A joker is a little fool who is different from everyone else.");
	    text.add("Failure is not Fatal, Customer.");
	    text.add("Taking a rest is not a sin young man.");
	    text.add("What is truth, but a survivor's story?");
	    text.add("In a world without love, death means nothing.");
	    text.add("Always appreciate your own endeavors, Milord.");
	    text.add("Fear is the first of many foes.");
	    text.add("The climb may be long, but the view is worth it.");
	    text.add("The waves will drag you down, unless you fight to shore.");
	    text.add("The darker the night, the brighter the stars.");
	    text.add("Fight and be remembered, or die and be forgotten.");
	    text.add("In case no one asked, are you doing fine youngster?");
	    text.add("Nothing bears fruit from hatred, but disaster my friend.");
	    text.add("Another day to become a legend.");
	    text.add("In case no one told you this, you are awesome!");
	    text.add("My dear friend always busy, want some cookies?");
	    text.add("Never Forget the Arcanery.");
	    text.add("Show em what you got stranger!");
	    text.add("The Arcanery likes your presence.");
	    text.add("What shall the Arcanery grant unto you?");
	    text.add("Life is always full of mysteries.");
	    text.add("Find what you seek on Tresdins Lair.");
	    text.add("Seek and you shall find.");
	    text.add("Everyone is a survivor from the cruel reality.");
	    text.add("The Arcanery loves your efforts.");
	    text.add("Destiny awaits us all.");
	    text.add("From knowledge comes skill.");
	    text.add("What must be discovered?");
	    text.add("Even a master falters.");
    }
    
    static ArrayList<String> welcome=new ArrayList<>();
    static {
        welcome.add("Hi!");
        welcome.add("Hello.");
        welcome.add("Greetings.");
        welcome.add("Good Day!");
        welcome.add("Settings");
    }

    /** A listener receiving homepage loaded events. */
    public interface HomepageLoadedListener {
        /** Called when the homepage is loaded. */
        void onHomepageLoaded();
    }

    private interface FragmentBuilder<T extends Fragment>  {
        T build();
    }

    /**
     * Try to add a {@link HomepageLoadedListener}. If homepage is already loaded, the listener
     * will not be notified.
     *
     * @return Whether the listener is added.
     */
    public boolean addHomepageLoadedListener(HomepageLoadedListener listener) {
        if (mHomepageView == null) {
            return false;
        } else {
            if (!mLoadedListeners.contains(listener)) {
                mLoadedListeners.add(listener);
            }
            return true;
        }
    }

    /** Returns the main content fragment */
    public TopLevelSettings getMainFragment() {
        return mMainFragment;
    }

    @Override
    public CategoryMixin getCategoryMixin() {
        return mCategoryMixin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_homepage_container);
        mIsEmbeddingActivityEnabled = ActivityEmbeddingUtils.isEmbeddingActivityEnabled(this);

        updateHomepageBackground();
        mLoadedListeners = new ArraySet<>();

        final View root = findViewById(R.id.settings_homepage_container);
	LinearLayout commonCon = root.findViewById(R.id.common_con);
        final Toolbar toolbar = root.findViewById(R.id.search_action_bar);
	collapsing_toolbar =  root.findViewById(R.id.collapsing_toolbar);
	TextView greeter = root.findViewById(R.id.greeter);
	greeter.setText(text.get(randomNum(0, text.size()-1)));

        FeatureFactory.getFactory(this).getSearchFeatureProvider()
                .initSearchToolbar(this /* activity */, toolbar, SettingsEnums.SETTINGS_HOMEPAGE);
                
        AppBarLayout appBarLayout = root.findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, i) -> {
        	
        	float abs = ((float) Math.abs(i)) / ((float) appBarLayout1.getTotalScrollRange());
            float f2 = 1.0f - abs;
            //greeter text
            if (f2 == 1.0)
                ObjectAnimator.ofFloat(greeter, View.ALPHA, 1f).setDuration(500).start();
            else
                greeter.setAlpha(0f);
        });

        getLifecycle().addObserver(new HideNonSystemOverlayMixin(this));
        collapsing_toolbar.setTitle(welcome.get(randomNum(0, welcome.size()-1)));
        mCategoryMixin = new CategoryMixin(this);
        getLifecycle().addObserver(mCategoryMixin);

	final String highlightMenuKey = getHighlightMenuKey();
        mMainFragment = showFragment(() -> {
            final TopLevelSettings fragment = new TopLevelSettings();
            fragment.getArguments().putString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY,
                    highlightMenuKey);
            return fragment;
        }, R.id.main_content);

        ((FrameLayout) findViewById(R.id.main_content))
                .getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        // Launch the intent from deep link for large screen devices.
        launchDeepLinkIntentToRight();
    }

    @Override
    protected void onStart() {
        ((SettingsApplication) getApplication()).setHomeActivity(this);
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // When it's large screen 2-pane and Settings app is in the background, receiving an Intent
        // will not recreate this activity. Update the intent for this case.
        setIntent(intent);
        reloadHighlightMenuKey();
        if (isFinishing()) {
            return;
        }
        // Launch the intent from deep link for large screen devices.
        launchDeepLinkIntentToRight();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void updateHomepageBackground() {
        if (!mIsEmbeddingActivityEnabled) {
            return;
        }

        final Window window = getWindow();
        final int color = ActivityEmbeddingUtils.isTwoPaneResolution(this)
                ? Utils.getColorAttrDefaultColor(this, com.android.internal.R.attr.colorSurface)
                : Utils.getColorAttrDefaultColor(this, android.R.attr.colorBackground);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // Update status bar color
        window.setStatusBarColor(color);
        // Update content background.
        findViewById(R.id.settings_homepage_container).setBackgroundColor(color);
    }

    private <T extends Fragment> T showFragment(FragmentBuilder<T> fragmentBuilder, int id) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        T showFragment = (T) fragmentManager.findFragmentById(id);

        if (showFragment == null) {
            showFragment = fragmentBuilder.build();
            fragmentTransaction.add(id, showFragment);
        } else {
            fragmentTransaction.show(showFragment);
        }
        fragmentTransaction.commit();
        return showFragment;
    }

    private void launchDeepLinkIntentToRight() {
        if (!mIsEmbeddingActivityEnabled) {
            return;
        }

        final Intent intent = getIntent();
        if (intent == null || !TextUtils.equals(intent.getAction(),
                ACTION_SETTINGS_EMBED_DEEP_LINK_ACTIVITY)) {
            return;
        }

        if (!(this instanceof DeepLinkHomepageActivity
                || this instanceof SliceDeepLinkHomepageActivity)) {
            Log.e(TAG, "Not a deep link component");
            finish();
            return;
        }

        final String intentUriString = intent.getStringExtra(
                EXTRA_SETTINGS_EMBEDDED_DEEP_LINK_INTENT_URI);
        if (TextUtils.isEmpty(intentUriString)) {
            Log.e(TAG, "No EXTRA_SETTINGS_EMBEDDED_DEEP_LINK_INTENT_URI to deep link");
            finish();
            return;
        }

        final Intent targetIntent;
        try {
            targetIntent = Intent.parseUri(intentUriString, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Failed to parse deep link intent: " + e);
            finish();
            return;
        }

        final ComponentName targetComponentName = targetIntent.resolveActivity(getPackageManager());
        if (targetComponentName == null) {
            Log.e(TAG, "No valid target for the deep link intent: " + targetIntent);
            finish();
            return;
        }
        targetIntent.setComponent(targetComponentName);

        // To prevent launchDeepLinkIntentToRight again for configuration change.
        intent.setAction(null);

        targetIntent.setFlags(targetIntent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        // Sender of intent may want to send intent extra data to the destination of targetIntent.
        targetIntent.replaceExtras(intent);

        targetIntent.putExtra(EXTRA_IS_FROM_SETTINGS_HOMEPAGE, true);
        targetIntent.putExtra(SettingsActivity.EXTRA_IS_FROM_SLICE, false);

        targetIntent.setData(intent.getParcelableExtra(
                SettingsHomepageActivity.EXTRA_SETTINGS_LARGE_SCREEN_DEEP_LINK_INTENT_DATA));

        // Set 2-pane pair rule for the deep link page.
        ActivityEmbeddingRulesController.registerTwoPanePairRule(this,
                new ComponentName(getApplicationContext(), getClass()),
                targetComponentName,
                targetIntent.getAction(),
                SplitRule.FINISH_ALWAYS,
                SplitRule.FINISH_ALWAYS,
                true /* clearTop */);
        ActivityEmbeddingRulesController.registerTwoPanePairRule(this,
                new ComponentName(getApplicationContext(), Settings.class),
                targetComponentName,
                targetIntent.getAction(),
                SplitRule.FINISH_ALWAYS,
                SplitRule.FINISH_ALWAYS,
                true /* clearTop */);
        startActivity(targetIntent);
    }

    private String getHighlightMenuKey() {
        final Intent intent = getIntent();
        if (intent != null && TextUtils.equals(intent.getAction(),
                ACTION_SETTINGS_EMBED_DEEP_LINK_ACTIVITY)) {
            final String menuKey = intent.getStringExtra(
                    EXTRA_SETTINGS_EMBEDDED_DEEP_LINK_HIGHLIGHT_MENU_KEY);
            if (!TextUtils.isEmpty(menuKey)) {
                return menuKey;
            }
        }
        return getString(DEFAULT_HIGHLIGHT_MENU_KEY);
    }

    private void reloadHighlightMenuKey() {
        mMainFragment.getArguments().putString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY,
                getHighlightMenuKey());
        mMainFragment.reloadHighlightMenuKey();
    }

    private void initHomepageContainer() {
        final View view = findViewById(R.id.homepage_container);
        // Prevent inner RecyclerView gets focus and invokes scrolling.
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }
    
    private int randomNum(int min , int max) {
	int r = (max - min) + 1;
	return (int)(Math.random() * r) + min;
    }
}
