package com.walter.brainy;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by walter on 2/7/19.
 */

public class MyNetworkSingleTon {
    private static MyNetworkSingleTon instance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;


    private MyNetworkSingleTon(Context context){
        mCtx=context;
        mRequestQueue=getRequestQueue();
    }

    public static synchronized MyNetworkSingleTon getInstance(Context context) {
        if (instance ==null){
            instance =new MyNetworkSingleTon(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue==null){
            mRequestQueue= Volley.newRequestQueue(mCtx);
        }
        return  mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    //MyNetworkSingleTon.getInstance(this).addToRequestQueue(stringRequest);
    /*RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
    getRequestQueue();*/

}