package com.ttn.stationarymanagement.presentation.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.core.Base;
import com.ttn.stationarymanagement.R;
import com.ttn.stationarymanagement.data.local.WorkWithDb;
import com.ttn.stationarymanagement.data.local.model.CapPhat;
import com.ttn.stationarymanagement.data.local.model.NhanVien;
import com.ttn.stationarymanagement.data.local.model.VanPhongPham;
import com.ttn.stationarymanagement.presentation.baseview.BaseActivity;
import com.ttn.stationarymanagement.utils.CustomToast;
import com.ttn.stationarymanagement.utils.GetDataToCommunicate;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DetailBillActivity extends BaseActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_id_bill)
    TextView tvIdBill;

    @BindView(R.id.tv_date_create)
    TextView tvDateCreate;

    @BindView(R.id.tv_id_staft)
    TextView tvIdStaft;

    @BindView(R.id.tv_name_staft)
    TextView tvNameStaft;

    @BindView(R.id.tv_id_product)
    TextView tvIdProduct;

    @BindView(R.id.tv_name_product)
    TextView tvNameProduct;

    @BindView(R.id.tv_amount_product)
    TextView tvAmountProduct;

    @BindView(R.id.tv_total_price)
    TextView tvTotalPrice;

    @BindView(R.id.tv_note_bill)
    TextView tvNote;

    CompositeDisposable compositeDisposable;


    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, DetailBillActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_bill);
        ButterKnife.bind(this);

        setControls();
        getDatas();


    }

    private VanPhongPham product;   // S???n ph???m c???p ph??t
    private NhanVien staft;         // Nh??n vi??n c???p ph??t


    private void getDatas() {

        Intent intent = getIntent();

        if (intent.hasExtra("ID_BILL")) {

            long idBill = intent.getLongExtra("ID_BILL", 0);        // L???y id s???n ph???m c???n hi???n th??? tri ti???t

            Observable<CapPhat> getInforBill = Observable.just(WorkWithDb.getInstance().getAllocationById(idBill));

            compositeDisposable.add(getInforBill.subscribeOn(Schedulers.newThread())
                    .filter(capPhat -> capPhat != null)
            .flatMap(capPhat -> {

                // L???y th??ng tin s???n ph???m
                product = WorkWithDb.getInstance().getProductById(capPhat.getMaVPP());

                // L???y th??ng tin nh??n vi??n
                staft = WorkWithDb.getInstance().getStaftById(capPhat.getMaNV());

                return Observable.just(capPhat);

            }).observeOn(AndroidSchedulers.mainThread())
            .subscribe(capPhat -> {

                // Hi???n th??? c??c th??ng tin

                tvIdBill.setText(getResources().getString(R.string.code_bill) + ": " + capPhat.getMaPhieu());
                tvDateCreate.setText(capPhat.getNgayCap());
                tvIdStaft.setText(capPhat.getMaNV() + "");
                tvIdProduct.setText(capPhat.getMaVPP() + "");
                tvAmountProduct.setText(capPhat.getSoLuong() + "");
                tvTotalPrice.setText(GetDataToCommunicate.changeToPrice( capPhat.getSoLuong() * capPhat.getTongGia()));

                if (!TextUtils.isEmpty(capPhat.getGhiChu())) {
                    tvNote.setText(getResources().getString(R.string.note) + ": " + capPhat.getGhiChu());
                } else {
                    tvNote.setVisibility(View.GONE);
                }

                // T??n nh??n vi??n
                if (staft != null) {
                    tvNameStaft.setText(!TextUtils.isEmpty(staft.getTenNV()) ? staft.getTenNV() : "");
                } else {
                    tvNameStaft.setText("");
                }

                // T??n s???n ph???m
                if (product != null) {
                    tvNameProduct.setText(!TextUtils.isEmpty(product.getTenSP()) ? product.getTenSP() : "");
                } else {
                    tvNameProduct.setText("");
                }

            }, throwable -> {
                CustomToast.showToastError(this, getResources().getString(R.string.occurre_error), Toast.LENGTH_SHORT);
            }));


        } else {
            CustomToast.showToastError(this, getResources().getString(R.string.do_not_get_infor_of_product), Toast.LENGTH_SHORT);
            finish();
        }


    }

    private void setControls() {

        compositeDisposable = new CompositeDisposable();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.detail_bill));

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}