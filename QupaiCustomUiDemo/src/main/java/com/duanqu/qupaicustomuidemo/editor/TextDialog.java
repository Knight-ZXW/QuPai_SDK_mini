package com.duanqu.qupaicustomuidemo.editor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.asset.FontResolver;
import com.duanqu.qupai.effect.OverlayUIController;
import com.duanqu.qupai.effect.JChineseConvertor;
import com.duanqu.qupai.uil.UILOptions;
import com.duanqu.qupai.widget.CircularColorView;
import com.duanqu.qupai.widget.control.TabGroup;
import com.duanqu.qupai.widget.control.TabbedViewStackBinding;
import com.duanqu.qupai.widget.control.ViewStack;
import com.duanqu.qupaicustomuidemo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextDialog extends DialogFragment {

	private EditText mEditView;
	private TextView mSend;
	private View font_new;
	private TextView textLimit;

	private GridView colorList;
	private GridView strokeList;
	GridView fontList;
	FontAdapter fontAdapter;
	private FrameLayout pageContainer;
	private TabGroup pageTabGroup;
	private TabGroup colorTabGroup;
	private FontResolver fontManager;
	private AssetRepository repo;

	private OverlayUIController controller;

	private boolean isStroke;
	private int sharePosition = -1;
	
	private OnStateChangeListener mOnStateChangeListener;
	public static TextDialog newInstance() {
		return new TextDialog();
	}

	public void setFontManager(FontResolver fontManager) {
		this.fontManager = fontManager;
	}

	public void setAssetRepository(AssetRepository repo) {
		this.repo = repo;
	}

	public void setOverlayController(OverlayUIController controller){
		this.controller = controller;
	}
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		mEditView.removeTextChangedListener(textWatch);
		CharSequence text = controller.getText();
		if(mOnStateChangeListener != null){
			mOnStateChangeListener.onDismiss(TextUtils.isEmpty(text) ? null : text.toString(),
					controller.getTextColor(), controller.getTextStrokeColor(), controller.getFontId());
		}

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		if (dialog != null) {
			dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
									 KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						dialog.dismiss();
						return true;
					}
					return false;
				}
			});
			dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}

		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container,
			Bundle savedInstanceState) {

		View contentview = View.inflate(
				getActivity(), R.layout.qupai_row_text_bottom, null);

		if(controller == null){
			dismiss();
			return contentview;
		}

		isStroke = controller.getTextStrokeColor() != 0;

		FrameLayout video = (FrameLayout)contentview.findViewById(R.id.video);

		video.addView(controller.getEditOverlayView());
		mEditView = (EditText) controller.getEditOverlayView()
				.findViewById(R.id.qupai_overlay_content_text);
		mSend = (TextView) contentview.findViewById(R.id.send);

		pageContainer = (FrameLayout) contentview.findViewById(R.id.container);
		ViewStack pagerViewStack = new ViewStack(View.GONE);
		pagerViewStack.addView(new View(getActivity()));
		pagerViewStack.addView(contentview.findViewById(R.id.color_container));
		pagerViewStack.addView(contentview.findViewById(R.id.font_layout));
		pageTabGroup = new TabGroup();
		pageTabGroup.addView(contentview.findViewById(R.id.tab_text));
		pageTabGroup.addView(contentview.findViewById(R.id.tab_color));
		pageTabGroup.addView(contentview.findViewById(R.id.tab_font));
		TabbedViewStackBinding pagerStackBinding = new TabbedViewStackBinding() {
			@Override
			public void onCheckedChanged(TabGroup control, int checkedIndex) {

				if(checkedIndex == 0){
					pageContainer.setVisibility(View.GONE);
					mEditView.setEnabled(true);
					openKeyboard();
				}else{
					pageContainer.setVisibility(View.VISIBLE);
					closeKeyboard();
					super.onCheckedChanged(control, checkedIndex);
					if(checkedIndex == 1){
						if(isStroke){
							colorTabGroup.setCheckedIndex(0);
							if(strokeList.getCheckedItemPosition() == -1){
								strokeList.setItemChecked(((ColorAdapter)strokeList.getAdapter())
												.getLastCheckedPosition(controller.getTextColor(),
														controller.getTextStrokeColor()),
										true);
							}
						}else{
							colorTabGroup.setCheckedIndex(1);
							if(colorList.getCheckedItemPosition() == -1){
								colorList.setItemChecked(((ColorAdapter)colorList.getAdapter())
												.getLastCheckedPosition(controller.getTextColor()),
										true);
							}
						}
						mEditView.setEnabled(false);
					}else{
						if(font_new.getVisibility() == View.VISIBLE){
							font_new.setVisibility(View.GONE);
							SharedPreferences sp = getActivity().getSharedPreferences("AppGlobalSetting", 0);
							sp.edit().putBoolean("font_category_new", false).apply();
						}
						if(fontList.getCheckedItemPosition() == -1){
							int position = ((FontAdapter)fontList.getAdapter())
									.getLastCheckedPosition(controller.getFontId());
							fontList.setItemChecked(position, true);
							fontList.smoothScrollToPosition(position);
						}
					}

				}

			}
		};
		pagerStackBinding.setViewStack(pagerViewStack);
		pageTabGroup.setOnCheckedChangeListener(pagerStackBinding);

		ViewStack viewStack = new ViewStack(View.GONE);
		viewStack.addView(contentview.findViewById(R.id.color_stroke_list));
		viewStack.addView(contentview.findViewById(R.id.color_list));

		colorTabGroup = new TabGroup();
		colorTabGroup.addView(contentview.findViewById(R.id.tab_text_effect_stroke));
		colorTabGroup.addView(contentview.findViewById(R.id.tab_text_effect_color));

		TabbedViewStackBinding textColorStackBinding = new TabbedViewStackBinding(){

			@Override
			public void onCheckedChanged(TabGroup control, int checkedIndex) {
				super.onCheckedChanged(control, checkedIndex);
				ColorItem item = null;
				if(checkedIndex == 0){
					isStroke = true;
					int position = colorList.getCheckedItemPosition();
					strokeList.setItemChecked(position, true);
					item = (ColorItem) strokeList.getItemAtPosition(position);
					if(item != null){
						controller.setTextColor(item.color);
						controller.setTextStrokeColor(item.strokeColor);
					}
				}else if(checkedIndex == 1){
					isStroke = false;
					int position = strokeList.getCheckedItemPosition();
					colorList.setItemChecked(position, true);
					item = (ColorItem) colorList.getItemAtPosition(position);
					if(mOnStateChangeListener != null && item != null){
						controller.setTextColor(item.color);
						controller.setTextStrokeColor(0);
					}
				}

			}

		};
		textColorStackBinding.setViewStack(viewStack);
		colorTabGroup.setOnCheckedChangeListener(textColorStackBinding);

		colorList = (GridView) contentview.findViewById(R.id.color_list);
		strokeList = (GridView) contentview.findViewById(R.id.color_stroke_list);

		ColorAdapter colorAdapter = new ColorAdapter();
		colorAdapter.setData(initColors(false));
		colorList.setAdapter(colorAdapter);

		ColorAdapter strokeAdapter = new ColorAdapter();
		strokeAdapter.setData(initColors(true));
		strokeList.setAdapter(strokeAdapter);

		List<AssetInfo> list = new ArrayList<>();
		for(AssetInfo font : repo.find(AssetRepository.Kind.FONT)){
			if(font.isAvailable()){
				list.add(font);
			}
		}

		fontList = (GridView)contentview.findViewById(R.id.font_list);
		fontAdapter = new FontAdapter();
		fontAdapter.setData(list);
		fontList.setAdapter(fontAdapter);
		fontList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AssetInfo info = (AssetInfo)parent.getItemAtPosition(position);
				if(!info.isAvailable()){
					return;
				}

				controller.setFontId(info.getID());

				fontList.setItemChecked(position, true);

				String convert;
				try {
					convert = getTextByFontType(mEditView.getText().toString());
				} catch (IOException e){
					convert = null;
					e.printStackTrace();
				}
				if(convert != null){
					mEditView.setText(convert);
				}

			}
		});

		LayoutParams localLayoutParams = getDialog().getWindow()
				.getAttributes();
		localLayoutParams.gravity = Gravity.BOTTOM;
		localLayoutParams.width = LayoutParams.MATCH_PARENT;

		font_new = contentview.findViewById(R.id.tab_effect_font_new);
		SharedPreferences sp = getActivity().getSharedPreferences("AppGlobalSetting", 0);
		boolean isFontHasNew = sp.getBoolean("font_category_new", false);
		font_new.setVisibility(isFontHasNew ? View.VISIBLE : View.GONE);
		setOnClick();
		pageTabGroup.setCheckedIndex(0);
		textLimit = (TextView)contentview.findViewById(R.id.message);
		requestFocusForKeyboard();
		if(controller.isTextOnly()){
			textLimit.setVisibility(View.GONE);
		}else{
			CharSequence text = controller.getText();
			if(TextUtils.isEmpty(text)){
				textLimit.setText("0 / 10");
			}else{
				textLimit.setText(count(text.toString()) + " / 10");
			}
		}
		contentview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return contentview;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		closeKeyboard();
	}

	private List<ColorItem> initColors(boolean stroke){
		List<ColorItem> list = new ArrayList<ColorItem>();getResources();
		ColorItem c1 = new ColorItem();
		c1.color = controller.getDefaultTextColor();
		c1.first = true;
		c1.isStroke = stroke;
		int strokecolor = controller.getDefaultTextStrokeColor();
		c1.strokeColor = strokecolor == 0 ? Color.BLACK : strokecolor;
		list.add(c1);

		TypedArray colors = getResources().obtainTypedArray(R.array.qupai_text_edit_colors);
		TypedArray strokeColors = getResources().obtainTypedArray(R.array.qupai_text_edit_colors_stroke);

		for(int i = 0; i < 23; i++){
			int color = colors.getColor(i, Color.WHITE);
			ColorItem ci = new ColorItem();
			ci.color = color;
			ci.isStroke = stroke;
			list.add(ci);
			if(stroke){
				int strokeColor;
				if(i >= 13){
					strokeColor = Color.WHITE;
				}else{
					strokeColor = strokeColors.getColor(i, Color.WHITE);
				}
				ci.strokeColor = strokeColor;
			}

		}
		colors.recycle();
		strokeColors.recycle();
		return list;
	}

	ArrayList<BackgroundColorSpan> masks = new ArrayList<>();
	private String filterComposingText(Editable s){
		StringBuilder sb = new StringBuilder();

		int composingStart = 0;
		int composingEnd = 0;

		Object[] sps = s.getSpans(0, s.length(), Object.class);
		if (sps != null) {
            for (int i = sps.length - 1; i >= 0; i--) {
                final Object o = sps[i];
                final int fl = s.getSpanFlags(o);
                Log.d("EDITTEXT", "SpanFlag : " + fl + " is composing" + (fl&Spanned.SPAN_COMPOSING));
                if((fl&Spanned.SPAN_COMPOSING) != 0){
                	composingStart = s.getSpanStart(o);
                	composingEnd = s.getSpanEnd(o);
                	Log.d("EDITTEXT", "startAnimation : " + composingStart + " end : " + composingEnd);
                	break;
                }
            }
        }

		sb.append(s.subSequence(0, composingStart));
    	sb.append(s.subSequence(composingEnd, s.length()));
		if(composingStart == composingEnd){
			if(masks.size() > 0){
				for(BackgroundColorSpan mask : masks){
					s.removeSpan(mask);
				}
				masks.clear();
			}
		}else{
			BackgroundColorSpan mask = new BackgroundColorSpan(getResources().getColor(R.color.accent_material_dark));
			s.setSpan(mask, composingStart, composingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			masks.add(mask);
		}

		Log.d("EDITTEXT", "str : " + sb.toString());

    	return sb.toString();
	}

	private int count(String text){
		int len = text.length();
		int skip;
		int letter = 0;
		int chinese = 0;
//		int count = 0;
//		int sub = 0;
		for(int i = 0; i < len; i += skip){
			int code = text.codePointAt(i);
			skip = Character.charCount(code);
			if(code == 10){
				continue;
			}
			String s = text.substring(i, i + skip);
			if(isChinese(s)){
				chinese++;
			}else{
				letter++;
			}

		}
		letter = letter % 2 == 0 ? letter / 2 : (letter / 2 + 1);
		int result = chinese + letter;
		return result;
	}

	// 完整的判断中文汉字和符号
    private boolean isChinese(String strName) {
    	char[] ch = strName.toCharArray();
    	for (int i = 0; i < ch.length; i++) {
    		char c = ch[i];
    		if (isChinese(c)) {
    			return true;
    		}
    	}
    	return false;
    }

	private boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	private class MyTextWatcher implements TextWatcher {

		private String text;
        private int editStart;
        private int editEnd;
		private Toast toast_outOf;

		private void showOutofCount(Context context, String text, int gravity, int xOffset, int yOffset, int duration){
			if(toast_outOf != null){
				toast_outOf.cancel();
				toast_outOf = null;
			}
			toast_outOf = Toast.makeText(context, text, duration);
			toast_outOf.setGravity(gravity, xOffset, yOffset);
			toast_outOf.show();
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			//Log.d("EDITTEXT", "onTextChanged text : " + s + " startAnimation : " + startAnimation + " before : " + before + " count : " + count);

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			Log.d("EDITTEXT", "beforeTextChanged text : " + s + " startAnimation : " + start + " after : " + after + " count : " + count);

		}

		@Override
		public void afterTextChanged(Editable s) {

			text = filterComposingText(s);

			String convert;
			try {
				convert = getTextByFontType(text);
			} catch (IOException e){
				convert = null;
				e.printStackTrace();
			}

			if(convert != null){
				s.replace(0, convert.length(), convert);
			}
			if (controller.isTextOnly()){
				return ;
			}

			int count = count(text);

			if (!controller.isTextOnly()) {
				textLimit.setText((count > 10 ? 10 : count) + " / 10");
//				Toast.makeText(getActivity(), (count > 10 ? 10 : count) + " / 10", Toast.LENGTH_SHORT).show();
			}

            editStart = mEditView.getSelectionStart();
            editEnd = mEditView.getSelectionEnd();

            // 限定EditText只能输入10个数字
            if (count > 10 && editStart > 0) {
            	Log.d("TEXTDIALOG", "超过10个以后的数字");
                // 默认光标在最前端，所以当输入第11个数字的时候，删掉（光标位置从11-1到11）的数字，这样就无法输入超过10个以后的数字


				showOutofCount(getActivity(), getString(R.string.qupai_text_count_outof),
						Gravity.CENTER, 0, 0, Toast.LENGTH_SHORT);

                s.delete(editStart - 1, editEnd);
                mEditView.setText(s);
                mEditView.setSelection(s.length());
            }

		}

		public String getText(){
			return text;
		}
	}

	private String getTextByFontType(String text) throws IOException {
		if(TextUtils.isEmpty(text)){
			return null;
		}
		long overlayId = controller.getFontId();
		int fonttype = fontManager.getFontType(overlayId);

		String convert;
		if(fonttype == AssetInfo.FONT_TYPE_COMPLEX){
			convert = JChineseConvertor.getInstance(getActivity()).s2t(text);
		}else if(fonttype == AssetInfo.FONT_TYPE_FAMILIAR){
			convert = JChineseConvertor.getInstance(getActivity()).t2s(text);
		}else{
			convert = text;
		}
		if(TextUtils.equals(convert, text)){
			return null;
		}
		return convert;
	}

	private MyTextWatcher textWatch = new MyTextWatcher();

	private void setOnClick() {
		mEditView.addTextChangedListener(textWatch);

		mEditView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(pageTabGroup.getCheckedIndex() != 0 && event.getAction() == MotionEvent.ACTION_DOWN){
					pageTabGroup.setCheckedIndex(0);
					return true;
				}
				return false;
			}
		});

		mSend.setOnClickListener(new OnClickListener() {

			private void deleteWrap(Editable s){
				boolean skip = false;
				for(int i = 0; i < s.length(); i++) {
					char c = s.charAt(i);
					if(c == '\n'){
						if(!skip){
							skip = true;
						}else{
							s.delete(i, i + 1);
						}
					}
				}
			}

			@Override
			public void onClick(View v) {
				mEditView.removeTextChangedListener(textWatch);
				Editable editable = mEditView.getText();
				//deleteWrap(editable);
				String comment = filterComposingText(editable);

				if (mOnStateChangeListener != null) {
					mOnStateChangeListener.onSendButtonClick(comment, controller.getTextColor(),
							controller.getTextStrokeColor(), controller.getFontId());

				}
				dismiss();
			}
		});

	}

	public void closeKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(inputManager.isActive()){
			inputManager
					.hideSoftInputFromWindow(this.mEditView.getWindowToken(), 0);
		}
	}

	public void openKeyboard() {
		this.mEditView.postDelayed(this.mOpenKeyboardRunnable, 300);
	}

	private Runnable mOpenKeyboardRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				requestFocusForKeyboard();
				InputMethodManager input = ((InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE));
				if(!input.showSoftInput(mEditView, 0)){
					openKeyboard();
				}else {
					mEditView.setSelection(mEditView.getText().length());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void requestFocusForKeyboard() {
		this.mEditView.setFocusable(true);
		this.mEditView.setFocusableInTouchMode(true);
		this.mEditView.requestFocus();
		this.mEditView.requestFocusFromTouch();
	}

	@Override
	public void onActivityCreated(Bundle paramBundle) {
		super.onActivityCreated(paramBundle);
	}

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		Log.d("Dialog", "Dialog oncreate的时间：" + System.currentTimeMillis());
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.TextDlgStyle);

	}

	@Override
	public void onResume() {
		if(getDialog() != null){
			getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
		}

		if(sharePosition >= 0){
			AssetInfo info = fontAdapter.getData().get(sharePosition);
			AssetInfo newInfo = repo.find(AssetRepository.Kind.FONT, info.getID());
			fontAdapter.getData().set(sharePosition, newInfo);
			sharePosition = -1;
			fontAdapter.notifyDataSetChanged();
		}

		super.onResume();
	}

	public void setOnStateChangeListener(
			OnStateChangeListener onStateChangeListener) {
		this.mOnStateChangeListener = onStateChangeListener;
	}

	class ColorAdapter extends BaseAdapter {

		public List<ColorItem> list = new ArrayList<>();

		public void setData(List<ColorItem> data){
			if(data == null || data.size() == 0){
				return;
			}
			list.addAll(data);
			notifyDataSetChanged();
		}

		public int getLastCheckedPosition(int color){
			int position = 0;
			for(int i = 0; i < list.size(); i++){
				ColorItem ci = list.get(i);
				if(ci.color == color){
					position = i;
					break;
				}
			}
			return position;
		}

		public int getLastCheckedPosition(int color, int strokeColor){
			int position = 0;
			for(int i = 0; i < list.size(); i++){
				ColorItem ci = list.get(i);
				if(ci.color == color && ci.strokeColor == strokeColor){
					position = i;
					break;
				}
			}
			return position;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public ColorItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ColorItemViewMediator localViewHolder;
			if (convertView == null) {
				localViewHolder = new ColorItemViewMediator(parent);
				convertView = localViewHolder.getView();
				//Log.d("share_menu", "分享菜单的position：" + paramInt);
			} else {
				localViewHolder = (ColorItemViewMediator) convertView.getTag();
			}
			final ColorItem item = getItem(position);

			localViewHolder.setData(item);
			if(item.isStroke){
				if(strokeList.getCheckedItemPosition() == position){
					localViewHolder.setSelected(true);
				}else{
					localViewHolder.setSelected(false);
				}
			}else{
				if(colorList.getCheckedItemPosition() == position){
					localViewHolder.setSelected(true);
				}else{
					localViewHolder.setSelected(false);
				}
			}

			localViewHolder.setListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ColorItemViewMediator mediator = (ColorItemViewMediator) v.getTag();
					ColorItem item = mediator.getData();
					if(item.isStroke){
						controller.setTextColor(item.color);
						controller.setTextStrokeColor(item.strokeColor);
						strokeList.setItemChecked(position, true);
					}else{
						controller.setTextColor(item.color);
						controller.setTextStrokeColor(0);
						colorList.setItemChecked(position, true);
					}
				}
			});

			return convertView;
		}

	}

    private class ColorItemViewMediator {
        private CircularColorView image;
        private View select;
        private View root;
        private ColorItem _Data;

        ColorItemViewMediator(ViewGroup parent) {
        	root = View.inflate(parent.getContext(), R.layout.item_qupai_textcolor, null);
        	image = (CircularColorView) root.findViewById(R.id.color);
        	select = root.findViewById(R.id.selected);
        	root.setTag(this);
        }

        public View getView() {
            return root;
        }

        public ColorItem getData() {
            return _Data;
        }

        public void setListener(OnClickListener listener) {
        	root.setOnClickListener(listener);
        }

        public void setSelected(boolean selected){
        	select.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        public void setData(ColorItem item) {
            _Data = item;
            if(item.isStroke){
            	image.setCircularColor(item.color);
            	image.setStrokeColor(item.strokeColor);
            }else{
            	image.setCircularColor(item.color);
            }
        }
    }

	public static interface OnStateChangeListener {

		void onSendButtonClick(String text, int textColor, int textStroke, long fontId);

		void onDismiss(String text, int textColor, int textStroke, long fontId);

	}

	public class ColorItem {

		public boolean isStroke;
		public boolean first;
		public int color;
		public int strokeColor;

	}

	class FontAdapter extends BaseAdapter {

		private List<AssetInfo> list = new ArrayList<>();

		public void setData(List<AssetInfo> data){
			if(data == null || data.size() == 0){
				return;
			}
			list.addAll(data);
			notifyDataSetChanged();
		}

		public int getLastCheckedPosition(long fontId){
			int position = 0;
			for(int i = 0; i < list.size(); i++){
				AssetInfo asset = list.get(i);
				if(asset.getID() == fontId){
					if(asset.isAvailable()){
						position = i;
					}
					break;
				}
			}
			return position;
		}

		public List<AssetInfo> getData(){
			return list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public AssetInfo getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FontItemViewMediator localViewHolder;
			if (convertView == null) {
				localViewHolder = new FontItemViewMediator(parent);
				convertView = localViewHolder.getView();
				//Log.d("share_menu", "分享菜单的position：" + paramInt);
			} else {
				localViewHolder = (FontItemViewMediator) convertView.getTag();
			}
			final AssetInfo item = getItem(position);

			localViewHolder.setData(item);

			if(fontList.getCheckedItemPosition() == position){
				localViewHolder.setSelected(true);
			}else{
				localViewHolder.setSelected(false);
			}

			return convertView;
		}

	}

	class FontItemViewMediator {
		private ImageView image;
		private View select;
		private ImageView indiator;
		private TextView name;
		private View root;
		private AssetInfo fontInfo;

		public FontItemViewMediator(ViewGroup parent){
			root = View.inflate(parent.getContext(), R.layout.item_qupai_font_effect, null);
			select = root.findViewById(R.id.selected);
			image = (ImageView)root.findViewById(R.id.font_item_image);
			indiator = (ImageView)root.findViewById(R.id.indiator);
			name = (TextView)root.findViewById(R.id.item_name);
			root.setTag(this);
		}

		public void setData(AssetInfo font){
			this.fontInfo = font;
			name.setText(font.getTitle());

			ImageLoader.getInstance().displayImage(font.getBannerURIString(), image, UILOptions.DISK);
		}

		public void setSelected(boolean selected){
			select.setVisibility(selected ? View.VISIBLE : View.GONE);
		}

		public View getView(){
			return root;
		}

	}

}
