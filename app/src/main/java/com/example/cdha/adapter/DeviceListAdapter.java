package com.example.cdha.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cdha.myble.R;
import com.inuker.bluetooth.library.search.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/2/13 11:23
 */
public class DeviceListAdapter extends BaseAdapter implements Comparator<SearchResult> {
    private Context mContext;

    private List<SearchResult> mDataList;

    public DeviceListAdapter(Context context) {
        mContext = context;
        mDataList = new ArrayList<SearchResult>();
    }

    public void setDataList(List<SearchResult> datas) {
        mDataList.clear();
        mDataList.addAll(datas);
        Collections.sort(mDataList, this);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int compare(SearchResult lhs, SearchResult rhs) {
        return rhs.rssi - lhs.rssi;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        BaseViewHolder holder = BaseViewHolder.
                getViewHolder(mContext, convertView, viewGroup, R.layout.item_device_list, position);
        TextView name = holder.getView(R.id.name);
        TextView address = holder.getView(R.id.mac);
        TextView rssi = holder.getView(R.id.rssi);
        SearchResult result = (SearchResult) getItem(position);

        name.setText(result.getName());
        address.setText(result.getAddress());
        rssi.setText(String.format("Rssi: %d", result.rssi));
        return holder.getConvertView();
    }
}
