package com.zu.sweetalbum.util.rxbus;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by zu on 17-6-9.
 */

public class RxBus {
    private static RxBus instance = null;
//    private FlowableProcessor<Object> mBus;
    private final Subject<Object> mBus;
    private RxBus()
    {
//        mBus = PublishProcessor.create().toSerialized();
        mBus = PublishSubject.create().toSerialized();
    }

    public static RxBus getInstance()
    {
        if(instance == null)
        {
            instance = new RxBus();
        }
        return instance;
    }

    public void post(Object object)
    {
        mBus.onNext(object);
    }

    public Observable<Object> toObservable()
    {
//        return mBus.toObservable();
        return mBus;
    }


}
