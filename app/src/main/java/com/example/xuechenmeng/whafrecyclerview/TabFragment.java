package com.example.xuechenmeng.whafrecyclerview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by @vitovalov on 30/9/15.
 */
public class TabFragment extends Fragment {

    private ListAdapter mAdapter;
    private XRecyclerView mRecyclerView;
    private String mItemData = "Lorem Ipsum is simply dummy text of the printinger";
    private String mItemMoreData = "1 2 3 4 5 6 7 8 9";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, container, false);

        mRecyclerView = (XRecyclerView) view.findViewById(R.id.fragment_list_rv);
        mRecyclerView.setPullRefreshEnable(false);
        mRecyclerView.setPullLoadEnable(true);
        //这个RecyclerView不能设置LayoutManager，它内部已经包含了一个LinearLayoutManager。
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        //mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        String[] listItems = mItemData.split(" ");
        final String[] listMoreItems = mItemMoreData.split(" ");
        final List<String> list = new ArrayList<String>();
        final List<String> listMore = new ArrayList<String>();
        Collections.addAll(list, listItems);
        Collections.addAll(listMore, listMoreItems);
        mRecyclerView.setLoadMoreListener(new XRecyclerView.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                mAdapter.addData(listMore);
                mRecyclerView.stopLoadMore();
            }
        });
        mAdapter = new ListAdapter(list, R.layout.list_item, true);
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }
}
