package com.byt.market.mediaplayer;
import java.util.ArrayList;



import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.mediaplayer.MusicDownLoadManageActivity;
import com.byt.market.mediaplayer.ui.VideoMainFragment;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.RapitUtile;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
public class VideoActivity extends FragmentActivity  implements OnClickListener{

	private View recArea;
	AlwsydMarqueeTextView tv_title;
	private VideoMainFragment movieFragment;
	//private VallayFragment vallayFragment;
	private View movie_line,holly_line;
	Button buttonmovie,buttonholly;
	ViewPager mPager;
    private ArrayList<BaseUIFragment> fragmentList;  
    public static long umtime; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_main_fragment);
		initView();
		initPaper();
		umtime = System.currentTimeMillis();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String text = getString(R.string.happy_more);
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	private void initPaper() {
	
		
	}
	private void initView() {
		try {
			findViewById(R.id.topline4).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
			findViewById(R.id.titlebar_applist_button_container).setVisibility(
					View.GONE);
			findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
			findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
			findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
			findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
			findViewById(R.id.top_downbutton).setOnClickListener(this);
			tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
			tv_title.setText(R.string.movie_string);
			findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			recArea.setVisibility(View.VISIBLE);
			
		} catch (Exception e) {
			Log.d("myzx","initViewerror");
			e.printStackTrace();
		}

	}
	

	public void requestDelay() {
	movieFragment.requestDelay();
		
	}
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter{  
        ArrayList<BaseUIFragment> list;  
        public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<BaseUIFragment> list) {  
            super(getSupportFragmentManager());  
            this.list = list;  
              
        }  
          
          
        @Override  
        public int getCount() {  
            return list.size();  
        }  
          
        @Override  
        public BaseUIFragment getItem(int arg0) {  
            return list.get(arg0);  
        }  
          
          
          
          
    }

	private void onMYPageChange(int page) {
		switch(page)
		{
		case 0:
			movie_line.setVisibility(View.VISIBLE);
			holly_line.setVisibility(View.INVISIBLE);	
			buttonmovie.setSelected(true);
			buttonholly.setSelected(false);
			requestDelay();	
        	break; 

			/*case 3:
			buttonringrec.setSelected(false);
			buttonringhome.setSelected(false);
			buttonringhot.setSelected(false);
			buttonringrank.setSelected(true);
			if (homeFragment4 != null) {
				homeFragment4.request();
			}
			ringrec_line.setVisibility(View.INVISIBLE);
			ringhome_line.setVisibility(View.INVISIBLE);
			ringhot_line.setVisibility(View.INVISIBLE);
			ringrank_line.setVisibility(View.VISIBLE);	
    	break; */
		}

	}
	public void finishWindow() {
		finish();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		StatService.trackCustomEndEvent(this, "move", "");
		int endtime = (int) (System.currentTimeMillis()-umtime);
		MobclickAgent.onEventValue(this, "Move", null,endtime/1000);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		StatService.trackCustomBeginEvent(this, "move", "");
	}
	
	@Override
	protected void onStop() {
		super.onStop();	
	}

	@Override
	public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.titlebar_back_arrow:
				finishWindow();
				break;
			case R.id.top_downbutton:
				Intent downloadIntent = new Intent(this,
						MusicDownLoadManageActivity.class);
				startActivity(downloadIntent);
				this.overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
				break;
			}
		
	}

	
}
