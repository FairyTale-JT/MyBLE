package com.example.cdha.myble;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.cdha.adapter.DeviceListAdapter;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;
import com.inuker.bluetooth.library.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.cdha.utils.ToastUtils.showError;
import static com.example.cdha.utils.ToastUtils.showInfo;
import static com.example.cdha.utils.ToastUtils.showSuccess;
import static com.example.cdha.utils.ToastUtils.showWarning;
import static com.example.cdha.utils.ZhuanUtils.hexStringToByte;
import static com.example.cdha.utils.ZhuanUtils.numToHex16;
import static com.example.cdha.utils.ZhuanUtils.str2HexStr;
import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static String LOGTAG = "BLE_MainActivity";
    private ListView BLE_list;
    private LinearLayout search_linear;
    private LinearLayout luru_linear;
    private List<SearchResult> mDevices;
    private DeviceListAdapter mAdapter;
    private boolean mConnected;
    private boolean isname;
    private String address = "";
    private UUID mService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private UUID mCharacter = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private UUID mNotify = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        isBuleBLE();
    }

    /**
     * TODO  控件初始化
     */
    private void initView() {
        mDevices = new ArrayList<SearchResult>();
        BLE_list = (ListView) findViewById(R.id.BLE_list);
        mAdapter = new DeviceListAdapter(this);
        BLE_list.setAdapter(mAdapter);
        search_linear = (LinearLayout) findViewById(R.id.search_linear);
        search_linear.setOnClickListener(this);
        luru_linear = (LinearLayout) findViewById(R.id.luru_linear);
        luru_linear.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
        }
    }

    /**
     * TODO 每個Item点击
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_linear:
                isBuleBLE();
                ClientManager.getClient().stopSearch();
                searchDevice();
                break;
            case R.id.luru_linear:
                ClientManager.getClient().indicate(address, mService, mNotify, mNotifyRsp);
                send_Data();
                break;
        }
    }

    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(5000, 2).build();

        ClientManager.getClient().search(request, mSearchResponse);
    }

    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            BluetoothLog.w("MainActivity.onSearchStarted");
            showInfo(MainActivity.this, "扫描蓝牙中…");
            mDevices.clear();
            ClientManager.getClient().refreshCache(address);
            isname = false;
            address = "";
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                mAdapter.setDataList(mDevices);
                BluetoothLog.e(String.format("beacon for %s\n%s", device.getAddress(), device.toString()));

            }
        }

        @Override
        public void onSearchStopped() {
            BluetoothLog.e("MainActivity.onSearchStopped");
            BluetoothLog.e("扫描结束");
            for (int i_name = 0; i_name < mDevices.size(); i_name++) {
                if (mDevices.get(i_name).getName().equals("TKY_JZXDW@HA")) {
                    Log.e("is_name--", "iname----------");
                    for (int i = 0; i < new Beacon(mDevices.get(i_name).scanRecord).mItems.size(); i++) {
                        Log.e("ssssss", new String(new Beacon(mDevices.get(i_name).scanRecord).mItems.get(i).bytes));
                        if (numToHex16(new Beacon(mDevices.get(i_name).scanRecord).mItems.get(i).type).equals("ff")) {
                            Log.e("is_name--", "bytes----------");
                            address = mDevices.get(i_name).getAddress();
                            ClientManager.getClient().registerConnectStatusListener(mDevices.get(i_name).getAddress(), mConnectStatusListener);
                            connectDevice();
                            isname = true;
                            break;
                        }
                    }
                    if (isname) {
                        break;
                    }
                }
            }
        }

        @Override
        public void onSearchCanceled() {
            BluetoothLog.e("MainActivity.onSearchCanceled");
            BluetoothLog.e("扫描结束");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClientManager.getClient().stopSearch();
        ClientManager.getClient().closeBluetooth();
        if (!address.equals("")) {
            ClientManager.getClient().disconnect(address);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().stopSearch();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    /**
     * TODO 判断蓝牙是否打开
     */
    private void isBuleBLE() {

        if (!ClientManager.getClient().isBluetoothOpened()) {
            ClientManager.getClient().openBluetooth();
            ClientManager.getClient().registerBluetoothStateListener(mBluetoothStateListener);
        }
    }

    private final BluetoothStateListener mBluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            showInfo(MainActivity.this, "蓝牙已打开");
        }

    };
    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == STATUS_CONNECTED);
//
            connectDeviceIfNeeded(mac);
        }
    };

    private void connectDeviceIfNeeded(String mac) {
        if (!mConnected) {
            //connectDevice();
        }
    }

    private void connectDevice() {
        showInfo(MainActivity.this, String.format("%s%s", "正在连接", "--" + address));

        ClientManager.getClient().connect(address, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                BluetoothLog.v(String.format("profile:\n%s", profile));
                Log.e("Main--address", String.format("%s", address));


                if (code == REQUEST_SUCCESS) {
                    showSuccess(MainActivity.this, "连接成功");
                } else {

                    showError(MainActivity.this, "连接失败");
                }
            }
        });
    }

    /**
     * TODO 发送数据
     */
    private void send_Data() {
        final View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        final EditText editText = inflate.findViewById(R.id.dialog_input_editText);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("发送设备号");
        alertDialog.setView(inflate);
        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setPositiveButton("确定", null);
        final AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(false);
        dialog.show();
        //创建dialog点击监听OnClickListener
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString();
                if (str.trim().length() != 0 && str.trim().length() == 16) {
                    BluetoothLog.e(address);
                    ClientManager.getClient().write(address, mService, mCharacter,
                            hexStringToByte(str2HexStr("NB:" + str)), mWriteRsp);
                    dialog.cancel();
                } else {
                    showWarning(MainActivity.this, "请检查重新输入");
                }
            }
        });
    }

    private final BleWriteResponse mWriteRsp = new BleWriteResponse() {

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                showSuccess(MainActivity.this, "写入成功");
            } else {
                showError(MainActivity.this, "写入失败");
            }
        }
    };
    private final BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            Log.e("mNotifyRsp--", String.format("%s", service + "--" + character));
            if (service.equals(mService) && character.equals(mCharacter)) {
                Log.e("mNotifyRsp--", String.format("%s", ByteUtils.byteToString(value)));
            }
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                Log.e("mNotifyRsp--", "成功");
            } else {
                Log.e("mNotifyRsp--", "失败");
            }
        }
    };
}
