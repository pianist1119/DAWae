package com.hotteam67.firebaseviewer.tableview;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.AdapterDataSetChangedListener;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerView;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerViewAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.RowHeaderRecyclerViewAdapter;
import com.evrencoskun.tableview.filter.Filter;
import com.evrencoskun.tableview.filter.FilterChangedListener;
import com.evrencoskun.tableview.filter.FilterItem;
import com.evrencoskun.tableview.filter.FilterType;
import com.evrencoskun.tableview.filter.IFilterableModel;
import com.evrencoskun.tableview.sort.SortState;
import com.hotteam67.firebaseviewer.data.MultiFilter;

import java.util.ArrayList;
import java.util.List;

public class MultiFilterTableView extends TableView {

    private CellRecyclerViewAdapter mCellRecyclerViewAdapter;
    private RowHeaderRecyclerViewAdapter mRowHeaderRecyclerViewAdapter;
    private List<List<IFilterableModel>> originalCellDataStore, originalCellData, filteredCellList;
    private List originalRowDataStore, originalRowData, filteredRowList;

    private MultiFilterTableView sibling;

    public MultiFilterTableView(@NonNull Context context) {
        super(context);
    }

    public MultiFilterTableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiFilterTableView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void SetSiblingTableView(MultiFilterTableView sibling)
    {
        this.sibling = sibling;
    }

    @Override
    public void setAdapter(AbstractTableAdapter a)
    {
        super.setAdapter(a);
        FilterInit();
    }

    public void FilterInit() {
        getAdapter().addAdapterDataSetChangedListener(adapterDataSetChangedListener);
        this.mCellRecyclerViewAdapter = (CellRecyclerViewAdapter)
                getCellRecyclerView().getAdapter();

        this.mRowHeaderRecyclerViewAdapter = (RowHeaderRecyclerViewAdapter)
                getRowHeaderRecyclerView().getAdapter();
    }

    @Override
    public void sortColumn(int column, SortState sort)
    {
        sortColumn(column, sort, false);
    }

    public void sortColumn(int column, SortState sort, boolean skipSibling)
    {
        super.sortColumn(column, sort);
        if (!skipSibling && sibling != null)
            sibling.sortColumn(column, sort, true);
    }

    @Override
    public void filter(Filter filter)
    {
        filter(filter, false);
    }

    @SuppressWarnings("unchecked")
    public void filter(Filter filter, boolean doContains) {
        filter(filter, doContains, false);
    }

    public void filter(Filter filter, boolean doContains, boolean skipSibling)
    {
        if (originalCellDataStore == null || originalRowDataStore == null) {
            return;
        }

        originalCellData = new ArrayList<>(originalCellDataStore);
        originalRowData = new ArrayList<>(originalRowDataStore);
        filteredCellList = new ArrayList<>();
        filteredRowList = new ArrayList<>();

        if (filter.getFilterItems().isEmpty()) {
            filteredCellList = new ArrayList<>(originalCellDataStore);
            filteredRowList = new ArrayList<>(originalRowDataStore);
        } else {
            for (int x = 0; x < filter.getFilterItems().size(); ++x) {
                final FilterItem filterItem = filter.getFilterItems().get(x);
                if (filterItem.getFilterType().equals(FilterType.ALL)) {
                    for (List<IFilterableModel> itemsList : originalCellData) {
                        for (IFilterableModel item : itemsList) {
                            if (item
                                    .getFilterableKeyword()
                                    .toLowerCase()
                                    .equals(filterItem
                                            .getFilter()
                                            .toLowerCase())) {
                                filteredCellList.add(itemsList);
                                filteredRowList.add(originalRowData.get(filteredCellList.indexOf(itemsList)));
                                break;
                            }
                        }
                    }
                } else {
                    for (List<IFilterableModel> itemsList : originalCellData) {
                        String s1 = itemsList.get(filterItem.getColumn()).getFilterableKeyword().toLowerCase();
                        String s2 = filterItem.getFilter().toLowerCase();
                        boolean pass = (doContains) ? s1.contains(s2) : s1.equals(s2);
                        if (pass) {
                            filteredCellList.add(itemsList);
                            filteredRowList.add(originalRowData.get(originalCellData.indexOf(itemsList)));
                        }
                    }
                }

                /*
                // If this is the last filter to be processed, the filtered lists will not be cleared.
                if (++x < filter.getFilterItems().size()) {
                    originalCellData = new ArrayList<>(filteredCellList);
                    originalRowData = new ArrayList<>(filteredRowList);
                    filteredCellList.clear();
                    filteredRowList.clear();
                }
                */
            }
        }

        // Sets the filtered data to the TableView.
        mRowHeaderRecyclerViewAdapter.setItems(filteredRowList, true);
        mCellRecyclerViewAdapter.setItems(filteredCellList, true);
        if (!skipSibling && sibling != null)
        {
            sibling.filter(filter, doContains, true);
        }
    }

    @SuppressWarnings("unchecked")
    private AdapterDataSetChangedListener adapterDataSetChangedListener =
            new AdapterDataSetChangedListener() {
                @Override
                public void onRowHeaderItemsChanged(List rowHeaderItems) {
                    if (rowHeaderItems != null) {
                        originalRowDataStore = new ArrayList<>(rowHeaderItems);
                    }
                }

                @Override
                public void onCellItemsChanged(List cellItems) {
                    if (cellItems != null) {
                        originalCellDataStore = new ArrayList<>(cellItems);
                    }
                }
            };
}

