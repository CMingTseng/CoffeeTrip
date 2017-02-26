package tw.com.louis383.coffeefinder.maps;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import javax.inject.Inject;

import tw.com.louis383.coffeefinder.CoffeeTripApplication;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.mainpage.MainActivity;
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager;
import tw.com.louis383.coffeefinder.utils.ChromeCustomTabsHelper;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;
import tw.com.louis383.coffeefinder.widget.CoffeeDetailDialog;

public class MapsFragment extends Fragment implements OnMapReadyCallback, MapsPresenter.MapView, CoffeeDetailDialog.Callback, View.OnClickListener {

    public static final float ZOOM_RATE = 16f;

    private GoogleMap googleMap;
    private MapView mapView;
    private MapsPresenter presenter;

    private FrameLayout rootView;
    private Snackbar snackbar;
    private CoffeeDetailDialog detailDialog;
    private FloatingActionButton myLocationButton;

    private boolean mapInterfaceInitiated;

    public MapsFragment() {}

    public static MapsFragment newInstance() {

        Bundle args = new Bundle();

        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = (FrameLayout) view.findViewById(R.id.map_rootview);
        mapView = (MapView) view.findViewById(R.id.map_view);
        myLocationButton = (FloatingActionButton) view.findViewById(R.id.my_location_button);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ((CoffeeTripApplication) getActivity().getApplication()).getAppComponent().inject(this);

        presenter.attachView(this);
        myLocationButton.setOnClickListener(this);
    }

    @Inject
    public void initPresenter(CoffeeShopListManager coffeeShopListManager) {
        presenter = new MapsPresenter(coffeeShopListManager);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        presenter.setGoogleMap(googleMap);
    }

    @Override
    public void addMakers(LatLng latLng, String title, String snippet, String id, BitmapDescriptor icon) {
        String distance = getResources().getString(R.string.unit_m, snippet);
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.title(title);
        options.snippet(distance);
        options.icon(icon);

        Marker marker = googleMap.addMarker(options);
        marker.setTag(id);
    }

    @Override
    public void moveCamera(LatLng latLng, Float zoom) {
        if (!mapInterfaceInitiated) {
            // First time initiate interface
            presenter.fetchCoffeeShops();
            setupDetailedMapInterface();

            mapInterfaceInitiated = true;
        }

        CameraUpdate cameraUpdate;
        if (zoom != null) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        }
        googleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void openWebsite(Uri uri) {
        CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabIntent = customTabBuilder.build();

        ChromeCustomTabsHelper.openCustomTab(getActivity(), customTabIntent, uri, (activity, uri1) -> {
            // TODO:: a webview page to open a link.
            Intent intent = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(intent);
        });
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void setupDetailedMapInterface() {
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);

        UiSettings mapUISettings = googleMap.getUiSettings();
        mapUISettings.setRotateGesturesEnabled(false);
        mapUISettings.setTiltGesturesEnabled(false);
        mapUISettings.setMapToolbarEnabled(false);
        mapUISettings.setMyLocationButtonEnabled(false);
    }

    @Override
    public void showNeedsGoogleMapMessage() {
        String message = getResources().getString(R.string.googlemap_not_install);
        makeCustomSnackbar(message, false);
    }

    @Override
    public void makeCustomSnackbar(String message, boolean infinity) {
        int duration = infinity ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG;
        snackbar = Snackbar.make(rootView, message, duration);
        snackbar.show();
    }

    @Override
    public void showNoCoffeeShopDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(getResources().getString(R.string.dialog_no_coffeeshop_title));
        alertDialogBuilder.setMessage(getResources().getString(R.string.dialog_no_coffeeshop_message));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.dialog_no_coffeeshop_ok), (dialog, which) -> {});
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    @Override
    public void openCoffeeDetailDialog(CoffeeShopViewModel viewModel) {
        if (detailDialog == null) {
            detailDialog = new CoffeeDetailDialog(getContext(), viewModel, this);
        } else if (detailDialog.isShowing()) {
            detailDialog.dismiss();
        } else {
            detailDialog.setupCoffeeShop(viewModel);
        }

        detailDialog.show();
    }

    @Override
    public void cleanMap() {
        if (googleMap != null) {
            googleMap.clear();
        }
    }

    @Override
    public void navigateToLocation(Intent intent) {
        startActivity(intent);
    }

    @Override
    public boolean isGoogleMapInstalled(String packageName) {
        PackageManager packageManager = getActivity().getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public Location getCurrentLocation() {
        return ((MainActivity) getActivity()).getCurrentLocation();
    }

    @Override
    public Drawable getResourceDrawable(int resId) {
        return ContextCompat.getDrawable(getContext(), resId);
    }

    //region CoffeeDetailDialog Callback
    @Override
    public void onNavigationTextClicked(CoffeeShopViewModel viewModel) {
        presenter.prepareNavigation();
    }

    @Override
    public void onOpenWebsiteButtonClicked(CoffeeShopViewModel viewModel) {
        openWebsite(viewModel.getDetailUri());
    }
    //endregion

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_location_button:
                Location currentLocation = getCurrentLocation();
                if (currentLocation != null) {
                    LatLng lastLatlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    moveCamera(lastLatlng, null);
                }
                break;
        }
    }
}