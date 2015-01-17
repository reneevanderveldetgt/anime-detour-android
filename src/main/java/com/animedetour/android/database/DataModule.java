/*
 * This file is part of the Anime Detour Android application
 *
 * Copyright (c) 2014-2015 Anime Twin Cities, Inc. All rights Reserved.
 */
package com.animedetour.android.database;

import android.app.Application;
import com.animedetour.api.guest.GuestEndpoint;
import com.animedetour.api.guest.model.Category;
import com.animedetour.api.guest.model.Guest;
import com.animedetour.api.sched.api.ScheduleEndpoint;
import com.animedetour.api.sched.api.model.Event;
import com.inkapplications.prism.SubscriptionManager;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import dagger.Module;
import dagger.Provides;
import org.apache.commons.logging.Log;

import javax.inject.Singleton;
import java.sql.SQLException;

@Module(library = true, complete = false)
final public class DataModule
{
    @Provides @Singleton EventRepository provideEventRepository(
        DetourDatabaseHelper helper,
        ScheduleEndpoint remote,
        Log logger
    ) {
        ConnectionSource connectionSource = new AndroidConnectionSource(helper);
        SubscriptionManager<Event> subscriptionManager = new SubscriptionManager<>(logger);

        try {
            Dao<Event, String> local = DaoManager.createDao(connectionSource, Event.class);
            return new EventRepository(local, remote, subscriptionManager, logger);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides @Singleton GuestRepository provideGuestRepository(
        DetourDatabaseHelper helper,
        GuestEndpoint remote,
        Log logger
    ) {
        ConnectionSource connectionSource = new AndroidConnectionSource(helper);
        SubscriptionManager<Category> subscriptionManager = new SubscriptionManager<>(logger);

        try {
            Dao<Category, Integer> localCategory = DaoManager.createDao(connectionSource, Category.class);
            Dao<Guest, String> localGuest = DaoManager.createDao(connectionSource, Guest.class);
            return new GuestRepository(
                connectionSource,
                localCategory,
                localGuest,
                remote,
                subscriptionManager,
                logger
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides @Singleton DetourDatabaseHelper provideHelper(
        Application context
    ) {
        return new DetourDatabaseHelper(context);
    }
}
