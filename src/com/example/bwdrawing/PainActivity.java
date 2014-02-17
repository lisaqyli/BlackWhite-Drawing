package com.example.bwdrawing;



import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class PainActivity extends Activity {

	//layout, how to 
		CustomView mCustomView;
		OnTouchListener touchListener;
		Bitmap resizedBMap,grayBMap,mainBMap, shadowBMap, colorBMap, mutableBitmap;
		
		LinearLayout layout;
		//check for shadowbitmap, if its white than copy from the color bitmap, if its black then copy from the black bitmap	
				
		private Paint drawPaint = new Paint();
		private Path drawPath = new Path();
		private int color = Color.BLACK;
		Canvas canvas, shadowCanvas;
		
		int brushSize = 20;
		
		//graypixel and colorpixel arrays
		int  grayscalePixels[];
		int colorscalePixels[];
		
		//width && height
		int width = 700;
		int height = 800;
		
		boolean colorToGray = true;
		
		public void setColorBrush(boolean b) {
			colorToGray = b;
			shadowBMap.eraseColor(Color.WHITE);
			shadowCanvas.setBitmap(shadowBMap);
			drawPath = new Path();
		}
		
		public void clean(View v) {
			if (colorToGray) {
				canvas.drawBitmap(resizedBMap, 0, 0, null);
				mCustomView.invalidate();
			} else {
				canvas.drawBitmap(grayBMap, 0, 0, null);
				mCustomView.invalidate();
			}
		}

		
		
		//to gray scale
		//Cite:http://stackoverflow.com/questions/8381514/android-converting-color-image-to-grayscale
		public Bitmap toGrayscale(Bitmap bmpOriginal)
	    {        
	        int width, height;
	        height = bmpOriginal.getHeight();
	        width = bmpOriginal.getWidth();    

	        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	        Canvas c = new Canvas(bmpGrayscale);
	        Paint paint = new Paint();
	        ColorMatrix cm = new ColorMatrix();
	        cm.setSaturation(0);
	        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	        paint.setColorFilter(f);
	        c.drawBitmap(bmpOriginal, 0, 0, paint);
	        return bmpGrayscale;
	    }
		
		
		public class CustomView extends View {
			public CustomView(Context context){
				super(context);
				//Set up drawing so need to implement later;
				
				drawPaint.setAntiAlias(true);
				drawPaint.setColor(color);
				drawPaint.setStyle(Paint.Style.STROKE);
				drawPaint.setStrokeWidth(brushSize);
				
				//updating my graypixel array and colorpixel array
				grayscalePixels = new int[width * height];
				grayBMap.getPixels(grayscalePixels, 0, grayBMap.getWidth(), 0, 0,
						grayBMap.getWidth(), grayBMap.getHeight());
				
				colorscalePixels = new int[width * height];
				resizedBMap.getPixels(colorscalePixels, 0,resizedBMap.getWidth(), 0, 0,
						resizedBMap.getWidth(), resizedBMap.getHeight());
			}
			
			public void setBrushSize(int i) {
				brushSize = i;
			}
			
			protected void onDraw(Canvas canvas) {
				super.onDraw(canvas);
				//ondraw is updating the view
				
				
	        	shadowCanvas.drawPath(drawPath, drawPaint);
	        	//if i do a canvas and draw new bitmap on the canvas then I dont get my picture updated
	        	int width = resizedBMap.getWidth();
	        	int height = resizedBMap.getHeight();
	        	int[] shadowPixels = new int[width * height];
	    		int[] mainPixels = new int[width * height];
	    		
	    		shadowBMap.getPixels(shadowPixels, 0, shadowBMap.getWidth(), 0, 0,
	    				shadowBMap.getWidth(), shadowBMap.getHeight());
	    		mainBMap.getPixels(mainPixels, 0, mainBMap.getWidth(), 0, 0,
	    				mainBMap.getWidth(), mainBMap.getHeight());
	    		
	    		for (int i = 0; i < width * height; i++) {
	    			if (shadowPixels[i] == Color.BLACK) {
	    				if (colorToGray) {
	    					mainPixels[i] = grayscalePixels[i];
	    				} else {
	    					mainPixels[i] = colorscalePixels[i];
	    				}
	    				
	    			}
	    		}
	    		mainBMap.setPixels(mainPixels, 0, shadowBMap.getWidth(),0, 0,
	    						mainBMap.getWidth(), mainBMap.getHeight());
	    		
	    		canvas.drawBitmap(mainBMap, 0, 0, null);
			
		}
	}
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_pain);
			Resources res = getResources();
			colorBMap = BitmapFactory.decodeResource(res, R.drawable.heart);
			mutableBitmap = colorBMap.copy(Bitmap.Config.ARGB_8888, true);
			resizedBMap = Bitmap.createScaledBitmap(mutableBitmap, 700, 800, false);
			grayBMap = toGrayscale(resizedBMap);
			layout = (LinearLayout) findViewById(R.id.ll);
	        mCustomView = new CustomView(this);
	        layout.addView(mCustomView);
	      
	        Bundle extras = this.getIntent().getExtras();
	        int picId = extras.getInt("picId");
	        
	        mainBMap = Bitmap.createBitmap(resizedBMap);
	        shadowBMap = Bitmap.createBitmap(700, 800, Bitmap.Config.ARGB_8888);
	        canvas = new Canvas(mainBMap);
	        shadowCanvas =  new Canvas(shadowBMap);

	        
	        touchListener = new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event){
					float touchX = event.getX();
					float touchY = event.getY();
					
					switch (event.getAction()) {
					
					case MotionEvent.ACTION_DOWN:
					    drawPath.moveTo(touchX, touchY);
					    break;
					case MotionEvent.ACTION_MOVE:
					    drawPath.lineTo(touchX, touchY);
					    mCustomView.invalidate();
					    break;
					case MotionEvent.ACTION_UP:
					    shadowCanvas.drawPath(drawPath, drawPaint);
					    //drawPath.reset();
					    break;
					default:
					    return false;
					}
					//invalidate calls onDraw() again
					//drawPath = new Path(); 
					return true;
				}
			};
			mCustomView.setOnTouchListener(touchListener);
			
			final Spinner colorGraySpin = (Spinner) findViewById(R.id.spinner2);
			ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
			        R.array.brush_color, android.R.layout.simple_spinner_item);
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			colorGraySpin.setAdapter(adapter2);
			
			colorGraySpin.setOnItemSelectedListener(new  AdapterView.OnItemSelectedListener() { 

		        public void onItemSelected(AdapterView<?> adapterView, 
		         View view, int i, long l) { 
		         // TODO Auto-generated method stub
		        	String selected2 = colorGraySpin.getSelectedItem().toString();
			        Toast.makeText(PainActivity.this,"You Selected : "
			       + selected2,Toast.LENGTH_SHORT).show();     
		           if (selected2.equals("Colorscale")) {
		        	   setColorBrush(false);
		           } else{
		        	   setColorBrush(true);
		           }
		        	
		        }
	  		                // If no option selected
			    public void onNothingSelected(AdapterView<?> arg0) {
			     // TODO Auto-generated method stub	          
			    }
			  });
			
			//Things abt the spinner
			final Spinner spinner = (Spinner) findViewById(R.id.spinner1); 
			// Create an ArrayAdapter using the string array and a default spinner layout
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
			        R.array.brush_size_choice, android.R.layout.simple_spinner_item);
			// Specify the layout to use when the list of choices appears
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			spinner.setAdapter(adapter);
			
			// Set the ClickListener for Spinner
			spinner.setOnItemSelectedListener(new  AdapterView.OnItemSelectedListener() { 

		        public void onItemSelected(AdapterView<?> adapterView, 
		         View view, int i, long l) { 
		         // TODO Auto-generated method stub
		        	String selected = spinner.getSelectedItem().toString();
			        Toast.makeText(PainActivity.this,"You Selected : "
			       + selected +" brush ",Toast.LENGTH_SHORT).show();     
		           
		        	if (selected.equals("Small")) {
		        		drawPaint.setStrokeWidth(20);
		        	} else if (selected.equals("Medium")){
		        		drawPaint.setStrokeWidth(40);
		        	} else {
		        		drawPaint.setStrokeWidth(60);
		        	}
		        }
	  		                // If no option selected
			    public void onNothingSelected(AdapterView<?> arg0) {
			     // TODO Auto-generated method stub	          
			    }
			  });
			
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}
		

	}
