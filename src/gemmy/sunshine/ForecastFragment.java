package gemmy.sunshine;

import gemmy.sunshine.data.WeatherContract;
import gemmy.sunshine.data.WeatherContract.LocationEntry;
import gemmy.sunshine.data.WeatherContract.WeatherEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter mForecastAdapter;
	private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
	
    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING
    };


    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

	
	public ForecastFragment() {
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getLoaderManager().initLoader(FORECAST_LOADER, null, this);
	    super.onActivityCreated(savedInstanceState);
	    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.forecastfragment, menu);
		}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			updateWeather();
			return true;
			}
		return super.onOptionsItemSelected(item);
	}
	
	private void updateWeather(){
		FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
		String location = Utility.getPreferredLocation(getActivity());
		weatherTask.execute(location);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		Log.v(LOG_TAG, "Forecast Fragment onStart");
		//updateWeather();
	}
	
    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,	false);
		//Context context = getActivity(); //Current context, this fragment's parent activity
		
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
		mForecastAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_forecast,
                null,
                // the column names to use to fill the textviews
                new String[]{WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
                },
                // the textviews to fill with the data pulled from the columns above
                new int[]{R.id.list_item_date_textview,
                        R.id.list_item_forecast_textview,
                        R.id.list_item_high_textview,
                        R.id.list_item_low_textview
                },
                0
        );
		
		  mForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
	            @Override
	            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	                boolean isMetric = Utility.isMetric(getActivity());
	                switch (columnIndex) {
	                    case COL_WEATHER_MAX_TEMP:
	                    case COL_WEATHER_MIN_TEMP: {
	                        // we have to do some formatting and possibly a conversion
	                        ((TextView) view).setText(Utility.formatTemperature(
	                                cursor.getDouble(columnIndex), isMetric));
	                        return true;
	                    }
	                    case COL_WEATHER_DATE: {
	                        String dateString = cursor.getString(columnIndex);
	                        TextView dateView = (TextView) view;
	                        dateView.setText(Utility.formatDate(dateString));
	                        return true;
	                    }
	                }
	                return false;
	            }
	        });
		/* OLD CODE USING ARRAY ADAPTER
		 mForecastAdapter = new ArrayAdapter<String>(
				getActivity(),
				R.layout.list_item_forecast,
				R.id.list_item_forecast_textview,
				weekForecast);*/
		
		ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
		listView.setAdapter(mForecastAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				// TODO Auto-generated method stub
				/* OLD CODE USING ARRAY ADAPTER
			    String forecast = mForecastAdapter.getItem(position);
				Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
				Intent intentToDetailAct = new Intent (getActivity(), DetailActivity.class);
				intentToDetailAct.putExtra(Intent.EXTRA_TEXT, forecast);
				startActivity(intentToDetailAct);*/
				
				 Cursor cursor = mForecastAdapter.getCursor();
				 if (cursor != null && cursor.moveToPosition(position)) {
					 Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(DetailActivity.DATE_KEY, cursor.getString(COL_WEATHER_DATE));
					 startActivity(intent);
					 }
			}
			
		});
		return rootView;
	}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
		// TODO Auto-generated method stub
		mForecastAdapter.swapCursor(data);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		mForecastAdapter.swapCursor(null);
		
	}
	
	/*@SuppressLint("NewApi")
	public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{		
		@Override
		protected String[] doInBackground(String...param) {
			// These two need to be declared outside the try/catch
			// so that they can be closed in the finally block.
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			 
			// Will contain the raw JSON response as a string.
			String forecastJsonStr = null;
			String format = "json";
			String unit = "metric";
			Integer days = 7;
			try {
			    // Construct the URL for the OpenWeatherMap query
			    // Possible parameters are available at OWM's forecast API page, at
			    // http://openweathermap.org/API#forecast
				final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?";
				final String QUERY="q";
				final String FORMAT="mode";
				final String UNIT="units";
				final String DAYS="cnt";
				Uri buildUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
						.appendQueryParameter(QUERY, param[0])
						.appendQueryParameter(FORMAT, format)
						.appendQueryParameter(UNIT, unit)
						.appendQueryParameter(DAYS, Integer.toString(days))
						.build();
			    URL url = new URL(buildUri.toString());
			    Log.v(LOG_TAG,"Query URL: " + url);
			 
			    // Create the request to OpenWeatherMap, and open the connection
			    urlConnection = (HttpURLConnection) url.openConnection();
			    urlConnection.setRequestMethod("GET");
			    urlConnection.connect();
			 
			    // Read the input stream into a String
			    InputStream inputStream = urlConnection.getInputStream();
			    StringBuffer buffer = new StringBuffer();
			    if (inputStream == null) {
			        // Nothing to do.
			        return null;
			    }
			    reader = new BufferedReader(new InputStreamReader(inputStream));
			 
			    String line;
			    while ((line = reader.readLine()) != null) {
			        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
			        // But it does make debugging a *lot* easier if you print out the completed
			        // buffer for debugging.
			        buffer.append(line + "\n");
			        Log.v(LOG_TAG,"Get from Weather API: " + line);
			    }
			 
			    if (buffer.length() == 0) {
			        // Stream was empty.  No point in parsing.
			        forecastJsonStr = null;
			    }
			    forecastJsonStr = buffer.toString();
			} catch (IOException e) {
			    Log.e("PlaceholderFragment", "Error ", e);
			    // If the code didn't successfully get the weather data, there's no point in attempting
			    // to parse it.
			    forecastJsonStr = null;
			} finally{
			    if (urlConnection != null) {
			        urlConnection.disconnect();
			    }
			    if (reader != null) {
			        try {
			            reader.close();
			        } catch (final IOException e) {
			            Log.e("PlaceholderFragment", "Error closing stream", e);
			        }
			    }
			}
		    String [] weatherData = null;
		    
		    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String unitType = sharedPref.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_metric));
		    try {
				weatherData = ParseJSONdata.getWeatherDataFromJson(forecastJsonStr, days, unitType);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		    for (int i = 0; i < weatherData.length; i++){
		    	Log.v(LOG_TAG, "DATA RECEIVED: " + weatherData[i]);
		    }
			return weatherData;
		}
		
		@Override
		protected void onPostExecute(String...result) {
			mForecastAdapter.clear();
			mForecastAdapter.addAll(result);
			mForecastAdapter.notifyDataSetChanged();
	     }
		
	}*/
}