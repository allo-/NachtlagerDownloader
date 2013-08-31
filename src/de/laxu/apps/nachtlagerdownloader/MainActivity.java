package de.laxu.apps.nachtlagerdownloader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
		datePicker.setCalendarViewShown(false);
		Calendar c = Calendar.getInstance();
		((DatePicker) findViewById(R.id.datePicker)).init(c.get(Calendar.YEAR),
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				new DatePicker.OnDateChangedListener() {

					@Override
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						dayChanged();
					}
				});
		((TextView) findViewById(R.id.errorMessage)).setText("");
		TextView thanksText = ((TextView) findViewById(R.id.thanksText));
		thanksText.setMovementMethod(LinkMovementMethod.getInstance());
		thanksText.setText(Html.fromHtml(getResources().getString(
				R.string.thanks)));
		dayChanged();
	}

	public void dayChanged() {
		DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
		Button downloadButton = (Button) findViewById(R.id.downloadButton);
		Date selected_date = new Date(datePicker.getCalendarView().getDate());
		Calendar c = Calendar.getInstance();
		
		c.setTime(selected_date); //set selected date
		c.set(Calendar.HOUR_OF_DAY, 2); // the earliest hour, the recording can be available
		
		c.add(Calendar.DAY_OF_YEAR, -1); // day before the selected day
		Date date_before = c.getTime();
		c.setTime(selected_date); //set back to selected date

		int day_of_week = c.get(Calendar.DAY_OF_WEEK);
		
		TextView textWeekday = (TextView) findViewById(R.id.textWeekday);
		textWeekday.setText(
			new SimpleDateFormat("EEEE", Locale.GERMANY).format(date_before) +
			" -> " +
			new SimpleDateFormat("EEEE", Locale.GERMANY).format(selected_date)
		);
		TextView errorMessage = (TextView) findViewById(R.id.errorMessage);
		Calendar today = Calendar.getInstance();
		if (day_of_week == Calendar.SUNDAY || day_of_week == Calendar.MONDAY) {
			errorMessage.setText("Wochenende ausgewählt.");
			downloadButton.setEnabled(false);
		} else if (c.after(today)) {
			errorMessage.setText("Sendung liegt in der Zukunft.");
			downloadButton.setEnabled(false);
		} else {
			errorMessage.setText("");
			downloadButton.setEnabled(true);
		}
	}

	public void startDownload(View view) {
		DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
		Date date = new Date(datePicker.getCalendarView().getDate());
		String dateString = DateFormat.format("yyyy-MM-dd", date).toString();

		String quality = ((Switch) findViewById(R.id.hqSwitch)).isChecked() ? "hq"
				: "lq";
		String filename = "Domian_" + dateString + "." + quality + ".ogg";
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Request req = new DownloadManager.Request((new Uri.Builder())
					.scheme("http").authority("cache.domianarchiv.de")
					.path("/" + filename).build());
			req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			req.setVisibleInDownloadsUi(true);
			File folder = new File(Environment.DIRECTORY_PODCASTS + "/Domian");
			if (!folder.isDirectory()) {
				folder.mkdirs();
			}
			req.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS + "/Domian", filename);
			req.setTitle(filename);
			req.allowScanningByMediaScanner();
			DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
			assert req != null;
			downloadManager.enqueue(req);
		}
	}

}
