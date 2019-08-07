package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.multi_region.MultiRegionSupport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RenderRSSFeeds implements Runnable {
   private Context context;
   private WeakReference<DisplayLocalFolderAds> actReference;
   private HashMap<String,Integer> deviceInfo;
   private RelativeLayout parentLayout;
   private Handler handler;

   private int ids = 0;

    public RenderRSSFeeds(Context context,DisplayLocalFolderAds activity,
                          Handler handler)
    {
        this.context = context;
        actReference = new WeakReference(activity);
        deviceInfo  = new DeviceModel().getDeviceProperties(activity);
        this.parentLayout = actReference.get().findViewById(R.id.feeds_parent);
        this.handler = handler;

    }
    
    public void run()
    {
        //private ArrayList<>
        //get rss feeds
        Cursor cursor = CampaignsDBModel.getRSSFeeds(context);
        if(cursor!=null && cursor.moveToFirst())
        {


           parentLayout.setVisibility(View.VISIBLE);


            //has rss feeds
            do {

               if(isActivityOn()) {
                   String info = cursor.getString(cursor.getColumnIndex(CampaignsDBModel.RSS_FEED_INFO));
                   long campaignId = cursor.getLong(cursor.getColumnIndex(CampaignsDBModel.RSS_FEED_CAMPAIGN_SERVER_ID));
                   try {
                       actReference.get().runningFeeds.add(campaignId);

                       JSONObject infoObject = new JSONObject(info);
                       JSONArray regions = infoObject.getJSONArray("regions");
                       for (int i = 0; i < regions.length(); i++) {
                           JSONObject feedObj = regions.getJSONObject(i);

                           handler.post(new CreateRssRegion(feedObj.getInt("width"), feedObj.getInt("height"),
                                   feedObj.getInt("left_margin"), feedObj.getInt("top_margin"), ids,
                                   (feedObj.has("bg_color")?feedObj.getString("bg_color"):"transperant")));

                           actReference.get().rssFeedsTimer.scheduleAtFixedRate(new RetrieveFeeds(ids, feedObj.getString("media_name"), feedObj.getLong("refresh_interval"),
                                   (feedObj.has("rss_text_color") ? feedObj.getString("rss_text_color") : "#000000"),
                                   campaignId,(feedObj.has("rss_text_size") ? feedObj.getInt("rss_text_size") : 15)),
                                   0, 30000);

                           ++ids;


                       }

                   } catch (JSONException ex) {
                       ex.printStackTrace();
                   }catch(Exception ex)
                   {
                       ex.printStackTrace();
                   }
               }else
               {
                   break;
               }
            }while(cursor.moveToNext());
        }

        //refresh rss feeds task
        if(isActivityOn() && actReference.get().rssFeedsTimer!=null)
        {
            actReference.get().rssFeedsTimer.scheduleAtFixedRate(new RefreshFeeds(),0,30000);//every minute
        }

    }

    private class CreateRssRegion implements Runnable
    {
        private int width,height,leftMargin,topMargin;
        private int feedViewId;
        private String bgColor;

        public CreateRssRegion(int width,int height,int leftMargin,int topMargin,int feedViewId,String bgColor)
        {
          this.width = width;
          this.height = height;
          this.leftMargin = leftMargin;
          this.topMargin = topMargin;
          this.feedViewId = feedViewId;
          this.bgColor = bgColor;
        }

        public void run()
        {

            RelativeLayout.LayoutParams parentLayoutParams = new RelativeLayout.LayoutParams(MultiRegionSupport.calculateRequiredPixel(deviceInfo.get("width"), width),
                    MultiRegionSupport.calculateRequiredPixel(deviceInfo.get("height"), height));

            final RecyclerView rss = new RecyclerView(context);
            rss.setId(feedViewId);
            if(!bgColor.equals("transperant"))
            {
                rss.setBackgroundColor(Color.parseColor(bgColor));
            }

            rss.setLayoutParams(parentLayoutParams);

            //adding margins
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) rss.getLayoutParams();
            marginLayoutParams.leftMargin = MultiRegionSupport.calculateRequiredPixel(deviceInfo.get("width"), leftMargin);
            marginLayoutParams.topMargin = MultiRegionSupport.calculateRequiredPixel(deviceInfo.get("height"), topMargin);

           if(parentLayout!=null)
           {
               if(parentLayout.getVisibility()==View.GONE)
               {
                   parentLayout.setVisibility(View.VISIBLE);
               }

               parentLayout.addView(rss);

           }

        }
    }

    private class RetrieveFeeds extends TimerTask
    {
        private int viewId;
        private String urlString,textColor;
        private long refreshDuration,campaignId;
        private int status =-1 ; //-1 is initializing
        private int totalFeeds = 0;
        private boolean isMultiple=false,totalFeedsDisplayed=false;
        private int currentDisplayUrlPosition=0,textSize=15;
        private long feedsloadedTimeInMs = 0;


        public RetrieveFeeds(int viewId,String urlString,long refreshDuration,String textColor,long campaignId,int textSize)
        {
            this.viewId = viewId;
            this.urlString = urlString;
            this.refreshDuration = refreshDuration;
            this.textColor = textColor;
            this.campaignId = campaignId;
            this.textSize = textSize;


            //check whether it is multiple feeds
            isMultiple = (urlString!=null && (urlString.split(context.getString(R.string.multi_feed_split))).length>=2);


        }



        public void run()
        {

          if(isActivityOn()) {
              if(actReference.get().runningFeeds.contains(campaignId)) {
                  if (status == -1) {

                      //start fetching feeds
                      if (isMultiple) {
                          fetchFeeds(urlString.split(context.getString(R.string.multi_feed_split))[0]);
                      } else {
                          fetchFeeds(urlString);
                      }

                  } else if (status == 1) {
                      if (isMultiple && totalFeedsDisplayed) {
                          //display next url feeds
                          ++currentDisplayUrlPosition;
                          String[] multiFeedUrls = urlString.split(context.getString(R.string.multi_feed_split));
                          if (multiFeedUrls.length <= currentDisplayUrlPosition) {
                              currentDisplayUrlPosition = 0;
                          }

                          fetchFeeds(multiFeedUrls[currentDisplayUrlPosition]);
                      } else if (feedsloadedTimeInMs != 0 &&
                              (TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - feedsloadedTimeInMs)) >= refreshDuration)) {
                          //refresh the current url
                          fetchFeeds(urlString);
                      }else
                      {
                          RecyclerView mRecyclerView = actReference.get().findViewById(viewId);
                          if(mRecyclerView.canScrollVertically(1) && mRecyclerView.getScrollState()==RecyclerView.SCROLL_STATE_IDLE)
                          {
                              mRecyclerView.smoothScrollToPosition((totalFeeds-1));
                          }
                      }

                  } else if (status == -2) {
                      //error in fetching feeds
                      if (isMultiple) {
                          fetchFeeds(urlString.split(context.getString(R.string.multi_feed_split))[currentDisplayUrlPosition]);
                      } else {
                          fetchFeeds(urlString);
                      }
                  }
              }else
              {
                  //feed has been removed or skipped
                  //delete the view
                  deleteRssFeed(viewId);

                  //cancel the alarm
                  this.cancel();

              }
          }else
          {
              //cancel the alarm
              this.cancel();
          }

        }

        private void deleteRssFeed(final int viewId)
        {
            if(isActivityOn()&& handler!=null)
            {
               handler.post(new Runnable() {
                   @Override
                   public void run() {
                       RecyclerView recyclerView = actReference.get().findViewById(viewId);
                       if(recyclerView!=null)
                       {
                           ((ViewGroup)recyclerView.getParent()).removeView(recyclerView);
                       }
                   }
               });
            }


        }

        private void fetchFeeds(String urlString)
        {
            try {
                status=0;//fetching feeds
                totalFeedsDisplayed=false;//re initialize to false
                feedsloadedTimeInMs = 0;

                URL url = new URL(urlString);
                InputStream inputStream = url.openConnection().getInputStream();

                //process feeds
                final List<RssFeedModel> feeds = parseFeed(inputStream);
                totalFeeds=feeds.size();
                if(isActivityOn())
                {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            RecyclerView mRecyclerView = actReference.get().findViewById(viewId);

                            mRecyclerView.clearOnScrollListeners();

                            LinearLayoutManager mLayoutManager = new SpeedyLinearLayoutManager(context, SpeedyLinearLayoutManager.VERTICAL, false);
                            mRecyclerView.setLayoutManager(mLayoutManager);

                            mRecyclerView.setAdapter(new RenderRSSFeedAdapter(feeds,textColor,textSize));

                            mRecyclerView.getAdapter().notifyDataSetChanged();


                            mRecyclerView.smoothScrollToPosition((totalFeeds-1));

                            feedsloadedTimeInMs = System.currentTimeMillis();

                            setRefreshViewScrollListeners(mRecyclerView);



                        }
                    });

                    status = 1;

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
                status=-2;


            }catch(IOException ex)
            {
                ex.printStackTrace();
                status=-2;
            }catch(XmlPullParserException ex)
            {
                ex.printStackTrace();
                status=-2;
            }catch(Exception e)
            {
                e.printStackTrace();
                status=-2;
            }
        }

        private void setRefreshViewScrollListeners(RecyclerView mRecyclerView)
        {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                      Log.d("RssFeeds","Inside RenderRSSFeeds on scroll state"+newState);
                    if(!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE)
                    {
                        Log.d("RssFeeds","Inside RenderRSSFeeds on scroll state scrolling finish");
                        //bottom reached

                        recyclerView.scrollToPosition(0);
                        recyclerView.smoothScrollToPosition((totalFeeds-1));

                        totalFeedsDisplayed=true;
                    }
                }
            });

        }

        public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
                IOException {
            String title = null;
            String link = null;
            String description = null;
            boolean isItem = false;
            List<RssFeedModel> items = new ArrayList<>();

            try {
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                xmlPullParser.setInput(inputStream, null);

                xmlPullParser.nextTag();
                while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                    int eventType = xmlPullParser.getEventType();

                    String name = xmlPullParser.getName();
                    if(name == null)
                        continue;

                    if(eventType == XmlPullParser.END_TAG) {
                        if(name.equalsIgnoreCase("item")) {
                            isItem = false;
                        }
                        continue;
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if(name.equalsIgnoreCase("item")) {
                            isItem = true;
                            continue;
                        }
                    }


                    String result = "";
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        result = xmlPullParser.getText();
                        xmlPullParser.nextTag();
                    }

                    if(isItem)
                    {
                        if (name.equalsIgnoreCase("title")) {
                            title = result;
                        } else if (name.equalsIgnoreCase("link")) {
                            link = result;
                        } else if (name.equalsIgnoreCase("description")) {
                            description = result;
                        }
                    }


                    if (title != null && link != null && description != null) {


                        items.add(new RssFeedModel(title));
                        items.add(new RssFeedModel(description));

                        title = null;
                        link = null;
                        description = null;
                        isItem = false;
                    }
                }

                return items;
            } finally {
                inputStream.close();
            }
        }
    }

    private class RssFeedModel {

        private String title;
        private String description;

        public RssFeedModel(String title) {
            this.title = title;
           // this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription()
        {
            return description;
        }
    }

    public class RenderRSSFeedAdapter extends RecyclerView.Adapter<RenderRSSFeedAdapter.ViewHolder> {
        private List<RssFeedModel> feeds;
        private String textColor;
        private int textSize;
        public RenderRSSFeedAdapter(List<RssFeedModel> feeds,String textColor,int textSize)
        {
            this.feeds = feeds;
            this.textColor = textColor;
            this.textSize = textSize;
        }
        public class ViewHolder extends RecyclerView.ViewHolder
        {
            private TextView titleTextView;


            public ViewHolder(View view)
            {
                super(view);

                titleTextView = view.findViewById(R.id.feed_title);
                titleTextView.setTextColor(Color.parseColor(textColor));


            }
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int type)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rss_feed_support,parent,false);
            return new RenderRSSFeedAdapter.ViewHolder(view);
        }

        public void onBindViewHolder(RenderRSSFeedAdapter.ViewHolder viewHolder, int position)
        {
            RssFeedModel feed = feeds.get(position);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                viewHolder.titleTextView.setText(Html.fromHtml(feed.getTitle(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                viewHolder.titleTextView.setText(Html.fromHtml(feed.getTitle()));
            }

            viewHolder.titleTextView.setTextSize(textSize);


        }

        public int getItemCount()
        {
            return feeds.size();
        }
    }


    private class SpeedyLinearLayoutManager extends LinearLayoutManager {

        private static final float MILLISECONDS_PER_INCH = 10000f; //default is 25f (bigger = slower)



        public SpeedyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

            final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                @Override
                public PointF computeScrollVectorForPosition(int targetPosition) {
                    return super.computeScrollVectorForPosition(targetPosition);
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };

            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }



    }

    private boolean isActivityOn()
    {
        return (actReference!=null && actReference.get()!=null && actReference.get().isServiceRunning);
    }


    private class RefreshFeeds extends TimerTask
    {


        public void run()
        {

            if(isActivityOn()&& actReference.get().rssFeedsTimer!=null && handler!=null )
            {
                DisplayLocalFolderAds act = actReference.get();
                //get feeds
                Cursor activeFeedsCursor = CampaignsDBModel.getRSSFeeds(context);

                if(activeFeedsCursor!=null && activeFeedsCursor.moveToNext())
                {


                    ArrayList<Long> tempActiveFeeds = new ArrayList<>(activeFeedsCursor.getCount());
                   do {
                       long campaignId = activeFeedsCursor.getLong(activeFeedsCursor.getColumnIndex(CampaignsDBModel.RSS_FEED_CAMPAIGN_SERVER_ID));

                       if(!act.runningFeeds.contains(campaignId))
                       {
                           try
                           {
                           String info = activeFeedsCursor.getString(activeFeedsCursor.getColumnIndex(CampaignsDBModel.RSS_FEED_INFO));


                           JSONObject infoObject = new JSONObject(info);
                           JSONArray regions = infoObject.getJSONArray("regions");
                           for (int i = 0; i < regions.length(); i++) {
                               JSONObject feedObj = regions.getJSONObject(i);

                               handler.post(new CreateRssRegion(feedObj.getInt("width"), feedObj.getInt("height"),
                                       feedObj.getInt("left_margin"), feedObj.getInt("top_margin"), ids,
                                       (feedObj.has("bg_color")?feedObj.getString("bg_color"):"transperant")));

                               actReference.get().rssFeedsTimer.scheduleAtFixedRate(new RetrieveFeeds(ids, feedObj.getString("media_name"), feedObj.getLong("refresh_interval"),
                                       (feedObj.has("rss_text_color") ? feedObj.getString("rss_text_color") : "#000000"),
                                       campaignId,(feedObj.has("rss_text_size") ? feedObj.getInt("rss_text_size") : 15)),
                                       0, 30000);

                               ++ids;
                           }

                               //new feed
                               actReference.get().runningFeeds.add(campaignId);


                           }catch(Exception e)
                           {
                               e.printStackTrace();
                           }
                       }

                       tempActiveFeeds.add(campaignId);
                   }while(activeFeedsCursor.moveToNext());

                   //delete removed feeds
                   for(Long feed:act.runningFeeds)
                   {
                       if(!tempActiveFeeds.contains(feed))
                       {
                           act.runningFeeds.remove(feed);
                       }
                   }

                }else
                {
                    //no feeds found ,, delete all running feeds
                    actReference.get().runningFeeds.clear();
                }
            }else
            {
                this.cancel();
            }

        }
    }

}
