package com.ttn.stationarymanagement.presentation.dialog_fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ttn.stationarymanagement.R;
import com.ttn.stationarymanagement.data.local.WorkWithDb;
import com.ttn.stationarymanagement.data.local.model.PhongBan;
import com.ttn.stationarymanagement.utils.CustomToast;
import com.ttn.stationarymanagement.utils.GetDataToCommunicate;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddDepartmentDialog extends DialogFragment {

    public static final String TAG = AddDepartmentDialog.class.getSimpleName();

    @BindView(R.id.tv_dialog_add_department_close)
    TextView tvClose;

    @BindView(R.id.edt_dialog_add_department_name)
    EditText edtNameDepartment;

    @BindView(R.id.edt_dialog_add_department_note)
    EditText edtNote;

    @BindView(R.id.btn_dialog_add_department_add)
    Button btnAdd;

    private CompositeDisposable compositeDisposable;

    public interface AddDepartmentDilaogListener {
        public void onAddSuccesstion();
        public void onUpload(String department, String note);
    }

    private boolean isUpload = false;

    private AddDepartmentDilaogListener mListener;

    public void setListener(AddDepartmentDilaogListener listener) {
        this.mListener = listener;
    }

    public static AddDepartmentDialog newInstance(String department, String note) {
        Bundle args = new Bundle();
        args.putString("NAME", department);
        args.putString("NOTE",note );
        AddDepartmentDialog fragment = new AddDepartmentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_department, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        compositeDisposable = new CompositeDisposable();

        getData();
        setEvents();

    }

    // L???y d??? li???u ph??ng ban c???n c???p nh???t
    private void getData() {

        String name = getArguments().getString("NAME", "");
        String note = getArguments().getString("NOTE", "");

        if (!TextUtils.isEmpty(name)) {     // C?? th??ng tin c???n c???p nh???t

            // ????a d??? li???u c???n ???????c l??u l??n hi???n th???
            edtNameDepartment.setText(name);
            edtNote.setText(note);
            btnAdd.setText(getResources().getString(R.string.upload));
            isUpload = true;

        }

    }

    private void setEvents() {

        // Khi click v??o n??t h???y
        tvClose.setOnClickListener(v -> dismiss());

        // Khi nh???n v??o n??t th??m
        btnAdd.setOnClickListener(v -> {

            // Ki???m tra t??n ph??ng ban
            if (TextUtils.isEmpty(edtNameDepartment.getText().toString())) {
                edtNameDepartment.setError(getResources().getString(R.string.name_department_do_not_empty));
                edtNameDepartment.requestFocus();
                return;
            }

            if (isUpload) {     // C???p nh???t ph??ng ban
                if (mListener != null) {
                    String name = edtNameDepartment.getText().toString();
                    String note = !TextUtils.isEmpty(edtNote.getText().toString()) ? edtNote.getText().toString() : "";
                    mListener.onUpload(edtNameDepartment.getText().toString(), note);
                    dismiss();
                }
            } else {        // T???o m???i ph??ng ban
                createDepartment();
            }

        });

    }

    private void createDepartment() {

        // Ob t???o ph??ng ban
        Observable<Boolean> obCreateDepartment = Observable.create(r -> {

            try {

                // Set c??c gi?? tr??? ph??ng ban c???n t???o
                PhongBan phongBan = new PhongBan();
                phongBan.setTenPB(edtNameDepartment.getText().toString());
                phongBan.setGhiChu(!TextUtils.isEmpty(edtNameDepartment.getText().toString()) ? edtNote.getText().toString() : "");
                phongBan.setNgayTao(GetDataToCommunicate.getCurrentDate()); // Ng??y t???o

                r.onNext( WorkWithDb.getInstance().insert(phongBan)); // L??u xu???ng Db
                r.onComplete();

            } catch (Exception e) {
                e.printStackTrace();
                r.onError(e);
            }

        });

        compositeDisposable.add(obCreateDepartment.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {

            if (aBoolean) {
                CustomToast.showToastSuccesstion(getContext(), getResources().getString(R.string.add_successful), Toast.LENGTH_SHORT);

                if (mListener != null) {
                    mListener.onAddSuccesstion();
                    dismiss();
                }

            } else {
                CustomToast.showToastError(getContext(), getResources().getString(R.string.add_failed), Toast.LENGTH_SHORT);
            }

        }, throwable -> {
            CustomToast.showToastError(getContext(), getResources().getString(R.string.add_failed), Toast.LENGTH_SHORT);
        }));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.dispose();
    }
}
