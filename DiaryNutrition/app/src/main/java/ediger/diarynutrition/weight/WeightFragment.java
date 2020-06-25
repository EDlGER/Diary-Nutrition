package ediger.diarynutrition.weight;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Weight;
import ediger.diarynutrition.databinding.FragmentWeightBinding;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;

public class WeightFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentWeightBinding mBinding;

    private WeightViewModel mViewModel;

    private WeightAdapter mWeightAdapter;

    private int mNumberOfDays = 7;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater,R.layout.fragment_weight, container, false);

        setHasOptionsMenu(true);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(requireActivity(), R.array.weight_interval,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBinding.spInterval.setAdapter(adapter);
        mBinding.spInterval.setOnItemSelectedListener(this);

        mWeightAdapter = new WeightAdapter(mBinding.emptyListWeight);

        mBinding.listWeight.setAdapter(mWeightAdapter);
        mBinding.listWeight.setLayoutManager(new LinearLayoutManager(getContext()));
        registerForContextMenu(mBinding.listWeight);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WeightViewModel.class);

        mViewModel.getWeight().observe(this, weights -> {
            mWeightAdapter.setWeightList(weights);

            Calendar since = Calendar.getInstance();
            since.add(Calendar.DAY_OF_YEAR, -mNumberOfDays);
            mViewModel.setDate(since);

        });
        mViewModel.getWeightSinceDate().observe(this, this::generateData);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        requireActivity().getMenuInflater().inflate(R.menu.fragment_weight, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            showWeightDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, R.string.context_menu_del).setActionView(v);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = mWeightAdapter.getCurrentId();

        if (item.getItemId() == 1) {
            mViewModel.deleteWeight(id);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void generateData(List<Weight> weightData) {
        if (weightData == null) {
            return;
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM", Locale.getDefault());

        int numberOfPoints = weightData.size() > 7 ? weightData.size() : 7;
        float maxWeightViewport = 0;
        float minWeightViewport = 200;

        List<AxisValue> axisValues = new ArrayList<>();
        List<PointValue> values = new ArrayList<>();

        int i = 1;
        for (Weight weight : weightData) {
            values.add(new PointValue(i, weight.getAmount())
                    .setTarget(i, weight.getAmount())
                    .setLabel(String.format(Locale.getDefault(), "%.1f", weight.getAmount())));
            axisValues.add(new AxisValue(i).setLabel(dateFormatter.format(weight.getDatetime())));
            i++;
            if (weight.getAmount() > maxWeightViewport) maxWeightViewport = weight.getAmount();
            if (weight.getAmount() < minWeightViewport) minWeightViewport = weight.getAmount();
        }

        Line line = new Line(values);
        line.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        line.setShape(ValueShape.CIRCLE);
        line.setFilled(false);
        line.setHasLines(true);
        line.setHasPoints(true);
        line.setHasLabels(numberOfPoints == 7);
        line.setHasLabelsOnlyForSelected(numberOfPoints != 7);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData(lines);

        Axis axisY = new Axis().setHasLines(true);
        Axis axisX = new Axis();
        axisX.setValues(axisValues);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mBinding.weightChart.setLineChartData(data);
        mBinding.weightChart.setValueSelectionEnabled(numberOfPoints != 7);
        resetViewport(numberOfPoints, maxWeightViewport, minWeightViewport);
    }

    private void resetViewport(int maxDateViewport,
                               float maxWeightViewport,
                               float minWeightViewport) {
        final Viewport v = new Viewport(mBinding.weightChart.getMaximumViewport());
        v.bottom = 40;
        v.top = 150;
        v.left = 0;
        v.right = maxDateViewport + 1;
        mBinding.weightChart.setMaximumViewport(v);

        v.bottom = minWeightViewport - 2;
        v.top = maxWeightViewport + 2;
        mBinding.weightChart.setCurrentViewport(v);
    }

    private void showWeightDialog() {
        Bundle bundle = new Bundle();
        bundle.putLong(AddWeightDialog.ARG_DATE, Calendar.getInstance().getTimeInMillis());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_weight_to_action_add_weight_dialog, bundle);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Calendar since = Calendar.getInstance();

        switch (position) {
            case 0:
                mNumberOfDays = 7;
                break;
            case 1:
                mNumberOfDays = 30;
                break;
            case 2:
                mNumberOfDays = 60;
                break;
        }
        since.add(Calendar.DAY_OF_YEAR, -mNumberOfDays);
        mViewModel.setDate(since);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
