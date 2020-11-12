package pl.com.karwowsm.musiqueue.ui.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import pl.com.karwowsm.musiqueue.R;

abstract class AbstractActivity extends AppCompatActivity {

    private int STATUS_BAR_COLOR = R.color.colorPrimary;
    private int NAVIGATION_BAR_COLOR = R.color.colorPrimaryDark;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    void showProgressDialog(@StringRes int id) {
        pDialog.setMessage(getString(id));
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
    }

    void hideProgressDialog() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    void showToast(@StringRes int id) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show();
    }

    void showToast(@StringRes int id, Object... formatArgs) {
        showToast(getString(id, formatArgs));
    }

    void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    void setBarsColor(int barsColor) {
        setBarsColors(barsColor, barsColor);
    }

    void setBarsColors(int statusBarColor, int navigationBarColor) {
        STATUS_BAR_COLOR = statusBarColor;
        NAVIGATION_BAR_COLOR = navigationBarColor;
        setBarsColors();
    }

    void setBarsColors() {
        getWindow().setStatusBarColor(getResources().getColor(STATUS_BAR_COLOR));
        getWindow().setNavigationBarColor(getResources().getColor(NAVIGATION_BAR_COLOR));
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean requestPermissionsIfNeeded(int requestCode, String... permissions) {
        String[] notGrantedPermissions = Arrays.stream(permissions)
            .filter(permission -> checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
            .toArray(String[]::new);
        if (notGrantedPermissions.length > 0) {
            requestPermissions(notGrantedPermissions, requestCode);
            return true;
        }
        return false;
    }

    interface OnListViewScrollEnd extends AbsListView.OnScrollListener {

        void onScrollEnd();

        default void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !view.canScrollVertically(1)) {
                onScrollEnd();
            }
        }

        default void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    }
}
