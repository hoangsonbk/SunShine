package gemmy.sunshine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,	false);
			
			String[] forecastArray = {
					"Today - Sunny -28/29",
					"Tomorrow - Cloudy -26/29",
					"Wed - Shower -25/27",
					"Thu - Heavy Rain - 25/26",
					"Fri - Sunny - 28/30",
					"Sat - Foggy - 26/27",
					"Sun - Sunny - 27/29"
			};
			List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));
			ArrayAdapter<String> mForecastAdapter = new ArrayAdapter<String>(
					getActivity(), //Current context, this fragment's parent activity
					R.layout.list_item_forecast,
					R.id.list_item_forecast_textview,
					weekForecast);
			
			ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
			listView.setAdapter(mForecastAdapter);

			return rootView;
		}
	}
}
