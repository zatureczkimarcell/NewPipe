package org.schabi.newpipe.history;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.schabi.newpipe.R;
import org.schabi.newpipe.database.history.model.SearchHistoryEntry;
import org.schabi.newpipe.util.NavigationHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SearchHistoryFragment extends HistoryFragment<SearchHistoryEntry> {

    @NonNull
    public static SearchHistoryFragment newInstance() {
        return new SearchHistoryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected SearchHistoryAdapter createAdapter() {
        return new SearchHistoryAdapter(getContext());
    }

    @Override
    protected Single<List<Long>> insert(Collection<SearchHistoryEntry> entries) {
        return historyRecordManager.insertSearches(entries);
    }

    @Override
    protected Single<Integer> delete(Collection<SearchHistoryEntry> entries) {
        return historyRecordManager.deleteSearches(entries);
    }

    @NonNull
    @Override
    protected Flowable<List<SearchHistoryEntry>> getAll() {
        return historyRecordManager.getSearchHistory();
    }

    @StringRes
    @Override
    int getEnabledConfigKey() {
        return R.string.enable_search_history_key;
    }

    @Override
    public void onHistoryItemClick(final SearchHistoryEntry historyItem) {
        NavigationHelper.openSearch(getContext(), historyItem.getServiceId(),
                historyItem.getSearch());
    }

    @Override
    public void onHistoryItemLongClick(final SearchHistoryEntry item) {
        if (activity == null) return;

        new AlertDialog.Builder(activity)
                .setTitle(item.getSearch())
                .setMessage(R.string.delete_item_search_history)
                .setCancelable(true)
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_one, (dialog, i) -> {
                    final Single<Integer> onDelete = historyRecordManager
                            .deleteSearches(Collections.singleton(item))
                            .observeOn(AndroidSchedulers.mainThread());
                    disposables.add(onDelete.subscribe());
                    makeSnackbar(R.string.item_deleted);
                })
                .setNegativeButton(R.string.delete_all, (dialog, i) -> {
                    final Single<Integer> onDeleteAll = historyRecordManager
                            .deleteSearchHistory(item.getSearch())
                            .observeOn(AndroidSchedulers.mainThread());
                    disposables.add(onDeleteAll.subscribe());
                    makeSnackbar(R.string.item_deleted);
                })
                .show();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView search;
        private final TextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            search = itemView.findViewById(R.id.search);
            time = itemView.findViewById(R.id.time);
        }
    }

    protected class SearchHistoryAdapter extends HistoryEntryAdapter<SearchHistoryEntry, ViewHolder> {


        public SearchHistoryAdapter(Context context) {
            super(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View rootView = inflater.inflate(R.layout.item_search_history, parent, false);
            return new ViewHolder(rootView);
        }

        @Override
        void onBindViewHolder(ViewHolder holder, SearchHistoryEntry entry, int position) {
            holder.search.setText(entry.getSearch());
            holder.time.setText(getFormattedDate(entry.getCreationDate()));
        }
    }
}
