package vijji.codepath.android.nytimessearch.activites;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import vijji.codepath.android.nytimessearch.DatePickerFragment;
import vijji.codepath.android.nytimessearch.R;

/**
 * Created by vijji on 9/29/17.
 */

public class FilterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private TextView dateView;
    public Spinner sortOrder;
    public static int sortOrderIndex;
    public static int filtersOn;

    private CheckBox cbArts, cbFashion, cbSports;
    public static boolean bArts, bFashion, bSports;

    public static int year;
    public static int month;
    public static int day;
    // store the values selected into a Calendar instance
    static final Calendar c = Calendar.getInstance();
    private Calendar calendar;
    public static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    public static String beginDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_fragment);

        sortOrder = (Spinner) findViewById(R.id.spSort);
        sortOrder.setSelection(sortOrderIndex);

        filtersOn = 0;

        //find date view to launch DatePicker when clicked on this textview
        dateView = (TextView) findViewById(R.id.tvDatePicker);
        calendar = Calendar.getInstance();

        if (year == 0 && month == 0 && day == 0) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        cbArts = (CheckBox) findViewById(R.id.cbArt);
        cbFashion = (CheckBox) findViewById(R.id.cbFashion);
        cbSports = (CheckBox) findViewById(R.id.cbSports);


        cbArts.setChecked(bArts);
        cbFashion.setChecked(bFashion);
        cbSports.setChecked(bSports);

        showDate(year, month + 1, day);
    }


    // attach to an onclick handler to dateView to show the date picker
    public void showDatePickerDialog(View view) {

        dateView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    // Handle the date selected
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        beginDate = format.format(c.getTime());
        showDate(year, monthOfYear + 1, dayOfMonth);
    }

    //set current date in the dateView textview
    public void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(month).append("/")
                .append(day).append("/").append(year));
    }


    public void onSave(View view) {

        filtersOn = 1;

        sortOrderIndex = sortOrder.getSelectedItemPosition();

        this.year = c.get(Calendar.YEAR);
        this.month = c.get(Calendar.MONTH);
        this.day = c.get(Calendar.DAY_OF_MONTH);

        beginDate = format.format(c.getTime());

        bArts = cbArts.isChecked();
        bFashion= cbFashion.isChecked();
        bSports = cbSports.isChecked();

        Intent i=new Intent();

        // put the message in Intent
        i.putExtra("sortOrderIndex",sortOrderIndex);

        i.putExtra("bArts",bArts);
        i.putExtra("bFashion",bFashion);
        i.putExtra("bSports",bSports);

        i.putExtra("beginDate",beginDate);

        i.putExtra("filtersOn", filtersOn);


        // Set The Result in Intent
        setResult(2,i);
        // finish The activity
        finish();

    }

}
