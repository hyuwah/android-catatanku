package io.github.hyuwah.catatanku.CustomView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hyuwah on 26/01/18.
 */

public class RecyclerViewEmptySupport extends RecyclerView {
    @Nullable View emptyView;

    final @NonNull AdapterDataObserver emptyObserver = new AdapterDataObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }
    };

    void checkIfEmpty(){
        if(emptyView!=null){
            emptyView.setVisibility(getAdapter().getItemCount()>0?GONE:VISIBLE);
        }
    }

    public RecyclerViewEmptySupport(Context context) {
        super(context);
    }

    public RecyclerViewEmptySupport(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewEmptySupport(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if(oldAdapter!=null){
            oldAdapter.unregisterAdapterDataObserver(emptyObserver);
        }
        super.setAdapter(adapter);
        if(adapter!=null){
            adapter.registerAdapterDataObserver(emptyObserver);
        }
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}
