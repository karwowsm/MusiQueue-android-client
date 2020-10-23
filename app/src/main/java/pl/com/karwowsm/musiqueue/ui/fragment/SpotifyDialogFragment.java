package pl.com.karwowsm.musiqueue.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyContent;

public class SpotifyDialogFragment extends DialogFragment {

    private ViewPager viewPager;

    @Override
    public void setArguments(Bundle args) {
        Listener listener = (Listener) args.get(Listener.BUNDLE_KEY);
        SpotifyContentFragment.setListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_fragment_spotify, container, false);
        TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        viewPager = rootView.findViewById(R.id.view_pager);
        final Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(getString(R.string.tracks), SpotifyContentFragment.createInstance(SpotifyContentFragment.Type.TRACKS));
        adapter.addFragment(getString(R.string.artists), SpotifyContentFragment.createInstance(SpotifyContentFragment.Type.ARTISTS));
        adapter.addFragment(getString(R.string.playlists), SpotifyContentFragment.createInstance(SpotifyContentFragment.Type.PLAYLISTS));
        viewPager.setAdapter(adapter);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.spotify);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            SpotifyContentFragment currentFragment = getCurrentFragment();
            if (currentFragment.getContentType() == SpotifyContentFragment.Type.ARTIST_AND_ALBUMS) {
                currentFragment.changeContentType(SpotifyContentFragment.Type.ARTISTS);
            } else {
                dismiss();
            }
        });
        tabLayout.setupWithViewPager(viewPager);
        return rootView;
    }

    private SpotifyContentFragment getCurrentFragment() {
        return ((SpotifyContentFragment) ((Adapter) viewPager.getAdapter())
            .getItem(viewPager.getCurrentItem()));
    }

    private static class Adapter extends FragmentPagerAdapter {

        final List<Fragment> fragmentCollection = new ArrayList<>();
        final List<String> titleCollection = new ArrayList<>();

        Adapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        void addFragment(String title, Fragment fragment) {
            titleCollection.add(title);
            fragmentCollection.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleCollection.get(position);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentCollection.get(position);
        }

        @Override
        public int getCount() {
            return fragmentCollection.size();
        }
    }

    public interface Listener extends Serializable {

        String BUNDLE_KEY = "SPOTIFY_DIALOG_LISTENER";

        void onFinishSpotifyDialog(SpotifyContent spotifyContent);
    }
}
