package com.example.xuechenmeng.whafrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

/**
 * Created by @vitovalov on 30/9/15.
 */
public class ListAdapter extends XRecyclerView.XRecyclerViewAdapter<String> {

    List<String> mListData;

    public ListAdapter(List<String> dataList, int itemLayout, boolean pullEnable) {
        super(dataList, itemLayout, pullEnable);
        mListData = dataList;
    }

    public void setData(List<String> listData) {
        mListData = listData;
        notifyDataSetChanged();
    }
    public void addData(List<String> listData) {
        if (mListData != null) {
            mListData.addAll(listData);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder setItemViewHolder(View itemView) {
        return new MyViewHolder(itemView);
    }

    @Override
    public void initItemView(RecyclerView.ViewHolder itemHolder, int posion, String entity) {
        MyViewHolder holder = (MyViewHolder) itemHolder;
        holder.title.setText(entity);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.listitem_name);
        }
    }




//    @Override
//    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item,
//                viewGroup, false);
//        return new MyViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
//        myViewHolder.title.setText(mListData.get(i));
//        if (listener != null) {
//            listener.OnItemClick(i);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return mListData == null ? 0 : mListData.size();
//    }
//
//    public void setOnItemClickListener (OnItemClickListener listener) {
//        this.listener = listener;
//    }
//
//    interface OnItemClickListener {
//        void OnItemClick(int position);
//    }
//

}

