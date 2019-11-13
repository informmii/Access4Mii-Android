package ABS_CUSTOM_VIEW;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * Created by covetus-akki on 20/2/18.
 */

public class EditTextSemiBold extends AppCompatEditText {

    public EditTextSemiBold(Context context) {
        super(context);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "lato_semi_bold.ttf");
        this.setTypeface(face);
    }

    public EditTextSemiBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "lato_semi_bold.ttf");
        this.setTypeface(face);
    }

    public EditTextSemiBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "lato_semi_bold.ttf");
        this.setTypeface(face);
    }

    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);

    }

}
