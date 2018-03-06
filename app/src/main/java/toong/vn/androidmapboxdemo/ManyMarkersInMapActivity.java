package toong.vn.androidmapboxdemo;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import toong.vn.androidmapboxdemo.model.TextMarkerView;
import toong.vn.androidmapboxdemo.model.TextMarkerViewOptions;

public class ManyMarkersInMapActivity extends AppCompatActivity {
    String Mapbox_Key =
            "pk.eyJ1IjoicGhhbnZhbmxpbmg5NHZuIiwiYSI6ImNqMW44ZmtlbDAwcjYyd28yaDQzbzJwejAifQ.v9ID5IxcItXpaw72ZVN4dA";
    MapView mapView;

    float lat = 16.07f;
    float lng = 108.217655f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, Mapbox_Key);

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_many_markers_in_map);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                mapReady(mapboxMap);

                mapboxMap.setMinZoomPreference(14);
                mapboxMap.setMaxZoomPreference(18);
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16));

//                mapboxMap.addOnCameraIdleListener(new MapboxMap.OnCameraIdleListener() {
//                    @Override
//                    public void onCameraIdle() {
//                        mapboxMap.getMarkerViewManager().invalidateViewMarkersInVisibleRegion();
//                    }
//                });
            }
        });
    }

    private void mapReady(MapboxMap mapboxMap) {
        float plus = 0.001f;
        mapboxMap.getMarkerViewManager().addMarkerViewAdapter(new TextAdapter(this, mapboxMap));
        for (int i = 0; i < 25; i++) {
            add(mapboxMap, new LatLng(lat + plus, lng - plus));
            add(mapboxMap, new LatLng(lat - plus, lng + plus));
            add(mapboxMap, new LatLng(lat - plus, lng - plus));

            add(mapboxMap, new LatLng(lat + plus, lng));
            add(mapboxMap, new LatLng(lat - plus, lng));
            add(mapboxMap, new LatLng(lat, lng + plus));
            add(mapboxMap, new LatLng(lat, lng - plus));
            plus += 0.001;
        }
        mapboxMap.getMarkerViewManager().setWaitingForRenderInvoke(true);
    }

    private void add(MapboxMap mapboxMap, LatLng latLng) {
//        MarkerViewOptions markerViewOptions = new MarkerViewOptions().position(latLng)
//                .icon(IconFactory.getInstance(ManyMarkersInMapActivity.this)
//                        .fromResource(R.drawable.ic_maker_green));
//        mapboxMap.addMarker(markerViewOptions);

        mapboxMap.addMarker(
                new TextMarkerViewOptions().text("CD").position(latLng));
    }

    /**
     * Adapts a MarkerView to display text  in a TextView.
     */
    public static class TextAdapter extends MapboxMap.MarkerViewAdapter<TextMarkerView> {

        private LayoutInflater inflater;
        private MapboxMap mapboxMap;

        public TextAdapter(@NonNull Context context, @NonNull MapboxMap mapboxMap) {
            super(context);
            this.inflater = LayoutInflater.from(context);
            this.mapboxMap = mapboxMap;
        }

        @Nullable
        @Override
        public View getView(@NonNull TextMarkerView marker, @Nullable View convertView,
                @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.view_text_marker, parent, false);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textView.setText(marker.getText());
            return convertView;
        }

        @Override
        public boolean onSelect(@NonNull final TextMarkerView marker,
                @NonNull final View convertView, boolean reselectionForViewReuse) {
            animateGrow(marker, convertView, 0);

            // false indicates that we are calling selectMarker after our animation ourselves
            // true will let the system call it for you, which will result in showing an InfoWindow instantly
            return false;
        }

        @Override
        public void onDeselect(@NonNull TextMarkerView marker, @NonNull final View convertView) {
            animateShrink(convertView, 350);
        }

        @Override
        public boolean prepareViewForReuse(@NonNull MarkerView marker, @NonNull View convertView) {
            // this method is called before a view will be reused, we need to restore view state
            // as we have scaled the view in onSelect. If not correctly applied other MarkerView will
            // become large since these have been recycled

            // cancel ongoing animation
            convertView.animate().cancel();

            if (marker.isSelected()) {
                // shrink view to be able to be reused
                animateShrink(convertView, 0);
            }

            // true if you want reuse to occur automatically, false if you want to manage this yourself
            return true;
        }

        private void animateGrow(@NonNull final MarkerView marker, @NonNull final View convertView,
                int duration) {
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            Animator animator =
                    AnimatorInflater.loadAnimator(convertView.getContext(), R.animator.scale_up);
            animator.setDuration(duration);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    convertView.setLayerType(View.LAYER_TYPE_NONE, null);
                    mapboxMap.selectMarker(marker);
                }
            });
            animator.setTarget(convertView);
            animator.start();
        }

        private void animateShrink(@NonNull final View convertView, int duration) {
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            Animator animator =
                    AnimatorInflater.loadAnimator(convertView.getContext(), R.animator.scale_down);
            animator.setDuration(duration);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    convertView.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            });
            animator.setTarget(convertView);
            animator.start();
        }

        private static class ViewHolder {
            TextView textView;
        }
    }
}
