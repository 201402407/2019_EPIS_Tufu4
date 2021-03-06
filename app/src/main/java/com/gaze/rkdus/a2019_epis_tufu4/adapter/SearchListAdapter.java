package com.gaze.rkdus.a2019_epis_tufu4.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.gaze.rkdus.a2019_epis_tufu4.R;
import com.gaze.rkdus.a2019_epis_tufu4.item.SearchResultData;
import com.gaze.rkdus.a2019_epis_tufu4.utils.Prop;

/*
SearchActivity RecyclerView의 Adapter
 */
public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ItemViewHolder> {

    // adapter에 들어갈 list 입니다.
    private ArrayList<Prop.EntrpsData> entrpsList;
    private Context context;

    //아이템 클릭시 실행 함수
    private ItemClick itemClick;
    public interface ItemClick {
        public void onClick(View view,int position);
    }

    //아이템 클릭시 실행 함수 등록 함수
    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public SearchListAdapter(ArrayList<Prop.EntrpsData> list) {
        this.entrpsList = list;
    }

    public void resetAll(ArrayList<Prop.EntrpsData> newArrayList) { ;

        this.entrpsList = null;
        this.entrpsList = newArrayList;
    }

    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder {
        View view;
        private TextView hospitalNameText;
        private TextView ceoNameText;
        private ImageView signUpAppImage;

        ItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            hospitalNameText = itemView.findViewById(R.id.hospitalNameTextView);
            ceoNameText = itemView.findViewById(R.id.ceoNameTextView);
            signUpAppImage = itemView.findViewById(R.id.signUpAppImage);
        }

        void onBind(Prop.EntrpsData data) {
            hospitalNameText.setText(data.getEntrpsName());
            ceoNameText.setText(data.getEntrpsRepresentName());
            //ceoNameText.setText(String.valueOf(data.getRESERVATION_COUNT()));
            if(data.getEntrpsMemberId() != null && data.getEntrpsMemberId().length() != 0)    // 등록 시
                signUpAppImage.setImageResource(R.drawable.search_signupappoicon);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate.
        // return 인자는 ViewHolder.
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null) {
                    itemClick.onClick(v, position);
                }
            }
        });

        holder.onBind(entrpsList.get(position));
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return entrpsList.size();
    }

    void addItem(Prop.EntrpsData entrpsData) {
        // 외부에서 item을 추가시킬 함수입니다.
        entrpsList.add(entrpsData);
    }
}