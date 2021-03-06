package com.jeff.shareapp.net;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jeff.shareapp.ResponseEntivity.MyResponse;
import com.jeff.shareapp.model.UserinfoModel;
import com.jeff.shareapp.util.FormatUtil;
import com.jeff.shareapp.core.MyApplication;
import com.jeff.shareapp.util.StaticFlag;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 张武 on 2016/5/28.
 */

public class MyVolley<T> {

    public static RequestQueue requestQueue;

    private StringRequest mRequest;
    private Context context;

    private static MyVolley mMyVolley;
    public final Gson gson = FormatUtil.getFormatGson();

    public static MyVolley getMyVolley() {
        if (mMyVolley == null)
            mMyVolley = new MyVolley();
        return mMyVolley;
    }

    public MyVolley() {

    }

    /**
     *
     * @param url
     * @param params
     * @param mListener
     */
    @Deprecated
    public MyVolley(String url, final HashMap<String, String> params, final MyVolleyListener mListener) {
        //打印请求地址

        UserinfoModel u = MyApplication.getMyApplication().getDataPref().getLocalData(UserinfoModel.class);
        String token=null;
        if (u != null)
            token = u.getToken();
        token = token==null?"":token;
        params.put("token", token);

        Log.i("jeff", "MyVolley请求地址：  " + url);
        Log.i("jeff", "MyVolley参数：" + gson.toJson(params));


        mRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("jeff", "返回参数：  " + response);
                        MyResponse myResponse = gson.fromJson(response, new TypeToken<MyResponse<Object>>() {
                        }.getType());

                        if (myResponse.getStatus() == 200) {
                            mListener.onSuccess(myResponse.getResult());
                        } else
                            mListener.onFailure(myResponse.getStatus(), myResponse.getMessage());
                        //token过期
                        if (myResponse.getStatus() == StaticFlag.TOKEN_EXPIRE) {
                            //发送广播告诉apptoken过期
                            context = MyApplication.getMyApplication().getApplicationContext();
                            context.sendBroadcast(new Intent().putExtra("msg", StaticFlag.TOKEN_EXPIRE).setAction("com.jeff.token_expire"));
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListener.onError(-1, "网络超时！");
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

        };
        //超时时间20s 重试次数0
        mRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(MyApplication.getMyApplication());

        requestQueue.add(mRequest);
    }

    public void addStringRequest(final Type mType, String url, final HashMap<String, String> params, final MyVolleyListener<T> mListener) {


        UserinfoModel u = MyApplication.getMyApplication().getDataPref().getLocalData(UserinfoModel.class);
        String token=null;
        if (u != null)
            token = u.getToken();
        token=token==null?"":token;
        params.put("token", token);

        //打印请求地址
        Log.i("jeff", "MyVolley请求地址：  " + url);
        Log.i("jeff", "MyVolley参数：" + gson.toJson(params));
        mRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("jeff", "返回参数：  " + response);
                        MyResponse myResponse = gson.fromJson(response, new TypeToken<MyResponse<Object>>() {
                        }.getType());

                        if (myResponse.getStatus() == 200) {
                            Gson gson = FormatUtil.getFormatGson();
                            String jsonResult = gson.toJson(myResponse.getResult());
                            mListener.onSuccess((T) gson.fromJson(jsonResult, mType));
                        }else
                            mListener.onFailure(myResponse.getStatus(), myResponse.getMessage());

                        if (myResponse.getStatus() == StaticFlag.TOKEN_EXPIRE) {
                            //发送广播告诉apptoken过期
                            context = MyApplication.getMyApplication().getApplicationContext();
                            context.sendBroadcast(new Intent().putExtra("msg", StaticFlag.TOKEN_EXPIRE).setAction("com.jeff.token_expire"));
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListener.onError(-1, "网络异常！");
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

        };
        //超时时间20s 重试次数0
        mRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(MyApplication.getMyApplication());

        requestQueue.add(mRequest);
    }


}
