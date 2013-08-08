package com.nimbostrati.volleytest;


import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private String TAG = this.getClass().getSimpleName();
    private ListView lstView;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private ArrayList<NewsModel> arrNews ;
    private LayoutInflater lf;
    private VolleyAdapter va;
    private ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lf =LayoutInflater.from(this);

        arrNews = new ArrayList<NewsModel>();
        va = new VolleyAdapter();

        lstView = (ListView) findViewById(R.id.listView);
        lstView.setAdapter(va);

        mRequestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(10));

        //Use yahoo which connects to Rotten Tomatoes' Top Ten

        String url = "http://pipes.yahoo.com/pipes/pipe.run?_id=23ff425a7320cebd218fcc8cd194b256&_render=json";
        pd = ProgressDialog.show(this,"Please Wait..", "Please Wait...");
        try{
            Thread.sleep(2000);
        }catch(Exception e) {

        }

        JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET,url,null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG,response.toString());
                parseJSON(response);
                va.notifyDataSetChanged();
                pd.dismiss();
            }

        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,error.getMessage());
            }
        });

        mRequestQueue.add(jr);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private void parseJSON(JSONObject json){
        try{
            JSONObject value = json.getJSONObject("value");
            JSONArray items = value.getJSONArray("items");
            for(int i=0;i<items.length();i++){
                JSONObject item = items.getJSONObject(i);
                NewsModel nm = new NewsModel();
                nm.setTitle(item.optString("title"));
                nm.setDescription(item.optString("critics_consensus"));
                nm.setLink(item.optString("id"));
                JSONObject posters = item.getJSONObject("posters");
                nm.setImgUrl(posters.optString("thumbnail"));
                nm.setPubDate(item.optString("published"));
                arrNews.add(nm);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }
    class NewsModel{
        private String title;
        private String link;
        private String imgurl;
        private String description;
        private String pubDate;
        void setTitle(String title) {
            this.title = title; //title.replaceFirst("^\\d+% ","");
        }
        void setLink(String link) {
            this.link = link;
        }
        void setImgUrl(String imgurl) {
            this.imgurl = imgurl;
        }
        void setDescription(String description) {
            this.description = description; //description.replaceAll("em>","b>");
        }
        void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }
        String getLink() {
            return link;
        }
        String getImgUrl() {
            return imgurl;
        }
        String getDescription() {
            return description;
        }
        String getPubDate() {
            return pubDate;
        }
        String getTitle() {

            return title;
        }
    }
    class VolleyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return arrNews.size();
        }
        @Override
        public Object getItem(int i) {
            return arrNews.get(i);
        }
        @Override
        public long getItemId(int i) {
            return 0;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder vh ;
            if(view == null){
                vh = new ViewHolder();
                view = lf.inflate(R.layout.row_listview,null);
                vh.tvThumb = (NetworkImageView) view.findViewById(R.id.imageView);
                vh.tvTitle = (TextView) view.findViewById(R.id.txtTitle);
                vh.tvDesc = (TextView) view.findViewById(R.id.txtDesc);
                vh.tvDate = (TextView) view.findViewById(R.id.txtDate);
                view.setTag(vh);
            }
            else{
                vh = (ViewHolder) view.getTag();
            }

            NewsModel nm = arrNews.get(i);
            vh.tvThumb.setImageUrl(nm.getImgUrl(),mImageLoader);
            vh.tvTitle.setText(nm.getTitle());
            vh.tvDesc.setText(Html.fromHtml(nm.getDescription()));
            vh.tvDate.setText(nm.getPubDate());
            return view;
        }

        class  ViewHolder{
            NetworkImageView tvThumb;
            TextView tvTitle;
            TextView tvDesc;
            TextView tvDate;
        }
    }

    public class LruBitmapCache extends LruCache<String, Bitmap> implements ImageCache {

        public LruBitmapCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }

        @Override
        public Bitmap getBitmap(String url) {
            return get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(url, bitmap);
        }

    }

    }
