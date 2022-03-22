package app.touchlessChef.activity.standard.home;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import androidx.annotation.DrawableRes;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import app.touchlessChef.R;
import app.touchlessChef.adapter.DatabaseAdapter;
import app.touchlessChef.adapter.pager.MainPagerAdapter;
import app.touchlessChef.model.Recipe;
import app.touchlessChef.fragment.home.BaseFragment;
import app.touchlessChef.activity.standard.recipe.CreateRecipeActivity;
import app.touchlessChef.activity.standard.recipe.ViewRecipeActivity;
import app.touchlessChef.RecipeValues;

public class MainActivity extends MenuActivity implements BaseFragment.FragmentListener {
    private static final int REQUEST_ADD_RECIPE = 1;
    private static final int REQUEST_VIEW_RECIPE = 2;
    private MainPagerAdapter mAdapter;
    private DatabaseAdapter databaseAdapter;

    private ViewPager mViewPager;
    private ImageView firstView;
    private ImageView secondView;
    private ViewSwitcher mViewSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_home_main);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        databaseAdapter = DatabaseAdapter.getInstance(this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("touchlessChef");

        mViewPager = findViewById(R.id.viewpager);
        TabLayout myTabLayout = findViewById(R.id.tablayout);
        CollapsingToolbarLayout myCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        firstView = findViewById(R.id.firstView);
        secondView = findViewById(R.id.secondView);
        mViewSwitcher = findViewById(R.id.switcher);
        myTabLayout.bringToFront();
        mAdapter = new MainPagerAdapter(getSupportFragmentManager());

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        myCollapsingToolbarLayout.setCollapsedTitleTypeface(font);
        myCollapsingToolbarLayout.setExpandedTitleTypeface(font);

        mViewPager.setAdapter(mAdapter);
        myTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(myTabLayout));
        myTabLayout.setTabsFromPagerAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                @DrawableRes int image = -1;
                switch (position) {
                    case 0:
                        image = R.drawable.vn_botloc;
                        break;
                    case 1:
                        image = R.drawable.chn_dumpling;
                        break;
                }

                if (firstView.getVisibility() == View.VISIBLE) {
                    secondView.setImageResource(image);
                    mViewSwitcher.showNext();
                } else {
                    firstView.setImageResource(image);
                    mViewSwitcher.showPrevious();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_recipe:
                Intent intent = new Intent(this, CreateRecipeActivity.class);
                intent.putExtra("category", getCurrentlyDisplayedCategory());
                startActivityForResult(intent, REQUEST_ADD_RECIPE);
                break;
//            case R.id.sign_out:
//                UserPreferences.clear(this);
//                navigateToLogin();
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ADD_RECIPE:
                switch (resultCode) {
                    case RecipeValues.RECIPE_ADDED:
                        Snackbar.make(getWindow().getDecorView(), "Recipe added.", Snackbar.LENGTH_LONG)
                                .show();
                        break;
                    case RecipeValues.RECIPE_EDITED:
                        Snackbar.make(getWindow().getDecorView(), "Recipe modified.", Snackbar.LENGTH_LONG)
                                .show();
                        break;
                }
                break;
            case REQUEST_VIEW_RECIPE:
                switch (resultCode) {
                    case RecipeValues.RECIPE_SHOULD_BE_EDITED:
                        Recipe recipe = data.getParcelableExtra("recipe");
                        Intent intent = new Intent(this, CreateRecipeActivity.class);
                        intent.putExtra("recipe", recipe);
                        intent.putExtra("category", recipe.getCategory());
                        intent.putExtra("isUpdating", true);
                        startActivityForResult(intent, REQUEST_ADD_RECIPE);
                        break;
                    case RecipeValues.RECIPE_SHOULD_BE_DELETED:
                        long recipeId = data.getLongExtra("recipeId", -1);
                        if (recipeId != -1) {
                            onDeleteRecipe(recipeId);
                            mViewPager.getAdapter().notifyDataSetChanged();
                        }
                        break;
                }
                break;
        }
    }

    private String getCurrentlyDisplayedCategory() {
        return mAdapter.getPageTitle(mViewPager.getCurrentItem()).toString();
    }

//    private void navigateToLogin() {
//        Intent startIntent = new Intent(this, LoginActivity.class);
//        startActivity(startIntent);
//        finish();
//    }

    @Override
    public void onShowRecipe(Recipe recipe, Pair<View, String>[] pairs) {
        Intent intent = new Intent(this, ViewRecipeActivity.class);
        intent.putExtra("recipe", recipe);

        startActivityForResultWithSharedElement(
                this, intent, pairs[0].first, pairs[0].second, REQUEST_VIEW_RECIPE);
    }

    @Override
    public void onEditRecipe(Recipe recipe) {
        Intent intent = new Intent(this, CreateRecipeActivity.class);
        intent.putExtra("recipe", recipe);
        intent.putExtra("category", getCurrentlyDisplayedCategory());
        intent.putExtra("isUpdating", true);
        startActivityForResult(intent, REQUEST_ADD_RECIPE);
    }

    @Override
    public void onDeleteRecipe(long recipeId) {
        databaseAdapter.deleteRecipe(recipeId);
        Snackbar.make(getWindow().getDecorView(), "Recipe deleted.", Snackbar.LENGTH_LONG).show();
    }

    public static void startActivityForResultWithSharedElement(Context context,
                                                               Intent intent,
                                                               View sharedView,
                                                               String transitionName,
                                                               int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            ((Activity) context).startActivityForResult(intent, requestCode);
        else {
            ActivityOptions transitionActivityOptions =
                    ActivityOptions.makeSceneTransitionAnimation(
                            (Activity) context, sharedView, transitionName);
            ((Activity) context).startActivityForResult(
                    intent, requestCode, transitionActivityOptions.toBundle());
        }
    }
}