package com.example.katerina.mapsex.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.katerina.mapsex.Game.GameProvider;
import com.example.katerina.mapsex.Game.GamesActivity;
import com.example.katerina.mapsex.LocationProvider;
import com.example.katerina.mapsex.R;
import com.example.katerina.mapsex.Rating.RatingActivity;
import com.example.katerina.mapsex.Repository;
import com.example.katerina.mapsex.Team.TeamsActivity;

import com.datamodel.datamodels.CheckIn;
import com.datamodel.datamodels.Game;
import com.datamodel.datamodels.Location;
import com.datamodel.datamodels.Param;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.*;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MapFragment
        extends
        com.google.android.gms.maps.MapFragment
        implements

        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapLongClickListener,
        OnClickListener {


    private LatLng temp;
    //интервал обновления положения всплывающего окна.
    //для плавности необходимо 60 fps, то есть 1000 ms / 60 = 16 ms между обновлениями.
    private static final int POPUP_POSITION_REFRESH_INTERVAL = 16;
    //длительность анимации перемещения карты
    private static final int ANIMATION_DURATION = 500;

    private Map<Marker, CheckIn> spots;
    private   GoogleMap map;
    //точка на карте, соответственно перемещению которой перемещается всплывающее окно
    private LatLng trackedPosition;

    //Handler, запускающий обновление окна с заданным интервалом
    private Handler handler;

    //Runnable, который обновляет положение окна
    private Runnable positionUpdaterRunnable;

    //смещения всплывающего окна, позволяющее
    //скорректировать его положение относительно маркера
    private int popupXOffset;
    private int popupYOffset;
    //высота маркера
    private int markerHeight;
    private AbsoluteLayout.LayoutParams overlayLayoutParams;

    //слушатель, который будет обновлять смещения
    private ViewTreeObserver.OnGlobalLayoutListener infoWindowLayoutListener;

    //контейнер всплывающего окна
    private View infoWindowContainer;
    private TextView textView;
    private ImageView myImageView;
    private Button delete;

    public MapFragment(){
        this.setRetainInstance(true);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spots = new HashMap<>();
        markerHeight = getResources().getDrawable(R.drawable.pin).getIntrinsicHeight();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_map_fragment, null);
        delete=(Button) rootView.findViewById(R.id.delete);

        FrameLayout containerMap = (FrameLayout) rootView.findViewById(R.id.container_map);
        View mapView = super.onCreateView(inflater, container, savedInstanceState);
        containerMap.addView(mapView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        map = getMap();

        //Установка маркера штаба и перемещение карты на его расположение
        markerInitializer();
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMapLongClickListener(this);

        ImageButton popup_men = (ImageButton) rootView.findViewById(R.id.button_popup);
        popup_men.setOnClickListener(new View.OnClickListener() {

            //Обрабатываем нажатие кнопки Button:
            @Override
            public void onClick(View view) {
                //Вызываем popup меню, заполняем его с файла popup.xml и настраиваем
                //слушатель нажатий по пунктам OnMenuItemClickListener:
                PopupMenu popup_menu = new PopupMenu(getActivity(), view);
                popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {


                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.game_menu:
                              //Toast.makeText(this, "Выбран пункт 1", Toast.LENGTH_SHORT).show();
                              startActivity(new Intent(getActivity(), GamesActivity.class));
                              return true;
                        case R.id.map_menu:
                            //Toast.makeText(this, "Выбран пункт 3", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), DemoActivity.class));
                            return true;
                        case R.id.teams_menu:
                              //Toast.makeText(this, "Выбран пункт 2", Toast.LENGTH_SHORT).show();
                              startActivity(new Intent(getActivity(), TeamsActivity.class));
                              return true;
                        case R.id.rating_menu:
                              startActivity(new Intent(getActivity(), RatingActivity.class));
                              return true;
                      }
                    return true;
                  }
              });

                    popup_menu.inflate(R.menu.popup_menu);

                    popup_menu.show();
                }
            });

    infoWindowContainer = rootView.findViewById(R.id.container_popup);
    //подписываемся на изменения размеров всплывающего окна
    infoWindowLayoutListener = new InfoWindowLayoutListener();
    infoWindowContainer.getViewTreeObserver().addOnGlobalLayoutListener(infoWindowLayoutListener);
    overlayLayoutParams = (AbsoluteLayout.LayoutParams) infoWindowContainer.getLayoutParams();

    textView = (TextView) infoWindowContainer.findViewById(R.id.textview_title);
    myImageView = (ImageView)infoWindowContainer.findViewById(R.id.imageView);

    return rootView;
}

    //Обрабатываем нажатия по пунктам popup меню, ссылаясь на id каждого пункта, заданные в файле popup.xml:

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //очистка
        handler = new Handler(Looper.getMainLooper());
        positionUpdaterRunnable = new PositionUpdaterRunnable();
        handler.post(positionUpdaterRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        infoWindowContainer.getViewTreeObserver().removeGlobalOnLayoutListener(infoWindowLayoutListener);
        handler.removeCallbacks(positionUpdaterRunnable);
        handler = null;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        infoWindowContainer.setVisibility(INVISIBLE);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        final Marker temp=marker;
        Projection projection = map.getProjection();
        trackedPosition = marker.getPosition();
        Point trackedPoint = projection.toScreenLocation(trackedPosition);
        trackedPoint.y -= popupYOffset / 2;
        LatLng newCameraLocation = projection.fromScreenLocation(trackedPoint);
        map.animateCamera(CameraUpdateFactory.newLatLng(newCameraLocation), ANIMATION_DURATION, null);

        if (!spots.containsKey(marker)) { infoWindowContainer.setVisibility(INVISIBLE); return false;}

            CheckIn spot = spots.get(marker);
            ArrayList<Param> garbage =spot.getGarb_param();
            StringBuffer buffer=new StringBuffer();
            buffer.append(spot.getComment()+"\n");
            int AllGarbage=0;
            for (int i=0;i<garbage.size();i++){
                buffer.append(garbage.get(i).getName()+": "+garbage.get(i).getAmount()+"\n");
                AllGarbage+=garbage.get(i).getAmount();
            }
            buffer.append("Общее кол-во:"+AllGarbage);
            textView.setText(buffer);
        if (spot.photo != null) {
            myImageView.setImageBitmap(spot.getPhoto());
        } else
            myImageView.setImageResource(R.drawable.garbage_empty_photo);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    spots.remove(temp);
                    temp.remove();
                    infoWindowContainer.setVisibility(INVISIBLE);

                }
            });

            infoWindowContainer.setVisibility(VISIBLE);

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                
                LocationProvider locationProvider= LocationProvider.Initialize();
                CheckIn checkIn=locationProvider.getCheckin();
                LatLng locationTemp=checkIn.getLocation();
                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(checkIn.getLocation());
                markerOptions.title(locationTemp.latitude + " : " + locationTemp.longitude);
                map.animateCamera(CameraUpdateFactory.newLatLng(locationTemp));
                Projection projection = map.getProjection();
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.pin);
                Marker m =   map.addMarker(markerOptions.icon(icon));
                spots.put(m,checkIn);
            }
            if (resultCode == 1) {
                //Write your code if there's no result
            }

        }

    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        temp=latLng;
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        Projection projection = map.getProjection();
        new AlertDialog.Builder(this.getActivity()) .setTitle("Check-in?")
                .setMessage("Do you want to check-in your location?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle a positive answer
                        LocationProvider locationProvider=LocationProvider.Initialize(temp,true);
                        startActivityForResult(new Intent(getActivity(), CheckInInfoActivity.class), 1);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle a negative answer
                    }
                })
                .setIcon(R.drawable.check_in)
                .show();
    }



    private class InfoWindowLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            //размеры окна изменились, обновляем смещения
            popupXOffset = infoWindowContainer.getWidth() / 2;
            popupYOffset = infoWindowContainer.getHeight();
        }
    }

    private class PositionUpdaterRunnable implements Runnable {
        private int lastXPosition = Integer.MIN_VALUE;
        private int lastYPosition = Integer.MIN_VALUE;

        @Override
        public void run() {
            //помещаем в очередь следующий цикл обновления
            handler.postDelayed(this, POPUP_POSITION_REFRESH_INTERVAL);

            //если всплывающее окно скрыто, ничего не делаем
            if (trackedPosition != null && infoWindowContainer.getVisibility() == VISIBLE) {
                Point targetPosition = getMap().getProjection().toScreenLocation(trackedPosition);

                //если положение окна не изменилось, ничего не делаем
                if (lastXPosition != targetPosition.x || lastYPosition != targetPosition.y) {
                    //обновляем положение
                    overlayLayoutParams.x = targetPosition.x - popupXOffset;
                    overlayLayoutParams.y = targetPosition.y - popupYOffset - markerHeight -30;
                    infoWindowContainer.setLayoutParams(overlayLayoutParams);

                    //запоминаем текущие координаты
                    lastXPosition = targetPosition.x;
                    lastYPosition = targetPosition.y;
                }
            }
        }
    }

    private void markerInitializer(){
        GameProvider provider= GameProvider.Initialize(new Game());
        Game game = provider.getGame();
        ArrayList<CheckIn> checkIns = Repository.getCheckins();
        ArrayList<Location> locations=game.base_loc;
        for (int i=0;i<locations.size();i++) {
            BitmapDescriptor icon=null;
            Location location=locations.get(i);
            LatLng point=location.getLoc();
             switch(location.getRole()){
                 case BASE: {
                     map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 5.5f));
                     icon = BitmapDescriptorFactory.fromResource(R.drawable.base);
                     break;
                 }
                 case WAREHOUSE:{
                     icon = BitmapDescriptorFactory.fromResource(R.drawable.warehouse);
                     break;
                 }
             }
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(point);
            markerOptions.title(point.latitude + " : " + point.longitude);
            Marker m = map.addMarker(markerOptions.icon(icon));

        }
        for (int i=0; i<checkIns.size();i++){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.pin);
            LatLng point=checkIns.get(i).getLocation();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(point);
            Marker m = map.addMarker(markerOptions.icon(icon));
            if (!spots.containsKey(m)){spots.put(m,checkIns.get(i));}
        }
    }

}
