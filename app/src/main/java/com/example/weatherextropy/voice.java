import android.content.Context;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherextropy.R;

public class Voice extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Integer humidity;
    private Integer temperature;

    public Voice(Context context, int humidity, int temperature) {
        this.mediaPlayer = MediaPlayer.create(context, R.raw.rain_warning);
        this.humidity = humidity;
        this.temperature = temperature;
    }

    public void checkHumidityAndPlay() {
        if (humidity > 70) {
            mediaPlayer = MediaPlayer.create(mediaPlayer.getContext(), R.raw.rain_warning);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                }
            });
            mediaPlayer.start();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}