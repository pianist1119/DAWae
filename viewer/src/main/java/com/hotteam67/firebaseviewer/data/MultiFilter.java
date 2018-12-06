package com.hotteam67.firebaseviewer.data;

import android.text.TextUtils;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.filter.Filter;
import com.evrencoskun.tableview.filter.FilterItem;
import com.evrencoskun.tableview.filter.FilterType;
import com.hotteam67.firebaseviewer.tableview.MultiFilterTableView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
A total rebuild of the filter class which allows multiple terms
 */

public class MultiFilter extends Filter {
    private List<FilterItem> filterItems = new ArrayList<>();
    private ITableView tableView;

    public MultiFilter(ITableView view)
    {
        super(view);
        tableView = view;
    }

    // Modified, nothing is protected so we need to redeclare and overwrite previous stuff
    @Override
    public List<FilterItem> getFilterItems()
    {
        return filterItems;
    }


    // Modified, no longer updates, just adds to the end/removes all existing
    public void set(int column, String filter, boolean doContains) {
        final FilterItem filterItem = new FilterItem(
                column == -1 ? FilterType.ALL : FilterType.COLUMN,
                column,
                filter
        );

        if (isAlreadyFiltering(column, filterItem)) {
            if (TextUtils.isEmpty(filter)) {
                //remove(column, filterItem);
            } else {
                add(filterItem);
            }
        } else if (!TextUtils.isEmpty(filter)) {
            add(filterItem);
        }
        if (tableView instanceof MultiFilterTableView)
            ((MultiFilterTableView) tableView).filter(this, doContains);
        else
            tableView.filter(this);
        tableView.hideColumn(0);
    }

    /**
     * Adds new filter item to the list of this class.
     *
     * @param filterItem The filter item to be added to the list.
     */
    private void add(FilterItem filterItem) {
        filterItems.add(filterItem);
    }

    public void removeFilter(int column)
    {
        if (filterItems.size() == 0) return;
        for (Iterator<FilterItem> filterItemIterator = filterItems.iterator(); filterItemIterator.hasNext();)
        {
            final FilterItem item = filterItemIterator.next();
            if (column == item.getColumn()) filterItemIterator.remove();
        }
        tableView.filter(this);
    }

    // Modified, should now iterate and take out ALL matching filters
    private void remove(int column, FilterItem filterItem) {
        // This would remove a FilterItem from the Filter list when the filter is cleared.
        // Used Iterator for removing instead of canonical loop to prevent ConcurrentModificationException.
        for (Iterator<FilterItem> filterItemIterator = filterItems.iterator(); filterItemIterator.hasNext(); ) {
            final FilterItem item = filterItemIterator.next();
            if (column == -1 && item.getFilterType().equals(filterItem.getFilterType())) {
                filterItemIterator.remove();
            } else if (item.getColumn() == filterItem.getColumn()) {
                filterItemIterator.remove();
            }
        }
        tableView.filter(this);
    }

    /**
     * Method to check if a filter item is already added based on the column to be filtered.
     *
     * @param column     The column to be checked if the list is already filtering.
     * @param filterItem The filter item to be checked.
     * @return True if a filter item for a specific column or for ALL is already in the list.
     */
    private boolean isAlreadyFiltering(int column, FilterItem filterItem) {
        // This would determine if Filter is already filtering ALL or a specified COLUMN.
        for (FilterItem item : filterItems) {
            if (column == -1 && item.getFilterType().equals(filterItem.getFilterType())) {
                return true;
            } else if (item.getColumn() == filterItem.getColumn()) {
                return true;
            }
        }
        return false;
    }
}
