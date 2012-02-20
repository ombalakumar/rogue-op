package rogue_opcode;

import rogue_opcode.GameProc.TouchState;
import rogue_opcode.geometrics.XYf;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.graphics.Region;

public class TextSE extends ScreenElement {
	private static final long serialVersionUID = 1L;
	static TextPaint sSmallTextPaint;
	static TextPaint sLabelTextPaint;
	static TextPaint sFillPaint;
	static int sTextMargin = 10;
	
	DynamicLayout mTextDL;
	String mText;
	String mTitle;
	int mWidth;				//overall width - includes icon width
	int mIconWidth;
	int mHeight;
	int mTextBottomMargin;
	boolean mFixedHeight;
	RectF mDisplayRect;
	RectF mTextClipRect;
	
	TextPaint mTextPaint;
	
	boolean mInEditMode;
	boolean mEditable;
	boolean mDirtyFlag;		//set only if USER edited the text
		
	//Variable-Height constructor
	public TextSE(String pText, float pX, float pY, int pWidth) {
		super(0);
		initialize(pText, pX, pY, pWidth, -1);
	}
	
	//Fixed-Height constructor
	public TextSE(String pText, float pX, float pY, int pWidth, int pHeight) {
		super(0);
		initialize(pText, pX, pY, pWidth, pHeight);
	}
	
	public boolean Dirty() {
		boolean tDirty = mDirtyFlag;
		Dirty(false);
		
		return tDirty;
	}
	
	void Dirty(boolean pDirty) {
		mDirtyFlag = pDirty;
	}
	
	void initialize(String pText, float pX, float pY, int pWidth, int pHeight) {
		mHeight = pHeight;
		
		mFixedHeight = (mHeight != -1);
		
		mWidth = pWidth;
		mIconWidth = 0;
		
		mTextBottomMargin = 0;
		
		mPos.x = pX;
		mPos.y = pY;
		

		if (sSmallTextPaint == null) {
			sSmallTextPaint = new TextPaint();
			sSmallTextPaint.setTypeface(Typeface.createFromAsset(GameProc.sOnly.getAssets(), "fonts/aispec.ttf"));	//TODO - should be a call from outside of rogue-op
			sSmallTextPaint.setColor(Color.BLACK);
			sSmallTextPaint.setAlpha(0xff);
			sSmallTextPaint.setAntiAlias(true);
			sSmallTextPaint.setTextSize(16);
			sSmallTextPaint.setTextAlign(Paint.Align.LEFT);
		}

		if (sLabelTextPaint == null) {
			sLabelTextPaint = new TextPaint();
			sLabelTextPaint.setColor(0xff8ae234);
			sLabelTextPaint.setAntiAlias(true);
			sLabelTextPaint.setTextSize(16);
			sLabelTextPaint.setTextSkewX(-.25f);
			sLabelTextPaint.setTextAlign(Paint.Align.LEFT);
		}
		
		if (sFillPaint == null) {
			sFillPaint = new TextPaint();
			sFillPaint.setColor(Color.GRAY);
			sFillPaint.setAlpha(0xbb);
			sFillPaint.setAntiAlias(true);
			sFillPaint.setShader(new LinearGradient(0, 0, 0, 20, Color.WHITE, Color.GRAY, Shader.TileMode.CLAMP));
		}
		
		mTextPaint = sSmallTextPaint;
		
		SetText(pText);
		
		mInEditMode = false;
		mEditable = false;
		mDirtyFlag = false;
				
		ZDepth(100);
	}
	
	//Virtual width (not Physical width)
	public int Width() {
		return (int) (mWidth);
	}
	
	public int Height() {
		return (int) (mHeight);
	}
	
	public void AddIcon(int pGRID, int pIconWidth) {
		mIconWidth = (int) (pIconWidth);
		SetCurrentGR(pGRID);
		SetText(mText);
	}
	
	public void SetTextBottomMargin(int pMargin) {
		mTextBottomMargin = pMargin;
		SetText(mText);
	}
	
	public String Text() {
		return mText;
	}
	
	public void Text(String pText) {
		SetText(pText);
	}
	
	public String Title() {
		return (mTitle == null) ? "" : mTitle;
	}
	
	public void Title(String pTitle) {
		mTitle = pTitle;
	}
	
	public void SetTextPaint(TextPaint pTextPaint) {
		mTextPaint = pTextPaint;
		
		SetText(Text());
	}

	public void SetText(String pText) {
		mText = pText;
		
		try {
			mTextDL = new DynamicLayout(mText, mTextPaint, mWidth - (sTextMargin * 2) - (sTextMargin + mIconWidth), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		} catch(Exception e) {
			//The text and icon don't fit into this mWidth
		}
		if (!mFixedHeight) {
			mHeight = mTextDL.getLineTop(mTextDL.getLineCount()) + (sTextMargin * 2);
			
			if (mHeight < ((sTextMargin * 2) + mIconWidth))
				mHeight = (sTextMargin * 2) + mIconWidth;
		}
		
		mDisplayRect = new RectF(0, 0, mWidth, mHeight);
		mTextClipRect = new RectF(0, 0, mWidth, mHeight - mTextBottomMargin);
	}
	
	public boolean WithinRange(XYf pPoint) {
		return ((pPoint.x > mPos.x) && (pPoint.x < mPos.x + mWidth) && (pPoint.y > mPos.y) && (pPoint.y < mPos.y + mHeight));
	}
	
	public void Editable(boolean pEditable) {
		mEditable = pEditable;
	}
	
	public void Visible(boolean pVisible) {
		super.Visible(pVisible);
		
		if (mInEditMode) {
			GameProc.sOnly.HideTextEditor(this);
			mInEditMode = false;
		}
	}
	
	public void Update() {
		if (mEditable) {
			try {
				if (mInEditMode) {
					if (GameProc.sOnly.mEditTextParams.mCurrentTE == this)
						SetText(GameProc.sOnly.mEditTextParams.mEdit.getText().toString());
					
					if (GameProc.sOnly.mTouchState.Is(TouchState.SINGLE_TAP)) {
						if (!WithinRange(new XYf(GameProc.sOnly.mTouchState.GetMainX(), GameProc.sOnly.mTouchState.GetMainY()))) {
							mInEditMode = false;
							Dirty(true);
							GameProc.sOnly.HideTextEditor(this);
						}
					}
				} else {
					if (GameProc.sOnly.mTouchState.Is(TouchState.SINGLE_TAP)) {
						if (Visible()) {
							if (WithinRange(new XYf(GameProc.sOnly.mTouchState.GetMainX(), GameProc.sOnly.mTouchState.GetMainY()))) {
								mInEditMode = true;
								GameProc.sOnly.ShowTextEditor(this, mPos, mWidth, mHeight);
							}
						}
					}
				}
			} catch(Exception e) {
			}
		}
	}
	
	
	public void Draw() {
		Canvas tCanvas = AnimatedView.sCurrentCanvas;
		float tX = mPos.x;// * AnimatedView.sOnly.mPreScaler;
		float tY = mPos.y;// * AnimatedView.sOnly.mPreScaler;
		
		tCanvas.save();
		tCanvas.scale(AnimatedView.sOnly.mPreScaler, AnimatedView.sOnly.mPreScaler);
		
		tCanvas.translate(tX, tY);
		tCanvas.drawRoundRect(mDisplayRect, 10, 10, sFillPaint);
		tCanvas.drawText(Title(), 10, -2, sLabelTextPaint);
		tCanvas.translate(sTextMargin + sTextMargin + mIconWidth, sTextMargin);
		tCanvas.clipRect(mTextClipRect, Region.Op.REPLACE);
		mTextDL.draw(tCanvas);
		tCanvas.restore();
		
		tCanvas.save();
		int tIconOffset = (int) (AnimatedView.sOnly.mPreScaler * ((mIconWidth / 2) + sTextMargin));
		tCanvas.translate(tIconOffset, tIconOffset);
		super.Draw();
		tCanvas.restore();
	}
}
