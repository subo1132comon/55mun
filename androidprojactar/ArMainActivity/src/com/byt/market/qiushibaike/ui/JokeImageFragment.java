package com.byt.market.qiushibaike.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.HtmlTVpalyActivity;
import com.byt.market.activity.JokeDetailsWebViewActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.ZoomAndRounderBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.qiushibaike.JokeActivity;
import com.byt.market.qiushibaike.JokeCommentActivity;
import com.byt.market.qiushibaike.PlayGifActivity;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.CommentFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.NotifaHttpUtil;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CusPullListView.OnRefreshListener;
import com.byt.market.view.gifview.GifDecoderView;
import com.umeng.analytics.MobclickAgent;

public class JokeImageFragment extends ListViewFragment{
	private static final String TAG = "JokeTextImageFragment";
	String netType;
	private DisplayImageOptions mOptions;
	HashMap<Integer, String> mlist=new HashMap<Integer, String>();
	HashMap<Integer, String> murllist=new HashMap<Integer, String>();
	private TextView joke_comment_count;
	private TextView joke_collect_count;
	private TextView joke_share_count;
	private ImageAdapter  adapter;

	private int REQUEST_REFRESH = 0;
	private int comm_sid;
	private int refreshCount;
	private boolean isjok;
	
//	public JokeImageFragment(boolean b){
//		this.isjok = b;
//	}
	public void setISjoke(boolean b){
		this.isjok = b;
	}
	@Override
	protected String getRequestUrl() {
		
		LogCart.Log("jjjjjjjjjjjj------"+getPageInfo().getNextPageIndex());
    	return Constants.APK_URL+"Joke/v1.php?qt=Jokelist&cid=2"+"&pi="
		    + getPageInfo().getNextPageIndex() + "&ps="
		    + getPageInfo().getPageSize();
//		return Constants.APK_URL+"Joke/v1.php?qt=Jokelist&cid=2"+"&pi="
//	    + getPageInfo().getNextPageIndex() + "&ps=1"
//	   ;
    	
	}
	@Override
    public void canRequestGet() {
		request();
    }
	@Override
	protected String getRequestContent() {		
		return null;
	}
	
	@Override
	public void onAppClick(Object obj, String action) {
		
		//Intent intent = new Intent(action);
		Intent intent = new Intent(getActivity(),JokeDetailsWebViewActivity.class);
		if (obj instanceof CateItem) {
			CateItem caItem = (CateItem) obj;	
			
			Bundle bundle = new Bundle();
			//bundle.putString("joke_content", caItem.content);
			//bundle.putString("joke_image_path", caItem.ImagePath);
			//bundle.putInt("isshare", caItem.isshare);
			//bundle.putInt("isshare", caItem.id);
			bundle.putInt(("msid"), caItem.sid);
			bundle.putString("type", "joke");
			//bundle.putString("title", caItem.cTitle);
			//bundle.putString("time",caItem.publish_time);
			intent.putExtras(bundle);			
		}			
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);		
	}



	@Override
	protected List<BigItem> parseListData(JSONObject result) {
        try {       	
            return JsonParse.parseJokeList(getActivity(),result.getJSONArray("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.listview;
	}

    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	netType = Util.getNetAvialbleType(MyApplication.getInstance());
    	if(!TextUtils.isEmpty(netType)&&"wifi".equals(netType)){
    		//mImageFetcher.setLoadingImage(R.drawable.category_loading);
    	}
    	else
    	{
    		//mImageFetcher.setLoadingImage(R.drawable.joke_empty_icon);
    	}
    	mOptions = new DisplayImageOptions.Builder()
		.cacheOnDisc().build();
    }
    
	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) 
	{
        View view = inflater.inflate(res, container, false);
        return view;
	}
	
	@Override
	protected ImageAdapter createAdapter() 
	{
        return adapter = new JokeImageAdapter();
	}    



    private class JokeImageAdapter extends ImageAdapter{
		@Override
        public View newView(LayoutInflater inflater, BigItem item) {
            View view = null;
            BigHolder holder = new BigHolder();
            switch(item.layoutType){
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    view = inflater.inflate(R.layout.listitem_joke_image, null);
                    holder.layoutType = item.layoutType;
                    JokeItemHolder itemHolder = new JokeItemHolder();                    
                    itemHolder.content = (TextView) view.findViewById(R.id.jokecontent);    
                    itemHolder.image =   (ImageView) view.findViewById(R.id.jokeimage);  
                    itemHolder.isPaly_img = (ImageView) view.findViewById(R.id.is_paly_imageView);
                   itemHolder.gif_text = (ImageView) view.findViewById(R.id.gif_textV);
                    itemHolder.layout = (LinearLayout) view.findViewById(R.id.jokelistitem); 
                    itemHolder.comment = (LinearLayout) view.findViewById(R.id.joke_comment_btn);
                    itemHolder.collect = (LinearLayout) view.findViewById(R.id.joke_collect_btn);
                    itemHolder.share = (LinearLayout) view.findViewById(R.id.joke_share_btn);                      
                    itemHolder.comment_count_tv = (TextView)view.findViewById(R.id.joke_comment_count);
                    itemHolder.collect_count_tv = (TextView)view.findViewById(R.id.joke_collect_count);
                    itemHolder.share_count_tv = (TextView)view.findViewById(R.id.joke_share_count);
                    itemHolder.comment_img = (ImageView)view.findViewById(R.id.joke_comment_img);
                    itemHolder.collect_img = (ImageView)view.findViewById(R.id.joke_collect_img);
                    itemHolder.comment_layout=(FrameLayout) view.findViewById(R.id.imagelayout);
                    itemHolder.readview=(TextView) view.findViewById(R.id.joke_read_count);
                    holder.jokeImageHolders.add(itemHolder);
                    view.setTag(holder);
                    break;
                case BigItem.Type.LAYOUT_LOADING:
                    view = inflater.inflate(R.layout.listitem_loading, null);
                    holder.layoutType = item.layoutType;
                    
                    view.setTag(holder);
                    break;
                case BigItem.Type.LAYOUT_LOADFAILED:
                    view = inflater.inflate(R.layout.listitem_loadfailed2, null);
                    holder.layoutType = item.layoutType;
    				view.setOnClickListener(new OnClickListener() {
    					
    					@Override
    					public void onClick(View v) {
    						retry();
    					}
    				});
                    view.setTag(holder);
                    break;
            }
            return view;
        }
        @Override
        public void bindView(int position, BigItem item, BigHolder holder) {
            switch (item.layoutType){
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    ArrayList<JokeItemHolder> jokeImageHolders = holder.jokeImageHolders;  
                    for (int i = 0; i < item.cateItems.size() && i < jokeImageHolders.size(); i++) 
                    {            
                    	jokeImageHolders.get(i).isPaly_img.setVisibility(View.GONE);
                    	jokeImageHolders.get(i).gif_text.setVisibility(View.GONE);
                        final CateItem cateItem = item.cateItems.get(i);

                        jokeImageHolders.get(i).content.setText(cateItem.cTitle);
                        
                        
                        if (TextUtils.isEmpty(cateItem.ImagePath)) {
                        	jokeImageHolders.get(i).image.setImageResource(R.drawable.joke_empyt);
                        }  else if(!imageLoader.getPause().get()){
                        	imageLoader.displayImage(cateItem.ImagePath, jokeImageHolders.get(i).image, mOptions);
                     	   }
                        
                        /////////----------------------------
                        final ImageView ispay_img = jokeImageHolders.get(i).isPaly_img;
                        final ImageView gif_text = jokeImageHolders.get(i).gif_text;
                        String type  = "";
                        String url="";
						String contentstring = cateItem.content;
						if(contentstring!=null&&contentstring.trim().length()>0)
						{
							contentstring=contentstring.replaceAll("<p>", "");
							if(!contentstring.equals(""))
							{
								int start=0;
								int end=0;
								TextView textview=new TextView(JokeImageFragment.this.getActivity());
								LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
								lp1.setMargins(20, 0, 20, 0);//(int left, int top, int right, int bottom)
								LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); 
								lp2.setMargins(20, 12, 20, 12);
								FrameLayout.LayoutParams lp3=new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
								lp3.setMargins(20, 12, 20, 12);
								textview.setTextColor(getResources().getColor(R.color.jokedetailcolr));
								textview.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
								// imageView.setPadding(0, 0, 0,0);
								// imageView.setScaleType(ScaleType.FIT_XY);
								textview.setPadding(0, 0, 0,0);
								textview.setGravity(Gravity.TOP|Gravity.LEFT);
								android.util.Log.d("newzx",contentstring);
								android.util.Log.d("newzx",contentstring);

								if(contentstring.startsWith("<img src="))
								{
									start=contentstring.indexOf("<img src=");
									contentstring=contentstring.substring(start+10, contentstring.length());
									end=contentstring.indexOf("\"");
									if(!contentstring.startsWith("http://")){
										url=Constants.APK_URL+contentstring.substring(0,end);
									}else{
										url=contentstring.substring(0,end); 
									}
									end=contentstring.indexOf("/>");
									contentstring=contentstring.substring(end+2, contentstring.length());
									type = url.substring(url.lastIndexOf('.') + 1);
									//jokeImageHolders.get(i).layout.setId(position);
									//LogCart.Log("position-----"+position+"---type--"+type);
									//mlist.put(position, type);
									//murllist.put(position, url);
									if (type.toString().equals("gif")) {
										//ispay_img.setVisibility(View.VISIBLE);
										gif_text.setVisibility(View.VISIBLE);
										LogCart.Log("gif-------gif---"+type);
										
									} else {
										gif_text.setVisibility(View.GONE);
										//ispay_img.setVisibility(View.GONE);
									}


								}else{
									if(contentstring.startsWith("<iframe")||contentstring.startsWith("&lt;iframe")){
										ispay_img.setVisibility(View.VISIBLE);
										int st = 0;
								        st = contentstring.indexOf("http");
								        String h = contentstring.substring(st);
								        String[] ss =h.split("\"");
								        String s = ss[0];
								       // jokeTextImageHolders.get(i).layout.setId(position);
								        type = "mp4";
								        url = s;
								       // mlist.put(position, "mp4");
										//murllist.put(position, s);
									}else{
										ispay_img.setVisibility(View.GONE);
									}

								}
							}
						}
                        //////--------------------------
                        
                        jokeImageHolders.get(i).layout.
                        setOnClickListener(new MylayoutLisenler(position, cateItem,type,url));
                        if(comm_sid == cateItem.sid)
                        {
                        	cateItem.comment_count = refreshCount;
                        	cateItem.comment_img_resid = R.drawable.joke_btn_commented;
                        }                        
                        jokeImageHolders.get(i).comment_count_tv.setText(""+cateItem.comment_count);
                        jokeImageHolders.get(i).comment.setTag(cateItem);
                        jokeImageHolders.get(i).comment.setOnClickListener(new OnClickListener() {			
                			@Override
                			public void onClick(View view) 
                			{
                				CateItem cate=(CateItem)view.getTag();
                				joke_comment(cate);
                				NotifaHttpUtil.shareHttp(Constants.SHREA_PATH+cateItem.sid+".html");
                			}
                		});
                        jokeImageHolders.get(i).collect_count_tv.setText(""+cateItem.vote_count);
                        joke_collect_count = jokeImageHolders.get(i).collect_count_tv;
                        jokeImageHolders.get(i).collect.setTag(cateItem);
                        jokeImageHolders.get(i).collect.setOnClickListener(new OnClickListener() {			
                			@Override
                			public void onClick(View view) 
                			{
                				try {
									CateItem cate=(CateItem)view.getTag();
									joke_collect(cate);
									NotifaHttpUtil.shareHttp(Constants.SHREA_PATH+cateItem.sid+".html");
								} catch (Exception e) {
									e.printStackTrace();
								} 
                			}
                		});
                        jokeImageHolders.get(i).share.setTag(cateItem);
                        jokeImageHolders.get(i).share_count_tv.setText(""+cateItem.comment_count);
                        jokeImageHolders.get(i).share.setOnClickListener(new OnClickListener() {			
                			@Override
                			public void onClick(View arg0) 
                			{
                				joke_share((CateItem) arg0.getTag());
                			}
                		});      
                                               
                        jokeImageHolders.get(i).comment_img.setBackgroundResource(cateItem.comment_img_resid);
                        jokeImageHolders.get(i).collect_img.setBackgroundResource(cateItem.vote_img_resid);
                        try{
                        	jokeImageHolders.get(i).readview.setText(cateItem.cCount+"");
                            }catch(Exception e){
                            	jokeImageHolders.get(i).readview.setText("0");
                            }
                    }                   
                    break;
                case BigItem.Type.LAYOUT_LOADING:
                    
                    break;
                case BigItem.Type.LAYOUT_LOADFAILED:
                    
                    break;
            }
        }
        
    }

	@Override
	protected void onDownloadStateChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
	}

	/*@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == REQUEST_REFRESH)
		{
			if(resultCode == Constants.COMMENT_RESPONSE_CODE)
			{
				Bundle extras = intent.getExtras();
				CateItem cate = extras.getParcelable("jokecommcate");
				cate.comment_count = extras.getInt("jokecomm_count");
				adapter.notifyDataSetChanged();
			}
		}
	}//*/
	public void refreshItem(CateItem cate, int count)
	{
		cate.comment_count = count;
		cate.comment_img_resid = R.drawable.joke_btn_commented;
		comm_sid = cate.sid;
		refreshCount = count;
		adapter.notifyDataSetChanged();
	}
	private void joke_comment(CateItem cate)
	{
		Intent intent =new Intent();
		Bundle bundle = new Bundle();
		bundle.putParcelable("jokecate", cate);
		intent.putExtras(bundle);
		intent.setClass(getActivity(),JokeCommentActivity.class);		
		getActivity().startActivityForResult(intent, REQUEST_REFRESH);
	}
	
	private ProtocolTask task = new ProtocolTask(getActivity());
	
	private class CollectScoreTaskListener implements TaskListener {
		CateItem cateItem;
		public CollectScoreTaskListener(CateItem cateItem) {
			this.cateItem=cateItem;
		}
		@Override
		public void onNoNetworking() {
		}

		@Override
		public void onNetworkingError() {
			
			showShortToast(JokeImageFragment.this.getActivity().getString(R.string.rastar_neterror));
		}

		@Override
		public void onPostExecute(byte[] bytes) {
			try {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if(status == 1) 
				{
					if(!result.isNull("sid")) 
					{
						Log.d(TAG, "sid = " + result.getInt("sid"));
					}
					if(!result.isNull("allCount")) 
					{
						if(result.getInt("allCount") != 0) 
						{
							if(cateItem!=null&&cateItem.sid==result.getInt("sid")){
								cateItem.vote_count=result.getInt("allCount");
								cateItem.vote_img_resid = R.drawable.joke_btn_collected;								
								Log.d(TAG, "allCount = " + result.getInt("allCount"));
								adapter.notifyDataSetChanged();
								
								//----------------------------------------------------------------
								if(isjok){
									if(((MainActivity)getActivity()).queryDataIsExists(Integer.toString(result.getInt("sid"))))
									{
										((MainActivity)getActivity()).updateData(Integer.toString(result.getInt("sid")),0, 1);
									}
									else
									{
										((MainActivity)getActivity()).insertData(Integer.toString(result.getInt("sid")),0, 1);
									}
								}else{
									if(((JokeActivity)getActivity()).queryDataIsExists(Integer.toString(result.getInt("sid"))))
									{
										((JokeActivity)getActivity()).updateData(Integer.toString(result.getInt("sid")),0, 1);
									}
									else
									{
										((JokeActivity)getActivity()).insertData(Integer.toString(result.getInt("sid")),0, 1);
									}
								}
								
								//------------------------------------------------------------
								
								showShortToast(MyApplication.getInstance().getResources().getString(R.string.vote_success));
							}
						} 
						else 
						{
							
							showShortToast(MyApplication.getInstance().getResources().getString(R.string.vote_fail));
						}
					}
				} 
				else 
				{
					showShortToast(MyApplication.getInstance().getResources().getString(R.string.rastar_netcoll));
				}
			} catch (JSONException e) {
				e.printStackTrace(); 
			}
		}

	} 
	private void joke_collect(CateItem cateItem)
	{		
		String url = Constants.JOKE_COMMENT_URL + "?qt=" + Constants.RATING + "&sid=" + cateItem.sid+"&uid="+MyApplication.getInstance().getUser().getUid()
				+ "&rating=" + 1;

		Log.d(TAG, "send sid = " + cateItem.sid);
		
		if(!NetworkUtil.isNetWorking(getActivity())){
			showShortToast(MyApplication.getInstance().getResources().getString(R.string.toast_no_network));
			return;
		}
		
		if (task != null) {
			task.onCancelled();
		}
		
		task = new ProtocolTask(getActivity());
		task.setListener(new CollectScoreTaskListener(cateItem));
		task.execute(url, null, tag(),getHeader());
		
		

		//showShortToast(getString(R.string.submit_suggest));
	}
	private void joke_share(CateItem cateitem)
	{		
		//分享商店功能
		NotifaHttpUtil.shareHttp(Constants.SHREA_PATH+cateitem.sid+".html");
		Intent intent =new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("sid", cateitem.sid);
		bundle.putString("title",cateitem.cTitle );
		intent.putExtras(bundle);
		intent.setClass(getActivity(),ShareActivity.class);		
		getActivity().startActivity(intent);
	}
	
	/*add by zengxiao */
	OnRefreshListener refreshListen=new OnRefreshListener() {
		
		@Override
		public void onRefresh() {
			if(!NetworkUtil.isNetWorking(MyApplication.getInstance()))
			{
				listview.onRefreshComplete();
				Toast.makeText(MyApplication.getInstance(), R.string.rastar_netcoll, Toast.LENGTH_SHORT).show();
			}else{
				handler.removeCallbacks(refreshRunnable);
				handler.postDelayed(refreshRunnable, 2000);
		}
		}
	};
	Runnable refreshRunnable= new Runnable() {				
		@Override
		public void run() {
			listview.setrefreshok();				
		}
	};
	
	/*add end*/	
	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		listview.setonRefreshListener(refreshListen);
		/*View view=LayoutInflater.from(this.getActivity()).inflate(R.layout.bestone_qiushi_head,null);
		listview2.addHeaderView(view);
		TextView switch_date=(TextView)view.findViewById(R.id.switch_date);
		switch_date.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View vv) {
				try {
					Log.d("xxx", "vvvvv");
					adapter.clear();
					request();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});*/
		Log.d("rmyzx", "------------------Subfregment creat");
	}
	public void updaterequest() {
		try {
			Log.d("xxx", "vvvvv");
			adapter.clear();
			request();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected String getRefoushtUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// add   by  bobo-------
	private class MylayoutLisenler implements OnClickListener{
		
		private int mpositon;
		private CateItem mcaItem;
		private String mtype = null;
		private String murl = null;
		public MylayoutLisenler(int position,CateItem caItem,String type,String url){
			this.mpositon = position;
			this.mcaItem = caItem;
			this.mtype = type;
			this.murl = url;
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			LogCart.Log("ddddddddd-----"+mpositon);
			//String type = mlist.get(mpositon);
			LogCart.Log("图片-----"+mtype);
			NotifaHttpUtil.shareHttp(Constants.SHREA_PATH+mcaItem.sid+".html");
			if(mtype.equals("gif")){
				Intent intent = new Intent(JokeImageFragment.this.getActivity(),PlayGifActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("joke_image_path", murl);
				bundle.putInt("sid", mcaItem.sid);
				intent.putExtras(bundle);			
				JokeImageFragment.this.getActivity().startActivity(intent);
			}else if(mtype.equals("mp4")){
				Intent pintent = new Intent(JokeImageFragment.this.getActivity(), HtmlTVpalyActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("url", murl);
				bundle.putInt("sid", mcaItem.sid);
				pintent.putExtras(bundle);
				JokeImageFragment.this.getActivity().startActivity(pintent);
			}else{
				onAppClick(mcaItem, Constants.TOJOKETETAILS);
			}
		}
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageEnd("历史糗百");
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageStart("历史糗百");
	}
	
	@Override
	protected List<BigItem> dblistData() {
		// TODO Auto-generated method stub
		return null;
	}
}
