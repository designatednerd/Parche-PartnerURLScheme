package com.parche.partnerurlschemesample;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class StagingDiscountGenerator extends AsyncTask<Void, Void, String>   {


    public interface StagingDiscountListener {
        void gotDiscount(String aDiscount, String aAPIKey, String aUsername);
        void errorGettingDiscount(String errorDescription);
    }


    private static final String STAGING_BASE_URL = "https://api-staging.goparche.com/";
    private static final String DISCOUNT_REQUEST_ENDPOINT = "v1/partner/%s/create_discount/";
    private static final String API_KEY = "kLd67mG8";
    private static final String FAKE_USER_ID = "qa_test@example.com";

    private static final String PARTNER_USER_ID_KEY = "partner_user_id";
    private static final String API_SECRET_KEY = "api_secret";
    private static final String DISCOUNT_CODE_KEY = "discount_code";


    private StagingDiscountListener mListener;

    public StagingDiscountGenerator(StagingDiscountListener aListener) {
        mListener = aListener;
    }

    @Override
    protected String doInBackground(Void... params) {

        String urlString = STAGING_BASE_URL + String.format(DISCOUNT_REQUEST_ENDPOINT, API_KEY);
        HttpPost postRequest = new HttpPost(urlString);
        HttpClient client = new DefaultHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put(PARTNER_USER_ID_KEY, FAKE_USER_ID);
            json.put(API_SECRET_KEY, "LSnRMHhNqMsqvNekAG3M8qDjnRMfuBD8xGaLVX5BeyJCyUB4");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        if (stringEntity != null) {
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            postRequest.setEntity(stringEntity);
            try {
                HttpResponse response = client.execute(postRequest);
                String responseString = EntityUtils.toString(response.getEntity());
                if (responseString != null) {
                    JSONObject returnedJSON = new JSONObject(responseString);
                    String discount = returnedJSON.optString(DISCOUNT_CODE_KEY);
                    if (discount.length() > 0) {
                        return discount;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(String aDiscount) {
        super.onPostExecute(aDiscount);

        if (aDiscount == null) {
            mListener.errorGettingDiscount("No discount found!");
        } else {
            mListener.gotDiscount(aDiscount, API_KEY, FAKE_USER_ID);
        }
    }
}
