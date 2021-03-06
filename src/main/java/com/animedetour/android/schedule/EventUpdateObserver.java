/*
 * This file is part of the Anime Detour Android application
 *
 * Copyright (c) 2014-2015 Anime Twin Cities, Inc.
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.animedetour.android.schedule;

import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.animedetour.android.model.Event;
import com.inkapplications.android.widget.listview.ItemAdapter;

import java.util.List;

import monolog.Monolog;
import rx.Observer;

/**
 * Listens to data updates of the event list for a single day.
 *
 * When an update to the events list is detected, this will notify the fragment
 * of the new data and handle any errors.
 *
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
public class EventUpdateObserver implements Observer<List<Event>>
{
    final private Monolog logger;

    /** View to display when there are no items in the list */
    final private View emptyView;

    /**
     * ProgressBar to indicate whether or not content is being loaded */
    final private ProgressBar loadingIndicator;

    /** The list view we're to put events into. */
    final private ListView panelList;

    final private ItemAdapter<PanelView, Event> itemAdapter;

    /**
     * Scroll position state.
     *
     * This is a temporary storage of the scroll position state for when the
     * scroll position is restored - since the list will likely  not yet have
     * data in it. Once data is updated, this should be the position to scroll
     * to.
     */
    private int scrollPosition = 0;

    /**
     * Whether the scroll position has been restored already.
     *
     * This allows us to check if we've already changed the scroll position
     * after receiving a data update. This way, if multiple data updates come
     * in, we won't move the list multiple times; which could be annoying to the
     * user.
     */
    private boolean restored = false;

    public EventUpdateObserver(
        ListView panelList,
        ItemAdapter<PanelView, Event> listAdapter,
        View emptyView,
        Monolog logger,
        ProgressBar loadingIndicator
    ) {
        this.panelList = panelList;
        this.itemAdapter = listAdapter;
        this.emptyView = emptyView;
        this.logger = logger;
        this.loadingIndicator = loadingIndicator;

    }

    @Override
    public void onCompleted()
    {
    }

    @Override
    public void onError(Throwable e)
    {
        this.logger.error("Error fetching schedule", e);
    }

    @Override
    public void onNext(List<Event> events)
    {
        if (this.loadingIndicator != null) {
            this.loadingIndicator.setVisibility(View.GONE);
        }
        this.displayEvents(events);
    }

    /**
     * Change the items displayed in the event list.
     *
     * If the list is empty, this will toggle an empty view to be displayed.
     *
     * @param events The list of events to display.
     */
    private void displayEvents(List<Event> events)
    {
        this.itemAdapter.setItems(events);
        this.toggleEmptyView(events.isEmpty());

        if (false == events.isEmpty()) {
            this.restoreState();
        }
    }

    /**
     * Show or hide the empty view visibility.
     *
     * @param isEmpty Whether the list is empty and we should show the empty view.
     */
    private void toggleEmptyView(boolean isEmpty)
    {
        if (isEmpty) {
            this.emptyView.setVisibility(View.VISIBLE);
        } else {
            this.emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Scroll to the previous item position if it has not already been restored.
     */
    private void restoreState()
    {
        if (this.restored) {
            return;
        }

        this.restored = true;
        this.panelList.setSelection(this.scrollPosition);
    }

    /**
     * Get the current position of the event list view.
     *
     * @return The first visible item position in the list.
     */
    final public int getScrollPosition()
    {
        return this.panelList.getFirstVisiblePosition();
    }

    /**
     * Scrolls the list to an item position.
     *
     * This retains the scroll position so that if the list is updated, it will
     * return to that position.
     *
     * @param scrollPosition the position to scroll to.
     */
    public void setScrollPosition(int scrollPosition)
    {
        this.panelList.smoothScrollToPosition(scrollPosition);
        this.scrollPosition = scrollPosition;
    }
}
