/**
 * HANNAH BUZARD
 * DAVID KIPNIS
 * TYLER KJELDGAARD
 * DANIEL SHTUNYUK
 *
 * WESTERN WASHINGTON UNIVERSITY
 * CSCI 412 - WINTER 2021
 *
 * TASQ APPLICATION PROJECT
 */

package tasq.app.ui.daily;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tasq.app.Login;
import tasq.app.R;
import tasq.app.Task;
import tasq.app.ui.addedit.AddEditViewModel;

public class DailyFragment extends Fragment {

    private DailyViewModel mViewModel;
    private AddEditViewModel model;
    private TextView dailyDate;
    private Date curDate;
    private Date displayDate;
    private boolean running = false;

    private NavController navController;

    public static DailyFragment newInstance() {
        return new DailyFragment();
    }

    /**
     * inflating the appropriate layout when screen is being accessed
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.daily_fragment, container, false);
    }

    /**
     * creating references to the appropriate models and navcontroller
     * updating the screen with existing information
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d("update", "In activity creation");
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(getActivity()).get(DailyViewModel.class);
        model = new ViewModelProvider(requireActivity()).get(AddEditViewModel.class);
        navController = Navigation.findNavController(getView());
        curDate = new Date();
        model.getTask().observe(getViewLifecycleOwner(), item -> {
            Log.d("update", "In activity creation");
            updateUI(item);
        });
        dailyDate = getActivity().findViewById(R.id.daily_screen_date);
        //reformat date for display
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("EEE MMM dd");
        dailyDate.setText(df.format(date));
    }

    /**
     * method for updating the screen with any new or updated information
     */
    private void updateUI(ArrayList<Task> arr) {
        LinearLayout ll = getActivity().findViewById(R.id.daily_central_layout);
        ArrayList<Task> allTasks = new ArrayList<Task>();
        String currentDate = new SimpleDateFormat("MM.dd.yyyy").format(new Date());
        Date curDate = null;

        // creating the reference to the current date
        for (int i = 0; i < arr.size(); i++) {
            Task curTask = arr.get(i);
            String date = Task.getDate(curTask);
            if (date.compareTo("") != 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("MM.dd.yyyy");
                Date curTaskDate = null;
                try {
                    curTaskDate = formatter.parse(date);
                    curDate = formatter.parse(currentDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curDate.toString().compareTo(curTaskDate.toString()) == 0) {
                    allTasks.add(curTask);
                }
            }
        }

        // iterating through tasks and updating/adding them on the screen
        for (Task task : allTasks) {
            Button taskButton = new Button(getActivity());
            taskButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("taskName", Task.getText(task));
                    editor.putString("taskDate", Task.getDate(task));
                    editor.putString("taskColor", Task.getColor(task));
                    editor.apply();
                    navController.navigate(R.id.displayTask_page);
                    task.setText(sharedPreferences.getString("taskName", "---"));
                    task.setDate(sharedPreferences.getString("taskDate", "---"));
                    task.setColor(sharedPreferences.getString("taskColor", "---"));
                }
            });
            taskButton.setAllCaps(false);
            taskButton.setText(Task.getText(task));
            CheckBox ch = new CheckBox(getActivity());
            ch.setText("");
            if(task.isCompleted() == true) {
                ch.setChecked(true);
                taskButton.setPaintFlags(taskButton.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            taskButton.setTextColor(getResources().getColor(R.color.white));
            taskButton.setBackgroundResource(R.drawable.task_plain);
            taskButton.setTextAlignment(Button.TEXT_ALIGNMENT_VIEW_START);
            taskButton.setTextSize(25);
            taskButton.setPadding(70, 20, 70, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(30, 15, 30, 15);
            taskButton.setLayoutParams(lp);
            LinearLayout linear = new LinearLayout(getActivity());
            linear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linear.setOrientation(LinearLayout.HORIZONTAL);
            ch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: add sound when task is completed
                    if(task.isCompleted() == true) {
                        Task newTask = new Task(Task.getColor(task),
                                Task.getDate(task),
                               Task.getText(task),
                                false);
                         ll.removeAllViews();
                        model.updateTask(task, newTask);
                    } else {
                        Task newTask = new Task(Task.getColor(task),
                                Task.getDate(task),
                                Task.getText(task),
                                true);
                        ll.removeAllViews();
                        model.updateTask(task, newTask);
                    }
                }
            });
            linear.addView(ch);
            linear.addView(taskButton);
            ll.addView(linear);
        }
    }
}