package toong.vn.androidmapboxdemo;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.stops.Stop;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, MapboxMap.OnMapClickListener {
    String Mapbox_Key =
            "pk.eyJ1IjoicGhhbnZhbmxpbmg5NHZuIiwiYSI6ImNqMW44ZmtlbDAwcjYyd28yaDQzbzJwejAifQ.v9ID5IxcItXpaw72ZVN4dA";

    private MapView mapView;
    private MapboxMap mapboxMap;
    private boolean markerSelected = false;

    private static final String SOURCE_ID = "mapbox.poi";
    private static final String MAKI_LAYER_ID = "mapbox.poi.maki";
    private static final String LOADING_LAYER_ID = "mapbox.poi.loading";
    private static final String CALLOUT_LAYER_ID = "mapbox.poi.callout";

    private static final String PROPERTY_LOADING = "loading";
    private static final String PROPERTY_LOADING_PROGRESS = "loading_progress";

    private static final float LOADING_CIRCLE_RADIUS = 60;
    private static final int LOADING_PROGRESS_STEPS = 25; //number of steps in a progress animation
    private static final int LOADING_STEP_DURATION = 50; //duration between each step

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, Mapbox_Key);

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setMinZoomPreference(14);
        mapboxMap.setMaxZoomPreference(18);
        //mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-71.065634, 42.354950)));

        List<Feature> markerCoordinates = new ArrayList<>();
        markerCoordinates.add(Feature.fromGeometry(
                Point.fromCoordinates(Position.fromCoordinates(-71.065634, 42.354950)))
                // Boston Common Park
        );
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(markerCoordinates);

        Source geoJsonSource = new GeoJsonSource("marker-source", featureCollection);
        Source geoJsonSource2 = new GeoJsonSource("marker-source2", featureCollection);
        Source geoJsonSource3 = new GeoJsonSource("marker-source3", featureCollection);

        mapboxMap.addSource(geoJsonSource);
        mapboxMap.addSource(geoJsonSource2);
        mapboxMap.addSource(geoJsonSource3);
        Bitmap icon = BitmapFactory.decodeResource(MainActivity.this.getResources(),
                R.drawable.blue_marker_view);
        Bitmap icon2 = BitmapFactory.decodeResource(MainActivity.this.getResources(),
                R.drawable.ic_maker_green);

        // Add the marker image to map
        mapboxMap.addImage("my-marker-image", icon);
        mapboxMap.addImage("my-marker-image2", icon2);

        SymbolLayer markers = new SymbolLayer("marker-layer", "marker-source").withProperties(
                PropertyFactory.iconImage("my-marker-image"));
        SymbolLayer markers2 = new SymbolLayer("marker-layer2", "marker-source2").withProperties(
                PropertyFactory.iconImage("my-marker-image2"));

        mapboxMap.addLayer(markers);
        mapboxMap.addLayerBelow(markers2, "marker-layer");

        setupLoadingLayer();
    }

    private void setupLoadingLayer() {
        final CircleLayer circleLayer = new CircleLayer("progress", "marker-source3").withProperties(
                circleColor(Color.RED), PropertyFactory.circleRadius(30f));
        ValueAnimator attractionsColorAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#ec8a8a"), // Brighter shade
                Color.parseColor("#de3232") // Darker shade
        );
        attractionsColorAnimator.setDuration(1000);
        attractionsColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        attractionsColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        attractionsColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                circleLayer.setProperties(
                        circleColor((int) animator.getAnimatedValue())
                );
            }

        });
        attractionsColorAnimator.start();
        mapboxMap.addLayerBelow(circleLayer, "marker-layer");
    }

    private Stop<Integer, Float>[] getLoadingAnimationStops() {
        List<Stop<Integer, Float>> stops = new ArrayList<>();
        for (int i = 10; i < LOADING_PROGRESS_STEPS; i++) {
            stops.add(stop(i, circleRadius(LOADING_CIRCLE_RADIUS * i / LOADING_PROGRESS_STEPS)));
        }

        return stops.toArray(new Stop[LOADING_PROGRESS_STEPS]);
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {

        //        final SymbolLayer marker = (SymbolLayer) mapboxMap.getLayer("selected-marker-layer");
        //
        //        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
        //        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer");
        //        List<Feature> selectedFeature = mapboxMap.queryRenderedFeatures(pixel, "selected-marker-layer");
        //
        //        if (selectedFeature.size() > 0 && markerSelected) {
        //            return;
        //        }
        //
        //        if (features.isEmpty()) {
        //            if (markerSelected) {
        //                deselectMarker(marker);
        //            }
        //            return;
        //        }
        //
        //        FeatureCollection featureCollection = FeatureCollection.fromFeatures(
        //                new Feature[]{Feature.fromGeometry(features.get(0).getGeometry())});
        //        GeoJsonSource source = mapboxMap.getSourceAs("selected-marker");
        //        if (source != null) {
        //            source.setGeoJson(featureCollection);
        //        }
        //
        //        if (markerSelected) {
        //            deselectMarker(marker);
        //        }
        //        if (features.size() > 0) {
        //            selectMarker(marker);
        //        }
    }

    private void addListener() {
        //        mapboxMap.addOnCameraMoveStartedListener(new MapboxMap.OnCameraMoveStartedListener() {
        //            @Override
        //            public void onCameraMoveStarted(int reason) {
        //                Log.i("TAG", "onCameraMoveStarted");
        //            }
        //        });
        //        mapboxMap.addOnCameraMoveCancelListener(new MapboxMap.OnCameraMoveCanceledListener() {
        //            @Override
        //            public void onCameraMoveCanceled() {
        //                Log.i("TAG", "onCameraMoveCanceled");
        //            }
        //        });
        //        mapboxMap.addOnCameraMoveListener(new MapboxMap.OnCameraMoveListener() {
        //            @Override
        //            public void onCameraMove() {
        //                Log.i("TAG", "onCameraMove");
        //            }
        //        });
        //        mapboxMap.addOnCameraIdleListener(new MapboxMap.OnCameraIdleListener() {
        //            @Override
        //            public void onCameraIdle() {
        //                Log.i("TAG", "onCameraIdle");
        //            }
        //        });
    }

    private void selectMarker(final SymbolLayer marker) {
        ValueAnimator markerAnimator = new ValueAnimator();
        markerAnimator.setObjectValues(1f, 2f);
        markerAnimator.setDuration(300);
        markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                marker.setProperties(PropertyFactory.iconSize((float) animator.getAnimatedValue()));
            }
        });
        markerAnimator.start();
        markerSelected = true;
    }

    private void deselectMarker(final SymbolLayer marker) {
        ValueAnimator markerAnimator = new ValueAnimator();
        markerAnimator.setObjectValues(2f, 1f);
        markerAnimator.setDuration(300);
        markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                marker.setProperties(PropertyFactory.iconSize((float) animator.getAnimatedValue()));
            }
        });
        markerAnimator.start();
        markerSelected = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
