package pl.com.karwowsm.musiqueue.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
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
    private Listener listener;

    @Override
    public void setArguments(Bundle args) {
        listener = (Listener) args.get(Listener.BUNDLE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_fragment_spotify, container, false);
        TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        viewPager = rootView.findViewById(R.id.view_pager);
        final Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(getString(R.string.tracks), new SpotifyContentFragment(SpotifyContentFragment.Type.TRACKS, listener));
        adapter.addFragment(getString(R.string.artists), new SpotifyContentFragment(SpotifyContentFragment.Type.ARTISTS, listener));
        adapter.addFragment(getString(R.string.playlists), new SpotifyContentFragment(SpotifyContentFragment.Type.PLAYLISTS, listener));
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
        return ((Adapter) viewPager.getAdapter())
            .getItem(viewPager.getCurrentItem());
    }

    private static class Adapter extends FragmentPagerAdapter {

        private final List<String> titles = new ArrayList<>();
        private final List<SpotifyContentFragment> fragments = new ArrayList<>();

        Adapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        void addFragment(String title, SpotifyContentFragment fragment) {
            titles.add(title);
            fragments.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @NonNull
        @Override
        public SpotifyContentFragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public interface Listener extends Serializable {

        String BUNDLE_KEY = "SPOTIFY_DIALOG_LISTENER";

        void onFinishSpotifyDialog(SpotifyContent spotifyContent);
    }
}
