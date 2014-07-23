package in.animeshpathak.android.chiffres;

/**
 * Dedicated to Anjali Sharan.
 * Thanks to http://www.androidhive.info/2012/01/android-text-to-speech-tutorial/
 * @author animesh@gmail.com
 */

import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements
		TextToSpeech.OnInitListener {
	private TextToSpeech tts;
	private ImageButton btnSpeak;
	private String number = "42", prevGuess, prevAnswer;
	private int attempts = 0;
	private EditText guessEditText;
	private TextView countView, longestStreakView, thisStreakView;
	private InputMethodManager inputManager;
	private TextView statusView;
	private int longestStreak;
	private int thisStreak = 0;
	private SharedPreferences settings;
	private Button buttonPlayAnswer, buttonPlayGuess;

	private final static String PREFS_NAME = "MyPrefsFile";
	private final static String KEY_NUMBER = "NUMBER";
	private final static String KEY_ATTEMPTS = "ATTEMPTS";
	private final static String KEY_L_STREAK = "L_STREAK";
	private final static String KEY_T_STREAK = "T_STREAK";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		settings = getSharedPreferences(PREFS_NAME, 0);

		guessEditText = (EditText) findViewById(R.id.text_guess);
		countView = (TextView) findViewById(R.id.text_count);
		statusView = (TextView) findViewById(R.id.text_status);
		statusView.setText(R.string.message_welcome);
		thisStreakView = (TextView) findViewById(R.id.text_this_streak);
		longestStreakView = (TextView) findViewById(R.id.text_longest_streak);
		
		inputManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		tts = new TextToSpeech(this, this);

		btnSpeak = (ImageButton) findViewById(R.id.button_play);
		btnSpeak.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				attempts++;
				countView.setText(String.valueOf(attempts));
				speakOut(number);
			}
		});

		
		buttonPlayAnswer = (Button) findViewById(R.id.button_answer_listen);
		buttonPlayAnswer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				speakOut(prevAnswer);
			}
		});
		
		buttonPlayGuess = (Button) findViewById(R.id.button_guess_listen);
		buttonPlayGuess.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				speakOut(prevGuess);
			}
		});
		
		Button guessButton = (Button) findViewById(R.id.button_guess);
		guessButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// hide keyboard
				inputManager.hideSoftInputFromWindow(v.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

				prevGuess = guessEditText.getText().toString();
				prevAnswer = number;
				String statusText;
				if (!number.equals(prevGuess)) {
					// say error
					statusText = "ERROR!\nValue: " + number + "; your guess: "
							+ prevGuess + "; repeats: " + attempts;
					// reset streak
					thisStreak = 0;
					speakOut("Ratée!");
				} else {
					statusText = "SUCCESS!\nValue: " + number + "; your guess: "
							+ prevGuess + "; repeats: " + attempts;
					// increment streak
					thisStreak++;
					// if streak more than current best, update best streak
					if (thisStreak > longestStreak) {
						longestStreak = thisStreak;
						longestStreakView.setText(String.valueOf(longestStreak));
					}
					speakOut("Très Bien");
				}
				buttonPlayAnswer.setText(prevAnswer);
				buttonPlayGuess.setText(prevGuess);
				statusView.setText(statusText);
				thisStreakView.setText(String.valueOf(thisStreak));
				Toast.makeText(MainActivity.this, statusText,
						Toast.LENGTH_SHORT).show();

				guessEditText.getText().clear();
				showValueAndRestart();

			}
		});

//		Button showButton = (Button) findViewById(R.id.button_show);
//		showButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				showValueAndRestart();
//			}
//		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Restore preferences
		longestStreak = settings.getInt(KEY_L_STREAK, 0);
		thisStreak = settings.getInt(KEY_T_STREAK, 0);
		longestStreakView.setText(String.valueOf(longestStreak));
		thisStreakView.setText(String.valueOf(thisStreak));
	}

	@Override
	protected void onPause() {
		super.onPause();
		// TODO
		// restore current number, attempts, max and current streaks, in
		// variables and in views, as needed.
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(KEY_L_STREAK, longestStreak);
		editor.putInt(KEY_T_STREAK, thisStreak);

		// Commit the edits!
		editor.commit();
	}

	protected void showValueAndRestart() {
		// show the real value
		((TextView) findViewById(R.id.text_number)).setText(number);
		// set new number
		number = String.valueOf((int) (Math.random() * 100000));
		attempts = 0;
		countView.setText(String.valueOf(attempts));
	}

	@Override
	public void onDestroy() {
		// Don't forget to shutdown tts!
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.FRANCE);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
				Toast.makeText(MainActivity.this,
						"This Language is not supported", Toast.LENGTH_SHORT)
						.show();
			} else {
				btnSpeak.setEnabled(true);
				// speakOut();
			}

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}

	private void speakOut(String toSpeak) {

		tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
	}
}
