package com.walter.brainy;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1999;
    String URL_GET_TOKEN="http://10.0.2.2:8000/api/get/token";
    String URL_CHECK_OUT="http://10.0.2.2:8000/api/braintree/checkout";
    String TAG="DATA_FROM_SERVER";
    String TOKEN="";
    SweetAlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getToken();
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressDialog.setTitleText("Processing Your Payment");
        progressDialog.setCancelable(false);
    }

    public void btnCheckOut(View view) {
         if (TOKEN.isEmpty()){
             getToken();
         }
        DropInRequest dropInRequest=new DropInRequest().clientToken(TOKEN);
        startActivityForResult(dropInRequest.getIntent(this),REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==REQUEST_CODE){
            if (resultCode==RESULT_OK){
                DropInResult result=data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce= result.getPaymentMethodNonce();
                String strNounce=nonce.getNonce();
                String amount ="10";
                sendPayments(amount,strNounce );

            }else if(resultCode==RESULT_CANCELED){
                Toast.makeText(this, "User canceled", Toast.LENGTH_SHORT).show();

            }else{
                Exception error=(Exception)data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("Err",error.toString());
            }
        }
    }

    private void sendPayments(String amount, String strNounce) {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("amount",amount);
            jsonObject.put("nonce",strNounce);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.show();
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, URL_CHECK_OUT, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: " + response.toString());
                try {
                    if (response.getBoolean("success")){
                        Toast.makeText(MainActivity.this, "Payment Was successful", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Payment Did not go through", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                Log.e(TAG, "onErrorResponse: " + new String(error.networkResponse.data));
            }
        });
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 3000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 5;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Log.d(TAG, "retry: " + error.getMessage());
            }
        });
        MyNetworkSingleTon.getInstance(getApplicationContext()).addToRequestQueue(request);

    }
    private void getToken() {
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, URL_GET_TOKEN, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "onResponse: " + response.toString());
                try {
                    TOKEN=response.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                Log.e(TAG, "onErrorResponse: " + new String(error.networkResponse.data));
            }
        });
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 3000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 5;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Log.d(TAG, "retry: " + error.getMessage());
            }
        });
        MyNetworkSingleTon.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


}
