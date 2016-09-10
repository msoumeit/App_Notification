package com.app.saarr.notify.aboutus;

/**
 * Created by sumit_tanay on 31-12-2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.saarr.notify.NotifyUtils;
import com.app.saarr.notify.R;


public class PageFragment extends Fragment {
    private static final String ARG_PAGE_NUMBER = "page_number";

    public PageFragment() {
    }

    public static PageFragment newInstance(int page) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_NUMBER, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        TextView txtTitle = null;

        int page = getArguments().getInt(ARG_PAGE_NUMBER, -1);


//        Html spTxt =  Html.fromHtml(getResources().getString(R.string.notetrap_about_body)); Not working
        switch (page) {
            case 1:
                rootView = inflater.inflate(R.layout.fragment_page_about_layout, container, false);
                txtTitle = (TextView) rootView.findViewById(R.id.page_body);

                txtTitle.setText(Html.fromHtml(NotifyUtils.STR_ABOUTUS));
                break;
            case 2:
                rootView = inflater.inflate(R.layout.fragment_page_layout, container, false);
                txtTitle = (TextView) rootView.findViewById(R.id.page_body);

                txtTitle.setText(Html.fromHtml(NotifyUtils.STR_PRIVACY));

                break;
            /*case 3:
                rootView = inflater.inflate(R.layout.fragment_page_layout, container, false);
                txtTitle = (TextView) rootView.findViewById(R.id.page_body);
                break;*/
        }


        return rootView;
    }
}