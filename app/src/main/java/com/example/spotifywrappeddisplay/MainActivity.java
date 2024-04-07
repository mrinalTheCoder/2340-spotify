package com.example.spotifywrappeddisplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class MainActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    ArrayList<String> topArtists = new ArrayList<>(Arrays.asList(new String[]{"Daniel Caesar", "Frank Ocean", "Brent Faiyaz", "Drake", "Kanye West"}));
    ArrayList<String> topSongs = new ArrayList<>(Arrays.asList(new String[]{"drive ME crazy!", "Antidote", "I'm a Firefighter", "Novacane", "Pink + White"}));
    ArrayList<String> genre = new ArrayList<>(Arrays.asList(new String[]{"contemporary", "lgbtq+ hip hop", "neo soul", "r&b", "rap", "canadian hip hop"}));
    ArrayList<Double> audioFeatures = new ArrayList<>(Arrays.asList(new Double[]{0.5689999999999999, 0.31728, 0.5892, 0.2512, 0.394599999999999951}));
    ArrayList<String> recArtists = new ArrayList<>(Arrays.asList(new String[]{"Mac Miller", "dandelion hands", "Cigs After Sex", "The Alchemist", "BROCKHAMPTON", "Brent Faiyaz", "Jesse", "Frank Ocean"}));
    private static final int PROGRESS_COUNT = 6;

    private StoriesProgressView storiesProgressView;
    private ImageView image;

    private int counter = 0;

    private ArrayList<Bitmap> bitmapresources = new ArrayList<>();

    private final long[] durations = new long[]{
            500L, 1000L, 1500L, 4000L, 5000L, 1000,
    };

    long pressTime = 0L;
    long limit = 500L;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    private static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view1 = getLayoutInflater().inflate(R.layout.welcome_wrapped, null);
        Bitmap bitmap1 = getBitmapFromView(view1);
        bitmapresources.add(bitmap1);

        View view2 = getLayoutInflater().inflate(R.layout.topartists_wrapped, null);
        ((TextView) view2.findViewById(R.id.artist1)).setText(topArtists.get(0));
        ((TextView) view2.findViewById(R.id.artist2)).setText(topArtists.get(1));
        ((TextView) view2.findViewById(R.id.artist3)).setText(topArtists.get(2));
        ((TextView) view2.findViewById(R.id.artist4)).setText(topArtists.get(3));
        ((TextView) view2.findViewById(R.id.artist5)).setText(topArtists.get(4));
        Bitmap bitmap2 = getBitmapFromView(view2);
        bitmapresources.add(bitmap2);

        View view3 = getLayoutInflater().inflate(R.layout.topsongs_wrapped, null);
        ((TextView) view3.findViewById(R.id.song1)).setText(topSongs.get(0));
        ((TextView) view3.findViewById(R.id.song2)).setText(topSongs.get(1));
        ((TextView) view3.findViewById(R.id.song3)).setText(topSongs.get(2));
        ((TextView) view3.findViewById(R.id.song4)).setText(topSongs.get(3));
        ((TextView) view3.findViewById(R.id.song5)).setText(topSongs.get(4));
        Bitmap bitmap3 = getBitmapFromView(view3);
        bitmapresources.add(bitmap3);

        View view4 = getLayoutInflater().inflate(R.layout.topgenres_wrapped, null);
        ((TextView) view4.findViewById(R.id.genre1)).setText(genre.get(0));
        ((TextView) view4.findViewById(R.id.genre2)).setText(genre.get(1));
        ((TextView) view4.findViewById(R.id.genre3)).setText(genre.get(2));
        ((TextView) view4.findViewById(R.id.genre4)).setText(genre.get(3));
        ((TextView) view4.findViewById(R.id.genre5)).setText(genre.get(4));
        Bitmap bitmap4 = getBitmapFromView(view4);
        bitmapresources.add(bitmap4);

        View view5 = getLayoutInflater().inflate(R.layout.audiofeatures_wrapped, null);
        ProgressBar progressBar1 = (ProgressBar) view5.findViewById(R.id.progressBar18);
        ProgressBar progressBar2 = (ProgressBar) view5.findViewById(R.id.progressBar12);
        ProgressBar progressBar3 = (ProgressBar) view5.findViewById(R.id.progressBar13);
        ProgressBar progressBar4 = (ProgressBar) view5.findViewById(R.id.progressBar14);
        ProgressBar progressBar5 = (ProgressBar) view5.findViewById(R.id.progressBar16);
        progressBar1.setProgress((int) (100 * audioFeatures.get(0)));
        progressBar2.setProgress((int) (100 * audioFeatures.get(1)));
        progressBar3.setProgress((int) (100 * audioFeatures.get(2)));
        progressBar4.setProgress((int) (100 * audioFeatures.get(3)));
        progressBar5.setProgress((int) (100 * audioFeatures.get(4)));
        progressBar1.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar2.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar3.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar4.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar5.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        Bitmap bitmap5 = getBitmapFromView(view5);
        bitmapresources.add(bitmap5);

        View view6 = getLayoutInflater().inflate(R.layout.recartists_wrapped, null);
        ((TextView) view6.findViewById(R.id.recArtist1)).setText(recArtists.get(0));
        ((TextView) view6.findViewById(R.id.recArtist2)).setText(recArtists.get(1));
        ((TextView) view6.findViewById(R.id.recArtist3)).setText(recArtists.get(2));
        ((TextView) view6.findViewById(R.id.recArtist4)).setText(recArtists.get(3));
        ((TextView) view6.findViewById(R.id.recArtist5)).setText(recArtists.get(4));
        Bitmap bitmap6 = getBitmapFromView(view6);
        bitmapresources.add(bitmap6);

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        storiesProgressView = (StoriesProgressView) findViewById(R.id.stories);
        storiesProgressView.setStoriesCount(PROGRESS_COUNT);
        storiesProgressView.setStoryDuration(3000L);
        // or
        // storiesProgressView.setStoriesCountWithDurations(durations);
        storiesProgressView.setStoriesListener(this);
//        storiesProgressView.startStories();
        counter = 0;
        storiesProgressView.startStories(counter);

        image = (ImageView) findViewById(R.id.image);
        image.setImageBitmap(bitmapresources.get(counter));

        // bind reverse view
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onNext() {
        image.setImageBitmap(bitmapresources.get(++counter));;
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        image.setImageBitmap(bitmapresources.get(--counter));
    }

    @Override
    public void onComplete() {
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }
}