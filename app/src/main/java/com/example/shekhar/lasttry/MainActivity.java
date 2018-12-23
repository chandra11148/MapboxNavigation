package com.example.shekhar.lasttry;


import android.graphics.Point;
import android.location.Location;
import android.location.LocationProvider;
import android.nfc.Tag;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener,PermissionsListener,MapboxMap.OnMapClickListener {
     private MapView mapView;
     private MapboxMap map;
     private PermissionsManager permissionsManager;
     private LocationEngine locationEngine;
     private LocationLayerPlugin locationLayerPlugin;
     private Location originLocation;
     private com.mapbox.geojson.Point originPosition;
     private com.mapbox.geojson.Point destinationPosition;
     private Marker destinationMarker;
     private Button startButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiZ29sZGJ1cmcxIiwiYSI6ImNqbDNqNmsxbDIxejgzbHFwaXJyeXlrYTAifQ.XwGhMXz0Muvfj2eJUUN5hg");

        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        startButton=findViewById(R.id.button);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
    map=mapboxMap;
    map.addOnMapClickListener(this);
    enabledLocation();
    }

    private void enabledLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
         initializelocationEngine();
         initializeLocationLayer();
        }else {
            permissionsManager=new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    @SuppressWarnings("MissingPermission")
    private void initializelocationEngine(){
        locationEngine=new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation=locationEngine.getLastLocation();

        if (lastLocation!=null){
            originLocation=lastLocation;
            setCameraPosition(lastLocation);

        }else {
            locationEngine.addLocationEngineListener(this);
        }
    }
    private void initializeLocationLayer(){
        locationLayerPlugin=new LocationLayerPlugin(mapView,map,locationEngine);
        locationLayerPlugin.setRenderMode(RenderMode.COMPASS);
    }
    private void setCameraPosition(Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),13.0));
    }
    @SuppressWarnings("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        if (locationEngine!=null){
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin!=null){
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
     @SuppressWarnings("MissingPermission")
    @Override
    public void onStop() {
        super.onStop();
        if (locationEngine!=null){
            locationEngine.removeLocationUpdates();
        }
        if(locationLayerPlugin!=null){
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine!=null){
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
     @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected() {
     locationEngine.requestLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
     if(location!=null){
         originLocation=location;
         setCameraPosition(location);
         locationEngine.removeLocationEngineListener(this);
     }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
     //denies excess first time than it toast dialogue
    }

    @Override
    public void onPermissionResult(boolean granted) {
     if(granted){
         enabledLocation();
     }else {
         finish();
     }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        if (destinationMarker!=null){
            map.removeMarker(destinationMarker);
        }
         destinationMarker=map.addMarker(new MarkerOptions().position(point));
         destinationPosition= com.mapbox.geojson.Point.fromLngLat(point.getLongitude(),point.getLatitude());
         originPosition= com.mapbox.geojson.Point.fromLngLat(originLocation.getLongitude(),originLocation.getLatitude());
         startButton.setEnabled(true);
         startButton.setBackgroundResource(R.color.mapbox_blue);
    }
}
